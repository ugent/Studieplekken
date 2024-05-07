import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {PenaltyList, PenaltyService} from 'src/app/services/api/penalties/penalty.service';
import {Penalty} from '../../../model/Penalty';
import {TableComponent} from '../../../contracts/table.component.interface';
import {DeleteAction, TableAction, TableMapper} from '../../../model/Table';
import {TranslateService} from '@ngx-translate/core';

@Component({
    selector: 'app-penalty-table',
    templateUrl: './penalty-table.component.html',
    styleUrls: ['./penalty-table.component.scss']
})
export class PenaltyTableComponent implements TableComponent<Penalty> {

    @Input() penalties: PenaltyList;
    @Input() showDesignee = false;
    @Input() showDelete = false;
    @Input() showTitle = false;

    @Output() onDelete: EventEmitter<Penalty> = new EventEmitter();

    constructor(
        private penaltyService: PenaltyService,
        private translateService: TranslateService
    ) {
    }

    getIssuedBy(penalty: Penalty): string {
        if (!penalty.issuer) {
            return 'profile.penalties.table.system';
        } else {
            return penalty.issuer.firstName + ' ' + penalty.issuer.lastName;
        }
    }

    getPenaltyDescription(penalty: Penalty): string {
        if (penalty.penaltyClass === 'custom') {
            return penalty.description;
        }

        return penalty.penaltyClass;
    }

    getDesignee(penalty: Penalty): string {
        return penalty.designee.firstName + ' ' + penalty.designee.lastName;
    }

    delete(penalty: Penalty): void {
        this.penaltyService.deletePenalty(penalty).subscribe(
            () => this.onDelete.emit(penalty)
        );
    }

    getTableActions(): TableAction<Penalty>[] {
        return [
            new DeleteAction((penalty: Penalty) =>
                this.delete(penalty), () => this.showDelete
            )
        ];
    }

    getTableMapper(): TableMapper<Penalty> {
        return (penalty: Penalty) => ({
            'profile.penalties.table.header.timestamp': penalty.createdAt.format('DD/MM/YYYY HH:mm'),
            'profile.penalties.table.header.issuer': this.translateService.stream(this.getIssuedBy(penalty)),
            'profile.penalties.table.header.description': this.translateService.stream(this.getPenaltyDescription(penalty)),
            'profile.penalties.table.header.receivedPoints': penalty.points
        });
    }
}
