import {Component, OnDestroy, OnInit, TemplateRef} from '@angular/core';
import {
    AbstractControl, FormControl, FormGroup,
    Validators
} from '@angular/forms';
import {MatDialog} from '@angular/material/dialog';
import {BehaviorSubject, combineLatest, ReplaySubject, Subject, Subscription} from 'rxjs';
import {of} from 'rxjs/internal/observable/of';
import {map} from 'rxjs/internal/operators/map';
import {AddressResolverService} from 'src/app/extensions/services/addressresolver/nomenatim/addressresolver.service';
import {BuildingService} from 'src/app/extensions/services/api/buildings/buildings.service';
import {AuthenticationService} from 'src/app/extensions/services/authentication/authentication.service';
import {Building} from 'src/app/extensions/model/Building';
import {User} from '../../../extensions/model/User';
import {first, take} from 'rxjs/operators';
import {DeleteAction, EditAction, ListAction, TableAction, TableMapper} from '../../../extensions/model/Table';
import {Location} from '../../../extensions/model/Location';

@Component({
    selector: 'app-building-management',
    templateUrl: './building-management.component.html',
    styleUrls: ['./building-management.component.scss'],
})
export class BuildingManagementComponent implements OnInit, OnDestroy {
    protected userSub: Subject<User>;
    protected buildingsSub: Subject<Building[]>;
    protected institutionsSub: Subject<string[]>;

    protected isLoading: Subject<boolean>;
    protected addSuccess: Subject<boolean>;
    protected deleteSuccess: Subject<boolean>;
    protected feedbackMessage: Subject<string>;

    protected subscription: Subscription;

    constructor(
        private buildingService: BuildingService,
        private modalService: MatDialog,
        private authenticationService: AuthenticationService,
        private addressResolver: AddressResolverService
    ) {
        this.userSub = new BehaviorSubject(
            authenticationService.userValue()
        );

        this.buildingsSub = new ReplaySubject();
        this.institutionsSub = new ReplaySubject();

        this.subscription = new Subscription();
    }

    ngOnInit(): void {
        this.subscription.add(
            this.authenticationService.user.subscribe(user => {
                this.userSub.next(user);

                this.reloadBuildings();
                this.fillInstitutions();
            })
        );
    }

    ngOnDestroy(): void {
        this.subscription.unsubscribe();
    }

    fillInstitutions(): void {
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

    reloadBuildings(): void {
        combineLatest([
            this.userSub, this.buildingService.getAllBuildings()
        ]).pipe(first()).subscribe(([user, buildings]) => {
            this.buildingsSub.next(
                buildings.filter((building: Building) =>
                    (building.institution === user.institution) || user.isAdmin()
                )
            );
        });
    }

    closeModal(): void {
        this.modalService.closeAll();
    }

    getTableMapper(): TableMapper {
        return (building: Building) => ({
            'management.buildings.table.name': building.name,
            'management.buildings.table.address': building.address,
            'management.buildings.table.institution': building.institution
        });
    }

    getTableActions(): TableAction[] {
        return [
            new EditAction((building: Building) => {
                alert('edit moment');
            }),
            new DeleteAction((location: Location) => {
                alert('delete moment');
            })
        ];
    }
}
