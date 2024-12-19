import {Component, OnInit} from '@angular/core';
import {combineLatest, EMPTY, Observable, ReplaySubject, Subject} from 'rxjs';
import {User} from '@/model/User';
import {ActivatedRoute} from '@angular/router';
import {AuthenticationService} from '@/services/authentication/authentication.service';
import {UserService} from '@/services/api/users/user.service';
import {map, startWith, switchMap, tap} from 'rxjs/operators';
import {Authority} from '@/model/Authority';
import {AuthoritiesService} from '@/services/api/authorities/authorities.service';
import {LocationReservation} from '@/model/LocationReservation';
import {PenaltyList, PenaltyService} from '@/services/api/penalties/penalty.service';
import {
    LocationReservationsService
} from '@/services/api/location-reservations/location-reservations.service';
import { LocationService } from '@/services/api/locations/location.service';
import { Location } from '@/model/Location';

@Component({
    selector: 'app-user-details-management',
    templateUrl: './user-details-management.component.html',
    styleUrls: ['./user-details-management.component.scss'],
})
export class UserDetailsManagementComponent implements OnInit {

    protected currentUserObs$: Observable<User>;
    protected loggedInUserObs$: Observable<User>;
    protected reservationsObs$: Observable<LocationReservation[]>;
    protected penaltiesObs$: Observable<PenaltyList>;
    protected addedAuthoritiesObs$: Observable<Authority[]>;
    protected addableAuthoritiesObs$: Observable<Authority[]>;
    protected locationsObs$: Observable<Location[]>;
    protected refresh$: Subject<void>;

    protected showRolesManagement: boolean;
    protected userId: string;

    constructor(
        private userService: UserService,
        private route: ActivatedRoute,
        private authenticationService: AuthenticationService,
        private authoritiesService: AuthoritiesService,
        private penaltyService: PenaltyService,
        private reservationService: LocationReservationsService,
        private locationService: LocationService
    ) {
        this.refresh$ = new ReplaySubject();
    }

    ngOnInit(): void {
        this.userId = this.route.snapshot.paramMap.get('id');

        const tapper = tap((currentUser: User) => {
            // Get the added authorities of the user.
            this.addedAuthoritiesObs$ = this.authoritiesService.getAuthoritiesOfUser(
                currentUser.userId
            );
            // Get the reservations of the user.
            this.reservationsObs$ = this.reservationService.getLocationReservationsOfUser(
                currentUser.userId
            );
            // Get the penalties of the user.
            this.penaltiesObs$ = this.penaltyService.getPenaltiesOfUserById(
                currentUser.userId
            );
            // Query the logged-in user.
            this.loggedInUserObs$ = this.authenticationService.getUserObs().pipe(
                tap((user: User) =>
                    // Show roles only for admins.
                    this.showRolesManagement = user.isAdmin()
                )
            );
            // Get the locations.
            this.locationsObs$ = this.locationService.getAllLocations();
            // Query the addable authorities.
            this.addableAuthoritiesObs$ = combineLatest([this.loggedInUserObs$, this.addedAuthoritiesObs$]).pipe(
                switchMap(([user, addedAuthorities]) =>
                    (user.isAdmin() 
                        ? this.authoritiesService.getAllAuthorities() 
                        : this.authoritiesService.getAuthoritiesOfUser(user.userId)
                    ).pipe(
                        map(allAuthorities => allAuthorities.filter(authority =>
                                !addedAuthorities.some(addedAuthority => authority.authorityId === addedAuthority.authorityId)
                            )
                        )
                    )
                )
            );
        })

        // Query the selected user by ID.
        this.currentUserObs$ = this.refresh$.pipe(
            startWith(EMPTY), switchMap(() =>
                this.userService.getUserByAUGentId(this.userId).pipe(tapper)
            )
        );
    }

    refresh(): void {
        this.refresh$.next();
    }
}
