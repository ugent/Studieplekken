import {Component, OnInit} from '@angular/core';
import {Observable, of} from 'rxjs';
import {catchError, tap} from 'rxjs/operators';
import {StatsService} from '../../services/api/stats/stats.service';
import {LocationStat} from '../../shared/model/LocationStat';
import {LocationOverviewStat} from '../../shared/model/LocationOverviewStat';
import {Location} from '../../shared/model/Location';
import {LocationService} from '../../services/api/locations/location.service';
import {InstitutionOverviewStat} from '../../shared/model/InstitutionOverviewStat';

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
        this.statsObs = this.statsService.getStats().pipe(
            tap(() => (this.loading = false)),
            catchError((e) => {
                this.errorOnRetrievingStats = !!e;
                return of<LocationStat[]>([]);
            })
        );

        this.locations = this.locationService.getLocations();

        this.onLocationOverviewChange();
    }

    onDateChange(): void {
        if (this.date) {
            this.loading = true;
            this.statsObs = this.statsService.getStatsAtDate(this.date).pipe(
                tap(() => (this.loading = false)),
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
