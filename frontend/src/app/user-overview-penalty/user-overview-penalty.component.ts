import {
  OnInit,
  Component,
  Input, Output, EventEmitter,
} from '@angular/core';
import {IPenalty} from "../../interfaces/IPenalty";
import {PenaltyService} from "../../services/penalty.service";
import {transition, trigger, useAnimation} from "@angular/animations";
import {rowsAnimation} from "../animations";
import {BehaviorSubject, Observable} from "rxjs";
import {IDate} from "../../interfaces/IDate";
import {IPair} from "../../interfaces/IPair";

@Component({
  selector: 'app-user-overview-penalty',
  templateUrl: './user-overview-penalty.component.html',
  styleUrls: ['./user-overview-penalty.component.css'],
  animations: [trigger('rowsAnimation', [
    transition('void => *', [
      useAnimation(rowsAnimation)
    ])
  ])]
})
export class UserOverviewPenaltyComponent implements OnInit {
  @Input() display: Observable<string>;
  @Input() augentID: string;
  @Output() pointsToBeCalculatedEvent = new EventEmitter<boolean>();

  Object = Object;

  penalties: IPenalty[];
  penaltiesCopy: IPenalty[];
  selectedPenalty: IPenalty;

  displayThis = 'none';
  displayDelete = 'none';
  displayAdd = new BehaviorSubject<string>('none');

  cacheForPoints : Map<number, number>;

  constructor(private penaltyService: PenaltyService) {
    this.cacheForPoints = new Map<number, number>();
  }

  ngOnInit(): void {
    this.display.subscribe(d => {
      this.displayThis = d;
      if (d !== 'none' && this.augentID !== undefined && this.augentID.length !== 0) {
        this.penaltyService.getPenalties(this.augentID).subscribe(n => {
          this.penalties = n;
          this.penaltiesCopy = JSON.parse(JSON.stringify(n)); // deep copy
        });
      }
    });
  }

  persistPenaltyChanges(): void {
    let pair: IPair = {
      first: this.penaltiesCopy,
      second: this.penalties
    };

    this.penaltyService.updatePenalties(this.augentID, pair).subscribe(n => {});
    this.displayThis = 'none';
    this.pointsToBeCalculatedEvent.emit(true);
  }

  cancelPenaltyChanges(): void {
    this.pointsToBeCalculatedEvent.emit(false);
  }

  deletePenalty(penalty: IPenalty): void {
    let idx = this.penalties.findIndex(v => this.equalPenalties(v, penalty));
    this.penalties.splice(idx, 1);
  }

  addNewPenalty(penalty: IPenalty): void {
    if (penalty !== null) {
      this.penalties.push(penalty);
      if(!this.cacheForPoints.has(penalty.eventCode)){
        this.penaltyService.getPenaltyEvent(penalty.eventCode).subscribe(value => {
          this.cacheForPoints.set(penalty.eventCode, value.points);
        })
      }
    }
    this.displayAdd.next('none');
  }

  equalPenalties(p1: IPenalty, p2: IPenalty): boolean {
    return  p1.augentID === p2.augentID &&
      p1.eventCode === p2.eventCode &&
      this.equalDates(p1.timestamp, p2.timestamp) &&
      this.equalDates(p1.reservationDate, p2.reservationDate) &&
      p1.reservationLocation === p2.reservationLocation &&
      p1.receivedPoints === p2.receivedPoints;
  }

  equalDates(d1: IDate, d2: IDate): boolean {
    return d1.year === d2.year &&
      d1.month === d2.month &&
      d1.day === d2.day &&
      d1.hrs === d2.hrs &&
      d1.min === d2.min &&
      d1.sec === d2.sec;
  }
}
