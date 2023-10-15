import {Component, OnInit} from '@angular/core';
import {Observable, of} from 'rxjs';
import {catchError, filter, map, tap} from 'rxjs/operators';
import {StatsService} from '../../../extensions/services/api/stats/stats.service';
import {LocationStat} from '../../../model/LocationStat';
import {LocationOverviewStat} from '../../../model/LocationOverviewStat';
import {Location} from '../../../model/Location';
import {LocationService} from '../../../extensions/services/api/locations/location.service';
import {InstitutionOverviewStat} from '../../../model/InstitutionOverviewStat';

@Component({
    selector: 'app-stats',
    templateUrl: './stats.component.html',
    styleUrls: ['./stats.component.scss'],
})
export class StatsComponent implements OnInit {
    loading = true;
    statsObs: Observable<LocationStat[]>;
    locationOverviewStat: LocationOverviewStat;
    institutionOverviewStat: InstitutionOverviewStat;
    locations: Observable<Location[]>;
    institutions = ['UGent', 'HoGent', 'Arteveldehogeschool', 'KULeuven', 'Luca', 'Odisee'];
    institutionsExtended = ['All', 'UGent', 'HoGent', 'Arteveldehogeschool', 'KULeuven', 'Luca', 'Odisee', 'StadGent', 'Other'];

    errorOnRetrievingStats = false; // booleanId = 0

    activeTab = 0;

    showSpecificDateInsteadOfCurrentOrNext = false;
    date = '';
    occupancy = 0;
    total = 0;
    totalNotReservable = 0;

    selectedLocationId = 0;
    locationOverviewFrom = '';
    locationOverviewTo = '';

    selectedInstitutionLocations = 'UGent';
    selectedInstitutionStudents = 'UGent';
    institutionOverviewFrom = '';
    institutionOverviewTo = '';

    constructor(
        private statsService: StatsService,
        private locationService: LocationService,
    ) {
    }

    ngOnInit(): void {
        this.statsObs = this.statsService.getStats()
            .pipe(
                map(stats => {
                    return stats.filter(stat => stat.timeslotDate !== null);
                }),
                tap((x) => {
                    this.loading = false;
                    this.occupancy = x.map(y => y.numberOfTakenSeats).reduce((a, b) => a + b, 0);
                    this.total = x.filter(y => y.reservable).map(y => y.numberOfSeats).reduce((a, b) => a + b, 0);
                    this.totalNotReservable = x.filter(y => !y.reservable).map(y => y.numberOfSeats).reduce((a, b) => a + b, 0);
                }),
                catchError((e) => {
                    this.errorOnRetrievingStats = !!e;
                    return of<LocationStat[]>([]);
                })
            );

        this.locations = this.locationService.getLocations();
    }

    onDateChange(): void {
        if (this.date) {
            this.loading = true;
            this.statsObs = this.statsService.getStatsAtDate(this.date).pipe(
                tap((x) => {
                    this.loading = false;
                    this.occupancy = x.map(y => y.numberOfTakenSeats).reduce((a, b) => a + b, 0);
                    this.total = x.map(y => y.numberOfSeats).reduce((a, b) => a + b, 0);
                    this.totalNotReservable = x.filter(y => !y.reservable).map(y => y.numberOfSeats).reduce((a, b) => a + b, 0);
                }),
                catchError((e) => {
                    this.loading = false;
                    this.errorOnRetrievingStats = !!e;
                    return of<LocationStat[]>([]);
                })
            );
        }
    }

    onShowChange(): void {
        if (this.showSpecificDateInsteadOfCurrentOrNext) {
            this.onDateChange();
        }
    }

    changeTab(event, tab: number): void {
        event.preventDefault();
        this.activeTab = tab;
    }

    onLocationOverviewChange(): void {
        if (this.selectedLocationId && this.locationOverviewFrom && this.locationOverviewTo) {
            this.loading = true;
            this.locationOverviewStat = null;
            this.statsService.getStatsForLocationFromTo(this.selectedLocationId, this.locationOverviewFrom, this.locationOverviewTo)
                .subscribe(x => {
                this.locationOverviewStat = x;
                this.loading = false;
            });
        }
    }

    onInstitutionOverviewChange(): void {
        if (this.selectedInstitutionLocations && this.selectedInstitutionStudents
            && this.institutionOverviewFrom && this.institutionOverviewTo) {
            this.loading = true;
            this.institutionOverviewStat = null;
            this.statsService.getStatsForInstitutionFromTo(this.selectedInstitutionLocations, this.selectedInstitutionStudents,
                this.institutionOverviewFrom, this.institutionOverviewTo)
                .subscribe(x => {
                this.institutionOverviewStat = x;
                this.loading = false;
            });
        }
    }
}
