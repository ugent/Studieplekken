import {Component, EventEmitter, Input, Output} from '@angular/core';
import {Penalty} from '@/model/Penalty';
import {BaseManagementComponent} from '../../management//base-management.component';
import {User} from '@/model/User';
import {DeleteAction, TableAction, TableMapper} from '@/model/Table';
import {PenaltyList, PenaltyService} from '@/services/api/penalties/penalty.service';
import {FormControl, FormGroup, Validators} from '@angular/forms';

@Component({
    selector: 'app-user-penalty-manager',
    templateUrl: './user-penalty-manager.component.html',
    styleUrls: ['./user-penalty-manager.component.scss']
})
export class UserPenaltyManagerComponent extends BaseManagementComponent<Penalty> {

    @Input() currentUser: User;
    @Input() loggedInUser: User;
    @Input() penalties: PenaltyList;

    @Output() updatedPenalties = new EventEmitter<void>();

    constructor(
        private penaltyService: PenaltyService
    ) {
        super();
    }

    ngOnInit(): void {
        super.ngOnInit();

        this.refresh$.subscribe(() =>
            this.updatedPenalties.emit()
        );
    }

    setupForm(item: Penalty = new Penalty()): void {
        this.formGroup = new FormGroup<any>({
            points: new FormControl(item.points, Validators.required),
            description: new FormControl(item.description, Validators.required)
        });
    }

    storeAdd(body: any = this.formGroup.value): void {
        const penalty = new Penalty();
        penalty.designee = this.currentUser;
        penalty.points = body.points;
        penalty.description = body.description;
        this.sendBackendRequest(
            this.penaltyService.addPenalty(penalty)
        );
    }

    storeDelete(item: Penalty): void {
        this.sendBackendRequest(
            this.penaltyService.deletePenalty(item)
        );
    }

    getTableActions(): TableAction<Penalty>[] {
        return [
            new DeleteAction((penalty: Penalty) =>
                this.storeDelete(penalty)
            )
        ];
    }

    getTableMapper(): TableMapper<Penalty> {
        return (penalty: Penalty) => ({
            'profile.penalties.table.header.timestamp': penalty.createdAt.format('DD/MM/YYYY HH:mm'),
            'profile.penalties.table.header.issuer': penalty.issuer ? penalty.issuer.firstName + ' ' + penalty.issuer.lastName : '-',
            'profile.penalties.table.header.description': penalty.description ?? '-',
            'profile.penalties.table.header.receivedPoints': penalty.points
        });
    }
}
