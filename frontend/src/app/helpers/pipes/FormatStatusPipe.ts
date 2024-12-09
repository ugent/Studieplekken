import {Pipe, PipeTransform} from '@angular/core';
import {LangChangeEvent, TranslateService} from '@ngx-translate/core';
import {DatePipe} from '@angular/common';
import {EMPTY, merge, Observable, of} from 'rxjs';
import {map, mergeMap} from 'rxjs/operators';
import {Timeslot} from '@/model/Timeslot';
import * as moment from 'moment';

@Pipe({
    name: 'formatStatus'
})
export class FormatStatusPipe implements PipeTransform {
    constructor(
        private translate: TranslateService,
        private datePipe: DatePipe
    ) {
    }

    /**
     * Transforms the given `Timeslot` into an observable string representation based on the current language.
     * 
     * This method listens for language change events and updates the string representation accordingly.
     * Initially, it inserts the current language before subscribing to language change events.
     * 
     * @param {Timeslot} currentTimeslot - The timeslot to be transformed.
     * @returns {Observable<string>} An observable that emits the string representation of the timeslot.
     */
    public transform(currentTimeslot: Timeslot): Observable<string> {
        return of(this.translate.onLangChange).pipe(
            mergeMap(() => this.getValue(currentTimeslot))
        );
    }

    /**
     * Returns an observable string representing the status of the given timeslot.
     * 
     * The status is determined based on the current time and the start and end times of the timeslot.
     * 
     * - If the timeslot is null or undefined, it returns the status as "closed".
     * - If the current time is within the timeslot's start and end times, it returns the status as "open" with the end time.
     * - If the timeslot starts later today, it returns the status as "closed but active" with the start time.
     * - Otherwise, it returns the status as "closed but upcoming" with the start time.
     * 
     * @param {Timeslot} currentTimeslot - The timeslot to check the status for.
     * @returns {Observable<string>} An observable string representing the status of the timeslot.
     */
    public getValue(currentTimeslot: Timeslot): Observable<string> {
        const now = moment.now();

        if (!currentTimeslot) {
            return this.translate.get('dashboard.locationDetails.status.statusClosed');
        }

        if (currentTimeslot.getStartMoment().isBefore(now) && currentTimeslot.getEndMoment().isAfter(now)) {
            const datetime = currentTimeslot.getEndMoment().toDate()
            return this.translate
                .get('dashboard.locationDetails.status.statusOpen')
                .pipe(
                    map((next: string) =>
                        next.replace('{}', this.datePipe.transform(datetime, 'shortTime'))
                    )
                );
        } else if (currentTimeslot.getStartMoment().isAfter(now) && currentTimeslot.getStartMoment().isSame(now, "day")) {
            const datetime = currentTimeslot.getStartMoment().toDate();
            return this.translate
                .get('dashboard.locationDetails.status.statusClosedActive')
                .pipe(
                    map((next: string) =>
                        next.replace('{}', this.datePipe.transform(datetime, 'shortTime'))
                    )
                );

        } else {
            const datetime = currentTimeslot.getStartMoment().toDate().toLocaleString();
            return this.translate
                .get('dashboard.locationDetails.status.statusClosedUpcoming')
                .pipe(
                    map((next: string) =>
                        next.replace('{}', datetime)
                    )
                );
        }
    }
}
