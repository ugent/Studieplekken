import {ChangeDetectionStrategy, Component, OnDestroy, OnInit, TemplateRef, ViewChild} from '@angular/core';
import {BehaviorSubject, combineLatest, ReplaySubject, Subject, Subscription} from 'rxjs';
import {User} from '../../../extensions/model/User';
import {Building} from '../../../extensions/model/Building';
import {Location} from '../../../extensions/model/Location';
import {LocationService} from '../../../extensions/services/api/locations/location.service';
import {AuthenticationService} from '../../../extensions/services/authentication/authentication.service';
import {AuthoritiesService} from '../../../extensions/services/api/authorities/authorities.service';
import {BuildingService} from '../../../extensions/services/api/buildings/buildings.service';
import {MatDialog} from '@angular/material/dialog';
import {TimeslotsService} from '../../../extensions/services/api/calendar-periods/timeslot.service';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {first, map, mergeMap} from 'rxjs/operators';
import {Authority} from '../../../extensions/model/Authority';
import {DeleteAction, ListAction, TableAction, TableMapper} from '../../../extensions/model/Table';
import {Router} from '@angular/router';
import {Timeslot} from '../../../extensions/model/Timeslot';

@Component({
    selector: 'app-locations-management',
    templateUrl: './locations-management.component.html',
    styleUrls: ['./locations-management.component.scss']
})
export class LocationsManagementComponent implements OnInit, OnDestroy {

    @ViewChild('deleteLocationModal') deleteLocationModal: TemplateRef<any>;
    @ViewChild('addLocationModal') addLocationModal: TemplateRef<any>;

    protected userSub: Subject<User>;
    protected authoritiesSub: Subject<Authority[]>;
    protected locationsSub: Subject<Location[]>;
    protected buildingsSub: Subject<Building[]>;
    protected timeslotSub: Subject<Timeslot[]>;
    protected selectLocationSub: Subject<Location>;

    protected isLoading: Subject<boolean>;
    protected addSuccess: Subject<boolean>;
    protected deleteSuccess: Subject<boolean>;
    protected feedbackMessage: Subject<string>;

    protected formGroup: FormGroup;
    private subscription: Subscription;

    constructor(
        private locationService: LocationService,
        private authenticationService: AuthenticationService,
        private authoritiesService: AuthoritiesService,
        private buildingsService: BuildingService,
        private router: Router,
        private dialog: MatDialog,
        private timeslotsService: TimeslotsService
    ) {
        this.userSub = new BehaviorSubject(authenticationService.userValue());
        this.isLoading = new BehaviorSubject(true);

        this.feedbackMessage = new ReplaySubject();
        this.deleteSuccess = new ReplaySubject();
        this.addSuccess = new ReplaySubject();
        this.locationsSub = new ReplaySubject();
        this.selectLocationSub = new ReplaySubject();
        this.buildingsSub = new ReplaySubject();
        this.authoritiesSub = new ReplaySubject();
        this.timeslotSub = new ReplaySubject();

        this.subscription = new Subscription();
    }

    ngOnInit(): void {
        this.subscription.add(
            this.authenticationService.user.subscribe((user: User) => {
                this.userSub.next(
                    user
                );

                this.setupLocations();
                this.setupBuildings();
                this.setupAuthorities();
            })
        );

        this.setupForm();
    }

    ngOnDestroy(): void {
        this.subscription.unsubscribe();
    }

    setupForm(): void {
        this.formGroup = new FormGroup({
            locationId: new FormControl(0),
            name: new FormControl('', Validators.required),
            authority: new FormControl(0, Validators.required),
            building: new FormControl(0, Validators.required),
            numberOfSeats: new FormControl(0, Validators.required),
            forGroup: new FormControl(false, Validators.required),
            imageUrl: new FormControl(''),
            usesPenaltyPoints: new FormControl(false),
        });
    }

    setupAuthorities(): void {
        this.userSub.pipe(
            mergeMap((user: User) => {
                if (user.isAdmin()) {
                    return this.authoritiesService.getAllAuthorities();
                } else {
                    return this.authoritiesService.getAuthoritiesOfUser(user.userId);
                }
            }), first()
        ).subscribe((authorities: Authority[]) => {
            this.authoritiesSub.next(authorities);
        });
    }

    setupLocations(): void {
        this.userSub.pipe(
            mergeMap((user: User) => {
                if (user.isAdmin()) {
                    return this.locationService.getAllLocations(false).pipe(
                        map((locations: Location[]) => ({ user, locations }))
                    );
                } else {
                    return this.authoritiesService.getLocationsInAuthoritiesOfUser(user.userId).pipe(
                        map((locations: Location[]) => ({ user, locations }))
                    );
                }
            }), first()
        ).subscribe(({user, locations}) => {
            this.locationsSub.next(
                locations.filter((location: Location) =>
                        user.isAdmin() || user.userAuthorities.some((authority: Authority) =>
                            location.authority.authorityId === authority.authorityId
                        )
                )
            );
            this.isLoading.next(false);
        });
    }

    setupBuildings(): void {
        this.buildingsService.getAllBuildings().pipe(first()).subscribe((buildings: Building[]) => {
            this.buildingsSub.next(buildings);
        });
    }

    closeModal(): void {
        this.dialog.closeAll();
    }

    prepareAdd(): void {
        this.dialog.open(this.addLocationModal);
    }

    storeAdd(location: any): void {
        this.addSuccess.next(undefined);

        combineLatest([
            this.authoritiesSub, this.buildingsSub
        ]).pipe(
            map(([authorities, buildings]) =>
                [authorities.find(authority =>
                    authority.authorityId === Number(location.authority)
                ), buildings.find(building =>
                    building.buildingId ===  Number(location.building)
                )]
            )
        ).subscribe(([authority, building]) => {
            location = {
                ...location,
                authority,
                building
            };

            this.locationService.addLocation(location).subscribe(
                () => {
                    this.addSuccess.next(true);
                    this.setupLocations();
                    this.setupForm();
                    this.closeModal();
                },
                (error) => {
                    this.feedbackMessage.next(
                        error.error.message
                    );
                    this.addSuccess.next(false);
                }
            );
        });
    }

    prepareDelete(location: Location): void {
        this.selectLocationSub.next(
            location
        );

        this.timeslotsService.getTimeslotsOfLocation(location.locationId).pipe(
            map(timeslots =>
                timeslots.filter(timeslot =>
                    !timeslot.isInPast()
                )
            ), first()
        ).subscribe((timeslots: Timeslot[]) => {
            this.timeslotSub.next(timeslots);
        });

        this.dialog.open(this.deleteLocationModal);
    }

    storeDelete(location: Location): void {
        this.deleteSuccess.next(undefined);

        this.locationService.deleteLocation(location.locationId).subscribe(
            () => {
                this.deleteSuccess.next(true);
                this.setupLocations();
                this.closeModal();
            },
            (error) => {
                this.feedbackMessage.next(
                    error.error.message
                );
                this.addSuccess.next(false);
            }
        );
    }

    getTableMapper(): TableMapper {
        return (location: Location) => ({
            'management.locations.table.header.name': location.name,
            'management.locations.table.header.authority': location.authority.authorityName,
            'management.locations.table.header.numberOfSeats': location.numberOfSeats
        });
    }

    getTableActions(): TableAction[] {
        return [
            new ListAction((location: Location) => {
                void this.router.navigate(['management/locations/' + location.locationId]);
            }),
            new DeleteAction((location: Location) => {
                this.prepareDelete(location);
            })
        ];
    }
}
