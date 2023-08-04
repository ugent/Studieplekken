import {ChangeDetectionStrategy, Component, ViewChild} from '@angular/core';
import {combineLatest, ReplaySubject, Subject} from 'rxjs';
import {User} from '../../../../model/User';
import {Building} from '../../../../model/Building';
import {Location} from '../../../../model/Location';
import {LocationService} from '../../../../extensions/services/api/locations/location.service';
import {AuthenticationService} from '../../../../extensions/services/authentication/authentication.service';
import {AuthoritiesService} from '../../../../extensions/services/api/authorities/authorities.service';
import {BuildingService} from '../../../../extensions/services/api/buildings/buildings.service';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {filter, first, map, mergeMap, tap} from 'rxjs/operators';
import {Authority} from '../../../../model/Authority';
import {DeleteAction, ListAction, TableAction, TableMapper} from '../../../../model/Table';
import {Router} from '@angular/router';
import {Timeslot} from '../../../../model/Timeslot';
import {BaseManagementComponent} from '../base-management.component';
import {ModalComponent} from '../../../stad-gent-components/molecules/modal/modal.component';

@Component({
    selector: 'app-locations-management',
    templateUrl: './locations-management.component.html',
    styleUrls: ['./locations-management.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class LocationsManagementComponent extends BaseManagementComponent<Location> {

    @ViewChild('volunteersModal') volunteersModal: ModalComponent;

    protected userSub: Subject<User>;
    protected authoritiesSub: Subject<Authority[]>;
    protected buildingsSub: Subject<Building[]>;
    protected volunteersSub: Subject<User[]>;
    protected timeslotSub: Subject<Timeslot[]>;

    constructor(
        private locationService: LocationService,
        private authenticationService: AuthenticationService,
        private authoritiesService: AuthoritiesService,
        private buildingsService: BuildingService,
        private router: Router
    ) {
        super();

        this.userSub = new ReplaySubject();
        this.authoritiesSub = new ReplaySubject();
        this.buildingsSub = new ReplaySubject();
        this.volunteersSub = new ReplaySubject();
        this.timeslotSub = new ReplaySubject();
    }

    ngOnInit(): void {
        super.ngOnInit();

        this.subscription.add(
            this.authenticationService.user.subscribe((user: User) => {
                this.userSub.next(
                    user
                );

                this.setupItems();
                this.setupBuildings();
                this.setupAuthorities();
            })
        );
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

    setupItems(): void {
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
            this.itemsSub.next(
                locations.filter((location: Location) =>
                    user.isAdmin() || user.userAuthorities.some((authority: Authority) =>
                        location.authority.authorityId === authority.authorityId
                    )
                )
            );
        });
    }

    setupVolunteers(location: Location): void {
        this.volunteersSub.next();
        this.selectedSub.next(location);
        this.locationService.getVolunteers(location.locationId).subscribe((volunteers: User[]) => {
            this.volunteersSub.next(volunteers);
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

    setupBuildings(): void {
        this.buildingsService.getAllBuildings().pipe(first()).subscribe((buildings: Building[]) => {
            this.buildingsSub.next(buildings);
        });
    }

    storeAdd(location: any): void {
        this.sendBackendRequest(
            combineLatest([
                this.authoritiesSub, this.buildingsSub
            ]).pipe(
                map(([authorities, buildings]) =>
                    [authorities.find(authority =>
                        authority.authorityId === Number(location.authority)
                    ), buildings.find(building =>
                        building.buildingId ===  Number(location.building)
                    )]
                ),
                mergeMap(([authority, building]) => {
                    return this.locationService.addLocation({
                        ...location,
                        authority,
                        building
                    });
                })
            )
        );
    }

    storeDelete(location: Location): void {
        this.sendBackendRequest(
            this.locationService.deleteLocation(location.locationId)
        );
    }

    showVolunteers(location: Location): void {
        this.setupVolunteers(location);
        this.volunteersModal.open();
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
            new TableAction('icon-user', (location: Location) => {
                this.showVolunteers(location);
            }),
            new ListAction((location: Location) => {
                void this.router.navigate(['management/locations/' + location.locationId]);
            }),
            new DeleteAction((location: Location) => {
                this.prepareDelete(location);
            })
        ];
    }
}
