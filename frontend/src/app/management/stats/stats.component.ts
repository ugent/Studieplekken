import {Component, OnInit} from '@angular/core';
import {Observable, of} from 'rxjs';
import {catchError, tap} from 'rxjs/operators';
import {StatsService} from '../../services/api/stats/stats.service';
import {LocationStat} from '../../shared/model/LocationStat';
import {LocationOverviewStat} from '../../shared/model/LocationOverviewStat';
import {Location} from '../../shared/model/Location';
import {LocationService} from '../../services/api/locations/location.service';

@Component({
    selector: 'app-stats',
    templateUrl: './stats.component.html',
    styleUrls: ['./stats.component.scss'],
})
export class StatsComponent implements OnInit {
    loading = true;
    statsObs: Observable<LocationStat[]>;
    locationOverviewStat: LocationOverviewStat;
    locations: Observable<Location[]>;
    institutions = ['UGent', 'HoGent', 'Arteveldehogeschool', 'KULeuven', 'Luca', 'Odisee'];

    errorOnRetrievingStats = false; // booleanId = 0

    activeTab = 0;

    showSpecificDateInsteadOfCurrentOrNext = false;
    date = '';

    selectedLocationId = 0;
    locationOverviewFrom = '';
    locationOverviewTo = '';

    constructor(
        private statsService: StatsService,
        private locationService: LocationService,
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

        this.locations = this.locationService.getLocations();

        this.selectedLocationId = 42;
        this.locationOverviewFrom = '2022-01-01';
        this.locationOverviewTo = '2023-02-28';
        this.onLocationOverviewChange();
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

    changeTab(event, tab: number): void {
        event.preventDefault();
        this.activeTab = tab;
    }

    onLocationOverviewChange(): void {
        if (this.selectedLocationId && this.locationOverviewFrom && this.locationOverviewTo) {
            this.statsService.getStatsForLocationFromTo(this.selectedLocationId, this.locationOverviewFrom, this.locationOverviewTo)
                .subscribe(x => {
                this.locationOverviewStat = x;
                console.log(x);
            });
        }
    }
}
