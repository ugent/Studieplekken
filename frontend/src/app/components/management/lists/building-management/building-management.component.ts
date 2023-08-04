import {Component} from '@angular/core';
import {
    FormControl, FormGroup,
    Validators
} from '@angular/forms';
import {MatDialog} from '@angular/material/dialog';
import {combineLatest, Observable, ReplaySubject, Subject, throwError} from 'rxjs';
import {AddressResolverService} from 'src/app/extensions/services/addressresolver/nomenatim/addressresolver.service';
import {BuildingService} from 'src/app/extensions/services/api/buildings/buildings.service';
import {AuthenticationService} from 'src/app/extensions/services/authentication/authentication.service';
import {Building, BuildingConstructor} from 'src/app/model/Building';
import {User} from '../../../../model/User';
import {first, map, mergeMap, tap} from 'rxjs/operators';
import {TableMapper} from '../../../../model/Table';
import {BaseManagementComponent} from '../base-management.component';

@Component({
    selector: 'app-building-management',
    templateUrl: './building-management.component.html',
    styleUrls: ['./building-management.component.scss'],
})
export class BuildingManagementComponent extends BaseManagementComponent<Building> {

    protected userSub: Subject<User>;
    protected institutionsSub: Subject<string[]>;

    protected isCorrectAddress: Subject<boolean>;

    constructor(
        private authenticationService: AuthenticationService,
        private buildingService: BuildingService,
        private addressResolver: AddressResolverService
    ) {
        super();

        this.institutionsSub = new ReplaySubject();
        this.userSub = new ReplaySubject();
        this.isCorrectAddress = new ReplaySubject();
    }

    ngOnInit(): void {
        super.ngOnInit();

        this.subscription.add(
            this.authenticationService.user.subscribe(user => {
                this.userSub.next(user);

                this.setupInstitutions();
                this.setupItems();
            })
        );
    }

    setupForm(building: Building = BuildingConstructor.new()): void {
        this.formGroup = new FormGroup({
            name: new FormControl(building.name, Validators.required),
            address: new FormControl(building.address, Validators.required),
            institution: new FormControl(building.institution, Validators.required),
            latitude: new FormControl(building.latitude),
            longitude: new FormControl(building.longitude)
        });
    }

    setupInstitutions(): void {
        this.userSub.pipe(first()).subscribe((user: User) => {
            if (user.isAdmin()) {
                // Todo: we should not hardcode institutions.
                this.institutionsSub.next([
                    'UGent', 'HoGent', 'Arteveldehogeschool', 'StadGent', 'Luca', 'Odisee', 'Other'
                ]);
            } else {
                this.institutionsSub.next([
                    user.institution
                ]);
            }
        });
    }

    setupItems(): void {
        combineLatest([
            this.userSub, this.buildingService.getAllBuildings()
        ]).pipe(first()).subscribe(([user, buildings]) => {
            this.itemsSub.next(
                buildings.filter((building: Building) =>
                    (building.institution === user.institution) || user.isAdmin()
                )
            );
        });
    }

    storeAdd(body = this.formGroup.value): void {
        this.sendBackendRequest(
            this.checkAddress(body.address).pipe(
                mergeMap((coordinates) => {
                    if (coordinates) {
                        const newBuilding = {
                            ...body,
                            ...coordinates
                        };

                        return this.buildingService.addBuilding(
                            newBuilding
                        );
                    }
                    return throwError({
                        message: 'The address is incorrect'
                    });
                })
            )
        );
    }

    storeUpdate(building: Building, body = this.formGroup.value): void {
        this.sendBackendRequest(
            this.checkAddress(building.address).pipe(
                mergeMap((coordinates) => {
                    if (coordinates) {
                        const updatedBuilding = {
                            ...building,
                            ...body,
                            ...coordinates
                        };

                        return this.buildingService.updateBuilding(
                            building.buildingId, updatedBuilding
                        );
                    }
                    return throwError({
                       message: 'The address is incorrect'
                    });
                })
            )
        );
    }

    storeDelete(building: Building): void {
        this.sendBackendRequest(
            this.buildingService.deleteBuilding(building.buildingId)
        );
    }

    checkAddress(address: string): Observable<{latitude: string, longitude: string}> {
        return this.addressResolver.query(address).pipe(
            tap(result => console.log(result)),
            map(result =>
                result.length ? {
                    latitude: result[0].lat,
                    longitude: result[0].lon
                } : null
            )
        );
    }

    getTableMapper(): TableMapper {
        return (building: Building) => ({
            'management.buildings.table.name': building.name,
            'management.buildings.table.address': building.address,
            'management.buildings.table.institution': building.institution
        });
    }
}
