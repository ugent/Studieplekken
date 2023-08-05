import {Component, Input} from '@angular/core';
import {Penalty} from '../../../model/Penalty';
import {BaseManagementComponent} from '../../management/lists/base-management.component';
import {combineLatest, Observable} from 'rxjs';
import {User} from '../../../model/User';
import {DeleteAction, TableAction, TableMapper} from '../../../model/Table';
import {PenaltyList, PenaltyService} from '../../../extensions/services/api/penalties/penalty.service';
import {first, mergeMap, switchMap} from 'rxjs/operators';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {AuthenticationService} from '../../../extensions/services/authentication/authentication.service';

@Component({
    selector: 'app-user-penalty-manager',
    templateUrl: './user-penalty-manager.component.html',
    styleUrls: ['./user-penalty-manager.component.scss']
})
export class UserPenaltyManagerComponent extends BaseManagementComponent<Penalty> {

    @Input() userObs$: Observable<User>;

    constructor(
        private penaltyService: PenaltyService,
        private authenticationService: AuthenticationService
    ) {
        super();
    }

    setupForm(item: Penalty = new Penalty()): void {
        this.formGroup = new FormGroup<any>({
            points: new FormControl(item.points, Validators.required),
            description: new FormControl(item.description, Validators.required)
        });
    }

    setupItems(): void {
        this.userObs$.pipe(
            switchMap(user =>
                this.penaltyService.getPenaltiesOfUserById(user.userId)
            ), first()
        ).subscribe((penaltyList: PenaltyList) =>
            this.itemsSub.next(penaltyList.penalties)
        );
    }

    storeAdd(body: any = this.formGroup.value): void {
        this.sendBackendRequest(
            combineLatest([
                this.userObs$, this.authenticationService.user
            ]).pipe(
                mergeMap(([user, loggedInUser]) => {
                    const penalty = new Penalty();
                    penalty.issuer = loggedInUser;
                    penalty.designee = user;
                    penalty.points = body.points;
                    penalty.description = body.description;
                    return this.penaltyService.addPenalty(penalty);
                })
            )
        );
    }

    storeDelete(item: Penalty): void {
        this.sendBackendRequest(
            this.penaltyService.deletePenalty(item)
        );
    }

    getTableActions(): TableAction[] {
        return [
            new DeleteAction((penalty: Penalty) =>
                this.storeDelete(penalty)
            )
        ];
    }

    getTableMapper(): TableMapper {
        return (penalty: Penalty) => ({
            'profile.penalties.table.header.timestamp': penalty.createdAt.format('DD/MM/YYYY HH:mm'),
            'profile.penalties.table.header.issuer': penalty.issuer ? penalty.issuer.firstName + ' ' + penalty.designee.lastName : '-',
            'profile.penalties.table.header.description': penalty.description ?? '-',
            'profile.penalties.table.header.receivedPoints': penalty.points
        });
    }
}
