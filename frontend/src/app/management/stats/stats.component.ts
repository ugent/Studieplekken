import {Component, OnInit} from '@angular/core';
import {Observable, of} from 'rxjs';
import {catchError, tap} from 'rxjs/operators';
import {StatsService} from '../../services/api/stats/stats.service';
import {LocationStat} from '../../shared/model/LocationStat';

@Component({
    selector: 'app-stats',
    templateUrl: './stats.component.html',
    styleUrls: ['./stats.component.scss'],
})
export class StatsComponent implements OnInit {
    loading = true;
    statsObs: Observable<LocationStat[]>;

    errorOnRetrievingStats = false; // booleanId = 0

    showSpecificDateInsteadOfCurrentOrNext = false;
    date = '';

    constructor(
        private statsService: StatsService,
    ) {
    }

    ngOnInit(): void {
        this.statsObs = this.statsService.getStats().pipe(
            tap(() => (this.loading = false)),
            catchError((e) => {
                this.errorOnRetrievingStats = !!e;
                return of<LocationStat[]>([]);
            })
        );
    }

    onDateChange(): void {
        console.log('Date changed to: ' + this.date);
        if (this.date) {
            this.statsObs = this.statsService.getStatsAtDate(this.date).pipe(
                tap(() => (this.loading = false)),
                catchError((e) => {
                    this.errorOnRetrievingStats = !!e;
                    return of<LocationStat[]>([]);
                })
            );
        }
    }

    onShowChange(): void {
        if (this.showSpecificDateInsteadOfCurrentOrNext) {
            this.onDateChange();
        } else {
            this.ngOnInit();
        }
    }
}
