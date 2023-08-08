import {Component, ViewChild} from '@angular/core';
import {combineLatest, concat, EMPTY, Observable} from 'rxjs';
import {User} from '../../../../model/User';
import {Building} from '../../../../model/Building';
import {Location} from '../../../../model/Location';
import {LocationService} from '../../../../extensions/services/api/locations/location.service';
import {AuthenticationService} from '../../../../extensions/services/authentication/authentication.service';
import {AuthoritiesService} from '../../../../extensions/services/api/authorities/authorities.service';
import {BuildingService} from '../../../../extensions/services/api/buildings/buildings.service';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {concatAll, filter, map, mergeMap, share, startWith, switchMap} from 'rxjs/operators';
import {Authority} from '../../../../model/Authority';
import {DeleteAction, ListAction, TableAction, TableMapper} from '../../../../model/Table';
import {Router} from '@angular/router';
import {Timeslot} from '../../../../model/Timeslot';
import {BaseManagementComponent} from '../base-management.component';
import {ModalComponent} from '../../../stad-gent-components/molecules/modal/modal.component';
import {TimeslotsService} from '../../../../extensions/services/api/calendar-periods/timeslot.service';
import {of} from 'rxjs/internal/observable/of';

@Component({
    selector: 'app-locations-management',
    templateUrl: './locations-management.component.html',
    styleUrls: ['./locations-management.component.scss']
})
export class LocationsManagementComponent extends BaseManagementComponent<Location> {

    @ViewChild('volunteersModal') volunteersModal: ModalComponent;

    protected userObs$: Observable<User>;
    protected locationsObs$: Observable<Location[]>;
    protected authoritiesObs$: Observable<Authority[]>;
    protected buildingsObs$: Observable<Building[]>;
    protected volunteersObs$: Observable<User[]>;
    protected timeslotObs$: Observable<Timeslot[]>;

    constructor(
        private locationService: LocationService,
        private authenticationService: AuthenticationService,
        private authoritiesService: AuthoritiesService,
        private buildingsService: BuildingService,
        private timeslotService: TimeslotsService,
        private router: Router
    ) {
        super();
    }

    ngOnInit(): void {
        super.ngOnInit();

        this.userObs$ = this.authenticationService.getUserObs();

        this.buildingsObs$ = this.buildingsService.getAllBuildings();

        this.authoritiesObs$ = this.userObs$.pipe(
            switchMap((user: User) => {
                if (user.isAdmin()) {
                    return this.authoritiesService.getAllAuthorities();
                } else {
                    return this.authoritiesService.getAuthoritiesOfUser(user.userId);
                }
            })
        );

        this.volunteersObs$ = this.selectedSub$.pipe(
            filter(selected => !!selected), switchMap(location =>
                this.locationService.getVolunteers(location.locationId)
            )
        );

        this.timeslotObs$ = this.selectedSub$.pipe(
            filter(selected => !!selected), switchMap(location =>
                this.timeslotService.getTimeslotsOfLocation(location.locationId)
            )
        );

        this.locationsObs$ = combineLatest([
            this.userObs$, this.refresh$.pipe(startWith(EMPTY))
        ]).pipe(
            switchMap(([user]) =>
                user.isAdmin() ?
                    this.locationService.getAllLocations() :
                    this.authoritiesService.getLocationsInAuthoritiesOfUser(user.userId)
            )
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

    storeAdd(authorities: Authority[], buildings: Building[], location: any): void {
        const authority = authorities.find(a =>
            a.authorityId === Number(location.authority)
        );
        const building = buildings.find(b =>
            b.buildingId ===  Number(location.building)
        );
        this.sendBackendRequest(
            this.locationService.addLocation({
                ...location,
                authority,
                building
            })
        );
    }

    storeDelete(location: Location): void {
        this.sendBackendRequest(
            this.locationService.deleteLocation(location.locationId)
        );
    }

    showVolunteers(location: Location): void {
        this.selectedSub$.next(location);
        this.volunteersModal.open();
    }

    getTableMapper(): TableMapper<Location> {
        return (location: Location) => ({
            'management.locations.table.header.name': location.name,
            'management.locations.table.header.authority': location.authority.authorityName,
            'management.locations.table.header.numberOfSeats': location.numberOfSeats
        });
    }

    getTableActions(): TableAction<Location>[] {
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
