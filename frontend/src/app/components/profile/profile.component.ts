import {Component, OnInit} from '@angular/core';
import {BreadcrumbService} from '../stad-gent-components/header/breadcrumbs/breadcrumb.service';
import {User} from '@/model/User';
import {Observable} from 'rxjs';
import {AuthenticationService} from '@/services/authentication/authentication.service';
import {
    LocationReservationsService
} from '@/services/api/location-reservations/location-reservations.service';
import {switchMap} from 'rxjs/operators';
import {LocationReservation} from '@/model/LocationReservation';
import {map} from 'rxjs/internal/operators/map';

@Component({
    selector: 'app-profile',
    templateUrl: './profile.component.html',
    styleUrls: ['./profile.component.scss'],
})
export class ProfileComponent implements OnInit {

    protected userObs$: Observable<User>;
    protected reservationsObs$: Observable<LocationReservation[]>;

    constructor(
        private breadcrumbService: BreadcrumbService,
        private authenticationService: AuthenticationService,
        private reservationsService: LocationReservationsService
    ) {
    }

    ngOnInit(): void {
        this.userObs$ = this.authenticationService.getUserObs();

        this.reservationsObs$ = this.userObs$.pipe(
            switchMap((user: User) =>
                this.reservationsService.getLocationReservationsOfUser(user.userId)
            ),
            map(reservations =>
                reservations.sort((a, b) =>
                    b.timeslot.timeslotDate?.diff(a.timeslot.timeslotDate)
                )
            )
        );

        this.breadcrumbService.setCurrentBreadcrumbs([{
            pageName: 'Profile', url: '/profile/overview'
        }]);
    }
}
