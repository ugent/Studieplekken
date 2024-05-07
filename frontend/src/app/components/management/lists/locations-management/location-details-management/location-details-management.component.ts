import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {EMPTY, Observable, ReplaySubject, Subject} from 'rxjs';
import {Location} from '../../../../../model/Location';
import {LocationService} from '../../../../../services/api/locations/location.service';
import {catchError, filter, startWith, switchMap} from 'rxjs/operators';
import {Authority} from '../../../../../model/Authority';
import {Building} from '../../../../../model/Building';
import {AuthenticationService} from '../../../../../services/authentication/authentication.service';
import {AuthoritiesService} from '../../../../../services/api/authorities/authorities.service';
import {BuildingService} from '../../../../../services/api/buildings/buildings.service';
import {Timeslot} from '../../../../../model/Timeslot';
import {TimeslotsService} from '../../../../../services/api/calendar-periods/timeslot.service';

@Component({
    selector: 'app-location-details-management',
    templateUrl: './location-details-management.component.html',
    styleUrls: ['./location-details-management.component.scss'],
})
export class LocationDetailsManagementComponent implements OnInit {

    protected locationObs$: Observable<Location>;
    protected authoritiesObs$: Observable<Authority[]>;
    protected buildingsObs$: Observable<Building[]>;
    protected timeslotsObs$: Observable<Timeslot[]>;

    protected refresh$: Subject<void>;

    constructor(
        private locationService: LocationService,
        private authenticationService: AuthenticationService,
        private authoritiesService: AuthoritiesService,
        private buildingsService: BuildingService,
        private timeslotsService: TimeslotsService,
        private route: ActivatedRoute,
        private router: Router
    ) {
        this.refresh$ = new ReplaySubject();
    }

    ngOnInit(): void {
        const locationId = Number(
            this.route.snapshot.paramMap.get('locationId')
        );

        this.locationObs$ = this.refresh$.pipe(
            startWith(EMPTY), switchMap(() =>
                this.locationService.getLocation(locationId).pipe(
                    catchError((_) => {
                        void this.router.navigate(['/management/locations']);
                        return EMPTY;
                    }), filter(location => !!location)
                )
            )
        );

        this.authoritiesObs$ = this.authenticationService.getUserObs().pipe(
            switchMap(user =>
                user.isAdmin()
                    ? this.authoritiesService.getAllAuthorities()
                    : this.authoritiesService.getAuthoritiesOfUser(user.userId)
            )
        );

        this.timeslotsObs$ = this.locationObs$.pipe(
            switchMap(location =>
                this.timeslotsService.getTimeslotsOfLocation(location.locationId)
            )
        );

        this.buildingsObs$ = this.buildingsService.getAllBuildings();
    }
}
