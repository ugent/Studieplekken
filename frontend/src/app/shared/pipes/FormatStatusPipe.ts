import { Pipe, PipeTransform } from '@angular/core';
import { LocationStatus } from '../../app.constants';
import { Pair } from '../../shared/model/helpers/Pair';
import { LangChangeEvent, TranslateService } from '@ngx-translate/core';
import { DatePipe } from '@angular/common';
import { merge, Observable, of } from 'rxjs';
import { map, mergeAll, mergeMap, switchMap } from 'rxjs/operators';




@Pipe({ name: 'formatStatus' })
export class FormatStatusPipe implements PipeTransform {

    constructor(private translate: TranslateService,
                private datePipe: DatePipe) {
    }

    transform(status: Pair<LocationStatus, string>): Observable<string> {
        // For some reason, onLangChange isn't a subject. So We insert the current language first.
        return merge(of({lang: this.translate.currentLang} as LangChangeEvent), this.translate.onLangChange)
                .pipe(
                    mergeMap(() => this.getValue(status))
                    );
    }

    getValue(status: Pair<LocationStatus, string>): Observable<string> {
        if (!status) {
            return this.translate.get('general.notAvailableAbbreviation');
        }

        // status.second format: "yyyy-MM-dd hh:mm"
        switch (status.first) {
            case LocationStatus.OPEN: {
                const datetime = new Date(status.second);
                return this.translate.get('dashboard.locationDetails.status.statusOpen')
                    .pipe(map(
                        next => next.replace('{}', this.datePipe.transform(datetime, 'shortTime')))
                    );
                break;
            }
            case LocationStatus.CLOSED: {
                return this.translate.get('dashboard.locationDetails.status.statusClosed');
                break;
            }
            case LocationStatus.CLOSED_ACTIVE: {
                const datetime = new Date(status.second);
                return this.translate.get('dashboard.locationDetails.status.statusClosedActive').pipe(map(
                    next => next.replace('{}', this.datePipe.transform(datetime, 'shortTime'))
                ));
                break;
            }
            case LocationStatus.CLOSED_UPCOMING: {
                const datetime = new Date(status.second).toLocaleString();
                return this.translate.get('dashboard.locationDetails.status.statusClosedUpcoming').pipe(map(
                    next => next.replace('{}', datetime)
                ));
                break;
            }
        }
    }
}
