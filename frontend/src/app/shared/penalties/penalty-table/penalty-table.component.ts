import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { PenaltyList, PenaltyService } from 'src/app/services/api/penalties/penalty.service';
import { Penalty } from '../../model/Penalty';

@Component({
  selector: 'app-penalty-table',
  templateUrl: './penalty-table.component.html',
  styleUrls: ['./penalty-table.component.scss']
})
export class PenaltyTableComponent implements OnInit {

  @Input() penalties: PenaltyList;
  @Input() showDesignee = false;
  @Input() showDelete = false;
  @Input() showTitle = false;

  @Output() onDelete: EventEmitter<Penalty> = new EventEmitter();

  constructor(private penaltyService: PenaltyService) {
  }

  ngOnInit(): void {
    console.log('here', this.penalties);
  }

  getIssuedBy(penalty: Penalty) {
    if (!penalty.issuer) {
      return 'profile.penalties.table.system';
    } else {
      return penalty.issuer.firstName + ' ' + penalty.issuer.lastName;
    }
  }

  getPenaltyDescription(penalty: Penalty) {
    if (penalty.penaltyClass === 'custom') {
      return penalty.description;
    }

    return penalty.penaltyClass;
  }

  getDesignee(penalty: Penalty) {
    return penalty.designee.firstName + ' ' + penalty.designee.lastName;
  }

  delete(penalty: Penalty) {
    this.penaltyService.deletePenalty(penalty).subscribe(
      () => this.onDelete.emit(penalty)
    )
  }

}
