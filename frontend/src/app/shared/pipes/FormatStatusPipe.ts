import { Pipe, PipeTransform } from '@angular/core';
import { LocationStatus } from '../../app.constants';
import { Pair } from '../model/helpers/Pair';
import { LangChangeEvent, TranslateService } from '@ngx-translate/core';
import { DatePipe } from '@angular/common';
import { merge, Observable, of } from 'rxjs';
import { map, mergeMap } from 'rxjs/operators';
import { Timeslot } from '../model/Timeslot';
import * as moment from 'moment';

@Pipe({ name: 'formatStatus' })
export class FormatStatusPipe implements PipeTransform {
  constructor(
    private translate: TranslateService,
    private datePipe: DatePipe
  ) {}

  transform(currentTimeslot: Timeslot): Observable<string> {
    // For some reason, onLangChange isn't a subject. So We insert the current language first.
    return merge(
      of({ lang: this.translate.currentLang } as LangChangeEvent),
      this.translate.onLangChange
    ).pipe(mergeMap(() => this.getValue(currentTimeslot)));
  }

  getValue(currentTimeslot: Timeslot): Observable<string> {
    if (!currentTimeslot) {
      return this.translate.get(
        'dashboard.locationDetails.status.statusClosed'
      ) as Observable<string>;
    }

    const now = moment()
    // status.second format: "yyyy-MM-dd hh:mm"
    if(currentTimeslot.getStartMoment().isBefore(now) && currentTimeslot.getEndMoment().isAfter(now)) {
        const datetime = currentTimeslot.getEndMoment().toDate()
        return this.translate
          .get('dashboard.locationDetails.status.statusOpen')
          .pipe(
            map((next: string) =>
              next.replace('{}', this.datePipe.transform(datetime, 'shortTime'))
            )
          );
    } else if(currentTimeslot.getStartMoment().isAfter(now) && currentTimeslot.getStartMoment().isSame(now, "day")) {
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
        .pipe(map((next: string) => next.replace('{}', datetime)));

    }
  }
}
