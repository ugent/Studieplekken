import {Component} from '@angular/core';
import {
    FormControl, FormGroup,
    Validators
} from '@angular/forms';
import {combineLatest, EMPTY, Observable, throwError} from 'rxjs';
import {AddressResolverService} from 'src/app/extensions/services/addressresolver/nomenatim/addressresolver.service';
import {BuildingService} from 'src/app/extensions/services/api/buildings/buildings.service';
import {AuthenticationService} from 'src/app/extensions/services/authentication/authentication.service';
import {Building, BuildingConstructor} from 'src/app/model/Building';
import {User} from '../../../../model/User';
import {concatMap, map, mergeMap, startWith, switchMap, tap} from 'rxjs/operators';
import {TableMapper} from '../../../../model/Table';
import {BaseManagementComponent} from '../base-management.component';
import {institutions} from '../../../../app.constants';

@Component({
    selector: 'app-building-management',
    templateUrl: './building-management.component.html',
    styleUrls: ['./building-management.component.scss'],
})
export class BuildingManagementComponent extends BaseManagementComponent<Building> {

    protected contextObs$: Observable<any>;

    protected user: User;
    protected buildings: Building[];
    protected institutions: string[];

    constructor(
        private authenticationService: AuthenticationService,
        private buildingService: BuildingService,
        private addressResolver: AddressResolverService
    ) {
        super();
    }

    ngOnInit(): void {
        super.ngOnInit();

        this.contextObs$ = this.refresh$.pipe(
            startWith(EMPTY),
            switchMap(() =>
                combineLatest([
                    this.authenticationService.getUserObs(),
                    this.buildingService.getAllBuildings()
                ])
            )
        ).pipe(
            tap(([user, buildings]) => {
                this.user = user;
                this.buildings = buildings.filter((building: Building) =>
                    building.institution === user.institution || user.isAdmin()
                );
                this.institutions = user.isAdmin() ? institutions : [user.institution];
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

    storeAdd(body = this.formGroup.value): void {
        this.sendBackendRequest(
            this.checkAddress(body.address).pipe(
                switchMap((coordinates) => {
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
            map(result =>
                result.length ? {
                    latitude: result[0].lat,
                    longitude: result[0].lon
                } : null
            )
        );
    }

    getTableMapper(): TableMapper<Building> {
        return (building: Building) => ({
            'management.buildings.table.name': building.name,
            'management.buildings.table.address': building.address,
            'management.buildings.table.institution': building.institution
        });
    }
}
