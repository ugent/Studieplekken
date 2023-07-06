import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { PenaltyList, PenaltyService } from 'src/app/extensions/services/api/penalties/penalty.service';
import { Penalty } from '../../../extensions/model/Penalty';

@Component({
  selector: 'app-penalty-table',
  templateUrl: './penalty-table.component.html',
  styleUrls: ['./penalty-table.component.scss']
})
export class PenaltyTableComponent {

  @Input() penalties: PenaltyList;
  @Input() showDesignee = false;
  @Input() showDelete = false;
  @Input() showTitle = false;

  @Output() onDelete: EventEmitter<Penalty> = new EventEmitter();

  constructor(private penaltyService: PenaltyService) {
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

}
