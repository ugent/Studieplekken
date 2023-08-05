import {Component, Input, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {CalendarView} from 'angular-calendar';
import * as moment from 'moment';
import {Moment} from 'moment';
import {BehaviorSubject, Observable, ReplaySubject, Subject, Subscription} from 'rxjs';
import {filter, find, first, map, mergeMap, switchMap, take, takeUntil, tap} from 'rxjs/operators';
import {TimeslotsService} from 'src/app/extensions/services/api/calendar-periods/timeslot.service';
import {LocationReservationsService} from 'src/app/extensions/services/api/location-reservations/location-reservations.service';
import {Suggestion, TimeslotGroupService} from 'src/app/extensions/services/timeslots/timeslot-group/timeslot-group.service';
import {
    TimeslotCalendarEvent,
    TimeslotCalendarEventService
} from 'src/app/extensions/services/timeslots/timeslot-calendar-event/timeslot-calendar-event.service';

import {Location} from 'src/app/model/Location';
import {LocationReservation} from 'src/app/model/LocationReservation';
import {Timeslot} from 'src/app/model/Timeslot';
import {booleanSorter} from 'src/app/extensions/util/Util';
import {ModalComponent} from '../../../../../stad-gent-components/molecules/modal/modal.component';
import {
    LocationAddTimeslotDialogComponent
} from './location-add-timeslot-dialog/location-add-timeslot-dialog.component';

import StartOf = moment.unitOfTime.StartOf;
import {logging} from 'protractor';

type TypeOption = {
    date: string;
    openingHour: string;
    closingHour: string;
    reservable: boolean;
    reservableFrom: string;
    seatCount: number;
    timeslots: Timeslot[];
    repeatable: boolean;
};

@Component({
    selector: 'app-location-calendar',
    templateUrl: './location-calendar.component.html',
    styleUrls: ['./location-calendar.component.scss']
})
export class LocationCalendarComponent implements OnInit, OnDestroy {

    @ViewChild('addTimeslotModal') modifyModal: LocationAddTimeslotDialogComponent;
    @ViewChild('deleteTimeslotModal') deleteModal: ModalComponent;
    @ViewChild('copyTimeslotModal') copyModal: ModalComponent;

    @Input() locationSub: Observable<Location>;

    protected timeslotsSub: Subject<Timeslot[]>;
    protected suggestionsSub: Subject<Suggestion[]>;
    protected selectedSub: Subject<Timeslot>;
    protected reservationsSub: Subject<LocationReservation[]>;
    protected eventsSub: Subject<TimeslotCalendarEvent[]>;

    protected isError: Subject<boolean>;
    protected isSuccess: Subject<boolean>;
    protected showBulk: Subject<boolean>;
    protected refresh: Subject<unknown>;

    protected unsubscribe: Subject<unknown>;

    protected calendarViewStyle: CalendarView = CalendarView.Week;
    protected currentTime = moment();

    constructor(
        private timeslotService: TimeslotsService,
        private locationReservationService: LocationReservationsService,
        private translate: TranslateService,
        private timeslotGroupService: TimeslotGroupService,
        private timeslotCalendarEventService: TimeslotCalendarEventService,
    ) {
        this.timeslotsSub = new ReplaySubject();
        this.selectedSub = new ReplaySubject();
        this.suggestionsSub = new ReplaySubject();
        this.reservationsSub = new ReplaySubject();
        this.eventsSub = new ReplaySubject();

        this.isError = new ReplaySubject();
        this.isSuccess = new ReplaySubject();
        this.showBulk = new ReplaySubject();
        this.refresh = new Subject();
        this.unsubscribe = new Subject();
    }

    ngOnInit(): void {
        this.setupTimeslots();

        this.selectedSub.pipe(
            takeUntil(this.unsubscribe),
            switchMap(timeslot =>
                this.isSuggestion(timeslot).pipe(
                    filter(isSuggestion => !isSuggestion && timeslot.reservable),
                    switchMap(() =>
                        this.locationReservationService.getLocationReservationsOfTimeslot(
                            timeslot.timeslotSequenceNumber
                        )
                    )
                )
            )
        ).subscribe(reservations => {
            this.reservationsSub.next(reservations);
        });
    }

    ngOnDestroy(): void {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }


    setupTimeslots(): void {
        this.locationSub.pipe(
            filter(location => !!location), first(), switchMap((location: Location) =>
                this.timeslotService.getTimeslotsOfLocation(location.locationId).pipe(
                    map(timeslots => ({location, timeslots}))
                )
            )
        ).subscribe(({location, timeslots}) => {
            const suggestions = this.timeslotGroupService.getSuggestions(timeslots, location);

            this.suggestionsSub.next(
                suggestions
            );

            const suggestionEvents = suggestions.map(suggestion =>
                this.timeslotCalendarEventService.suggestedTimeslotToCalendarEvent(
                    suggestion.copy, this.translate.currentLang
                )
            );

            // Fill the events based on the calendar periods.
            this.eventsSub.next(
                timeslots.map(timeslot =>
                    this.timeslotCalendarEventService.timeslotToCalendarEvent(timeslot, this.translate.currentLang)
                ).concat(suggestionEvents)
            );

            // Refresh the calendar.
            this.refresh.next();
        });
    }

    timeslotPickedHandler(event: { timeslot: Timeslot }): void {
        const timeslot = event.timeslot;

        // event is a non-reservable calendar period.
        if (timeslot) {
            this.selectedSub.next(timeslot);
        }
    }

    hourPickedHandler(location: Location, date: Moment): void {
        const openingHour = moment(
            date?.format('HH:mm'), 'HH:mm'
        );
        this.selectedSub.next(
            new Timeslot().setDate(date).setLocationId(location.locationId).setOpeningHour(openingHour)
        );
        this.modifyModal.openModal();
    }

    prepareAdd(): void {
        this.isSuccess.next(null);
        this.selectedSub.next(null);
        this.modifyModal.openModal();
    }

    prepareUpdate(timeslot: Timeslot): void {
        this.isSuccess.next(null);
        this.selectedSub.next(timeslot);
        this.modifyModal.openModal();
    }

    prepareDelete(timeslot: Timeslot): void {
        this.isSuccess.next(null);
        this.selectedSub.next(timeslot);
        this.deleteModal.open();
    }

    prepareCopy(timeslot: Timeslot): void {
        this.isSuccess.next(null);
        this.selectedSub.next(timeslot);
        this.copyModal.open();
    }

    storeAdd(timeslot: Timeslot): void {
        this.isSuccess.next(undefined);
        this.timeslotService.addTimeslot(timeslot).subscribe(() =>
            this.setupTimeslots()
        );
        this.modifyModal.closeModal();
    }

    storeUpdate(timeslot: Timeslot): void {
        this.isSuccess.next(undefined);
        this.timeslotService.updateTimeslot(timeslot).subscribe(() => {
            this.setupTimeslots();
            this.isSuccess.next(true);
        }, () => {
            this.isSuccess.next(false);
        });
        this.modifyModal.closeModal();
    }

    storeDelete(timeslot: Timeslot): void {
        this.isSuccess.next(undefined);
        this.timeslotService.deleteTimeslot(timeslot).subscribe(() => {
            this.setupTimeslots();
            this.isSuccess.next(true);
        }, () => {
            this.isSuccess.next(false);
        });
        this.deleteModal.close();
    }

    storeCopy(timeslot: Timeslot, weekOffset: string, location: Location, keepReservableFrom: boolean): void {
        this.isSuccess.next(undefined);
        const newTimeslot = this.timeslotGroupService.copy(
            timeslot, moment(weekOffset), location, false, !keepReservableFrom
        );
        this.timeslotService.addTimeslot(newTimeslot).subscribe(() => {
            this.setupTimeslots();
            this.isSuccess.next(true);
        }, () => {
            this.isSuccess.next(false);
        });
        this.copyModal.close();
    }

    approveAll(): void {
        this.getCurrentSuggestions().subscribe(suggestions =>
            suggestions.forEach(suggestion => this.approve(suggestion.copy))
        );
    }

    approve(timeslot: Timeslot): void {
        this.timeslotService.addTimeslot(timeslot).subscribe(() =>{
            this.setupTimeslots();
            this.selectedSub.next(timeslot);
        });
    }

    rejectAll(): void {
        this.getCurrentSuggestions().subscribe(suggestions =>
            suggestions.forEach(suggestion => this.reject(suggestion.copy))
        );
    }

    reject(timeslot: Timeslot): void {
        this.getCurrentSuggestions().pipe(
            mergeMap(suggestions =>
                suggestions
            ),
            find(suggestion =>
                suggestion.copy === timeslot
            ),
            switchMap(suggestion =>
                this.timeslotService.setRepeatable(
                    suggestion.model, false
                )
            )
        ).subscribe(() => {
            this.setupTimeslots();
            this.selectedSub.next(timeslot);
        });
    }


    timeslotGroupData(timeslots: Timeslot[]): TypeOption[] {
        if (!timeslots) {
            return [];
        }

        const perGroup = this.timeslotGroupService.groupTimeslots(
            timeslots
        );
        const bestPerGroup = this.timeslotGroupService.getOldestTimeslotPerGroup(
            timeslots
        );

        const toSort = Array.from(perGroup);

        // Sort first on weekday, then on repeatability.
        // The repeatable (=> currently active, used) periods will come first, in a week overview (sorted by day)
        // The non-repeatable, old periods are second.
        toSort.sort((a, b) =>
            bestPerGroup.get(a[0]).timeslotDate.isoWeekday() - bestPerGroup.get(b[0]).timeslotDate.isoWeekday()
        );
        toSort.sort(
            booleanSorter(t =>
                bestPerGroup.get(t[0]).repeatable
            )
        );

        return toSort.map(([g, t]) =>
            this.getGroupDetails(t, bestPerGroup.get(g))
        );
    }

    /**
     * This function gets the details for the table.
     * Usually, a timeslot group will be on one day, in one time category.
     * However, with migrated timeslots, this might not be the case. We'll group all these together.
     */
    private getGroupDetails(timeslot: Timeslot[], oldestTimeslot: Timeslot): TypeOption {
        const days = timeslot.map(t => t.timeslotDate.day());
        const allOnSameWeekDay = days.every((a) => a === days[0]);
        if (!allOnSameWeekDay) {
            // This shouldn't happen.
            // Only happens with migrated timeslots.
            return {
                date: 'Varies',
                openingHour: 'Varies',
                closingHour: 'Varies',
                reservable: true,
                reservableFrom: 'Varies',
                repeatable: false,
                seatCount: timeslot[0].seatCount,
                timeslots: timeslot
            };
        }

        const allOnSameDay = timeslot.every(t => t.timeslotDate.isSame(timeslot[0].timeslotDate, 'day'));

        return {
            date: allOnSameDay ? oldestTimeslot.timeslotDate.format('DD/MM/YYYY') : oldestTimeslot.timeslotDate.format('dddd'),
            openingHour: oldestTimeslot.openingHour.format('HH:mm'),
            closingHour: oldestTimeslot.closingHour.format('HH:mm'),
            reservable: oldestTimeslot.reservable,
            reservableFrom: oldestTimeslot.reservable ?
                allOnSameDay ?
                    oldestTimeslot.reservableFrom.format('DD/MM/YYYY HH:mm') :
                    oldestTimeslot.reservableFrom.format('dddd HH:mm') :
                'Not reservable',
            seatCount: oldestTimeslot.seatCount,
            timeslots: timeslot,
            repeatable: oldestTimeslot.repeatable
        };
    }

    isSuggestion(timeslot: Timeslot): Observable<boolean> {
        return this.suggestionsSub.pipe(
            map((suggestions: any[]) =>
                suggestions.map(s => s.copy)
            ),
            map((mappedSuggestions: any[]) =>
                mappedSuggestions.some(suggestion => timeslot.timeslotSequenceNumber === suggestion.timeslotSequenceNumber)
            ), first()
        );
    }

    getCurrentSuggestions(): Observable<Suggestion[]> {
        return this.suggestionsSub.pipe(
            map(suggestions => {
                let granularity: StartOf = 'day';

                if (this.calendarViewStyle === CalendarView.Week) {
                    granularity = 'isoWeek';
                }

                if (this.calendarViewStyle === CalendarView.Month) {
                    granularity = 'month';
                }

                return this.timeslotGroupService.filterSuggestionsByMoment(suggestions, this.currentTime, granularity);
            }), first()
        );
    }
}
