import {
    Component,
    EventEmitter,
    Input,
    OnChanges,
    Output,
    SimpleChanges,
    ViewChild
} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {CalendarView} from 'angular-calendar';
import * as moment from 'moment';
import {Moment} from 'moment';
import {ReplaySubject, Subject} from 'rxjs';
import { TimeslotsService } from '@/services/api/calendar-periods/timeslot.service';
import { LocationReservationsService } from '@/services/api/location-reservations/location-reservations.service';
import {Suggestion, TimeslotGroupService} from '@/services/timeslots/timeslot-group/timeslot-group.service';
import {
    TimeslotCalendarEvent,
    TimeslotCalendarEventService
} from '@/services/timeslots/timeslot-calendar-event/timeslot-calendar-event.service';

import {Location} from '@/model/Location';
import {LocationReservation} from '@/model/LocationReservation';
import {Timeslot} from '@/model/Timeslot';
import {booleanSorter} from '@/helpers/Util';
import {ModalComponent} from '@/components/stad-gent-components/molecules/modal/modal.component';
import {
    LocationAddTimeslotDialogComponent
} from './location-add-timeslot-dialog/location-add-timeslot-dialog.component';

import StartOf = moment.unitOfTime.StartOf;

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
export class LocationCalendarComponent implements OnChanges {

    @ViewChild('addTimeslotModal') modifyModal: LocationAddTimeslotDialogComponent;
    @ViewChild('deleteTimeslotModal') deleteModal: ModalComponent;
    @ViewChild('copyTimeslotModal') copyModal: ModalComponent;

    @Input() location: Location;
    @Input() timeslots: Timeslot[];

    @Output() updatedTimeslots: EventEmitter<void>;

    protected events: TimeslotCalendarEvent[];
    protected suggestions: Suggestion[];
    protected selected: Timeslot;
    protected reservations: LocationReservation[];

    protected isSuccess: boolean;
    protected refresh: Subject<void>;

    protected calendarViewStyle: CalendarView = CalendarView.Week;
    protected currentTime = moment();

    constructor(
        private timeslotService: TimeslotsService,
        private locationReservationService: LocationReservationsService,
        private translate: TranslateService,
        private timeslotGroupService: TimeslotGroupService,
        private timeslotCalendarEventService: TimeslotCalendarEventService,
    ) {
        this.updatedTimeslots = new EventEmitter();
        this.refresh = new ReplaySubject();
    }

    /**
     * Set up suggestions and events on input changes.
     * @param changes
     */
    public ngOnChanges(changes: SimpleChanges): void {
        // Input: location and timeslots.
        // We calculate the suggestions from the timeslots and current location.
        if (changes.timeslots || changes.location) {
            this.setupSuggestions();
        }

        // We calculate the events from the timeslots.
        if (changes.timeslots && this.timeslots) {
            this.setupEvents();
        }
    }

    /**
     * Set up the suggestions for the list of timeslots.
     */
    public setupSuggestions(): void {
        if (this.timeslots === undefined || this.timeslots === null) {
            this.suggestions = [];
        } else {
            this.suggestions = this.timeslotGroupService.getSuggestions(
                this.timeslots,
                this.location
            );
        }
    }

    /**
     * Set up the calendar event for each timeslot.
     */
    public setupEvents(): void {
        const language = this.translate.currentLang;

        // Map timeslots and suggested timeslots to calendar events and concatenate them.
        this.events = this.timeslots?.map(timeslot =>
            this.timeslotCalendarEventService.timeslotToCalendarEvent(timeslot, language)
        ).concat(
            this.suggestions.map(suggestion =>
                this.timeslotCalendarEventService.suggestedTimeslotToCalendarEvent(
                    suggestion.copy, language
                )
            )
        ) ?? [];

        this.refresh.next();
    }

    /**
     * Set up the reservations for the selected timeslot.
     */
    public setupReservations(): void {
        this.locationReservationService.getLocationReservationsOfTimeslot(
            this.selected.timeslotSequenceNumber
        ).subscribe(reservations =>
            this.reservations = reservations
        );
    }

    /**
     * Handle a calendar event (timeslot) click event.
     * @param event
     */
    public timeslotPickedHandler(event: { timeslot: Timeslot }): void {
        const timeslot = event.timeslot;

        // event is a non-reservable calendar period.
        if (timeslot) {
            this.selected = timeslot;

            this.setupReservations();
        }
    }

    /**
     * Handle a calendar hour click event, opening the add modal
     * with a default start hour.
     * @param location
     * @param date
     */
    public hourPickedHandler(location: Location, date: Moment): void {
        const openingHour = moment(
            date?.format('HH:mm'), 'HH:mm'
        );

        this.selected = new Timeslot().setDate(date).setLocationId(location.locationId).setOpeningHour(openingHour);

        this.modifyModal.openModal();
    }

    /**
     * Prepare the creation of a timeslot through a modal.
     */
    public prepareAdd(): void {
        this.isSuccess = null;
        this.selected = null;
        this.modifyModal.openModal();
    }

    /**
     * Prepare the update of a timeslot through a modal.
     */
    public prepareUpdate(): void {
        this.isSuccess = null;
        this.modifyModal.openModal();
    }

    /**
     * Prepare the deletion of a timeslot through a modal.
     */
    public prepareDelete(): void {
        this.isSuccess = null;
        this.deleteModal.open();
    }

    /**
     * Prepare the copy of a timeslot through a modal.
     */
    public prepareCopy(): void {
        this.isSuccess = null;
        this.copyModal.open();
    }

    /**
     * Adds a new timeslot using the timeslot service and updates the component state accordingly.
     * 
     * @param {Timeslot} timeslot - The timeslot to be added.
     */
    public storeAddTimeslot(timeslot: Timeslot): void {
        this.isSuccess = undefined;

        this.timeslotService.addTimeslot(timeslot).subscribe(() => {
            this.updatedTimeslots.emit();
            this.isSuccess = true;
        }, () => {
            this.isSuccess = false;
        });

        this.modifyModal.closeModal();
    }

    public storeUpdateTimeslot(timeslot: Timeslot): void {
        this.isSuccess = undefined;

        this.timeslotService.updateTimeslot(timeslot).subscribe(() => {
            this.updatedTimeslots.emit();
            this.isSuccess = true;
        }, () => {
            this.isSuccess = false;
        });

        this.modifyModal.closeModal();
    }

    public storeDelete(timeslot: Timeslot): void {
        this.isSuccess = undefined;
        this.selected = null;

        this.timeslotService.deleteTimeslot(timeslot).subscribe(() => {
            this.updatedTimeslots.emit();
            this.isSuccess = true;
        }, () => {
            this.isSuccess = false;
        });

        this.deleteModal.close();
    }

    public storeCopy(timeslot: Timeslot, weekOffset: string, location: Location, keepReservableFrom: boolean): void {
        this.isSuccess = undefined;
        this.selected = null;

        const newTimeslot = this.timeslotGroupService.copy(
            timeslot, moment(weekOffset), location, false, !keepReservableFrom
        );

        this.timeslotService.addTimeslot(newTimeslot).subscribe(() => {
            this.updatedTimeslots.emit();
            this.isSuccess = true;
        }, () => {
            this.isSuccess = false;
        });

        this.copyModal.close();
    }

    public approveAll(): void {
        this.getCurrentSuggestions().forEach(suggestion =>
            this.approve(suggestion.copy)
        );
    }

    public approve(timeslot: Timeslot): void {
        this.selected = null;
        this.timeslotService.addTimeslot(timeslot).subscribe(() => {
            this.updatedTimeslots.emit();
        });
    }

    public rejectAll(): void {
        this.getCurrentSuggestions().forEach(suggestion =>
            this.reject(suggestion.copy)
        );
    }

    public reject(timeslot: Timeslot): void {
        this.selected = null;
        const currentSuggestion = this.getCurrentSuggestions().find(suggestion =>
            suggestion.copy === timeslot
        );

        this.timeslotService.setRepeatable(
            currentSuggestion.model, false
        ).subscribe(() => {
            this.updatedTimeslots.emit();
        });
    }


    public timeslotGroupData(): TypeOption[] {
        const timeslots = this.timeslots;

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

    public isSuggestion(timeslot: Timeslot): boolean {
        return this.suggestions.some(suggestion =>
            timeslot.timeslotSequenceNumber === suggestion.copy.timeslotSequenceNumber
        );
    }

    public getCurrentSuggestions(): Suggestion[] {
        let granularity: StartOf = 'day';

        if (this.calendarViewStyle === CalendarView.Week) {
            granularity = 'isoWeek';
        }

        if (this.calendarViewStyle === CalendarView.Month) {
            granularity = 'month';
        }

        return this.timeslotGroupService.filterSuggestionsByMoment(
            this.suggestions, this.currentTime, granularity
        );
    }
}
