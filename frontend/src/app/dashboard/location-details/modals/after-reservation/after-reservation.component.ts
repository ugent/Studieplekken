import { Component, Input, OnChanges, OnInit } from '@angular/core';
import * as moment from 'moment';
import { Moment } from 'moment';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-after-reservation',
  templateUrl: './after-reservation.component.html',
  styleUrls: ['./after-reservation.component.scss']
})
export class AfterReservationComponent implements OnInit, OnChanges {

  @Input() newReservationCreator: Observable<Moment[]>;
  loading: boolean = true;
  hasDelayedReservation = true;

  constructor() { }

  ngOnInit(): void {
  }

  ngOnChanges() {
    if (this.newReservationCreator) {
      this.loading = true;
      this.newReservationCreator.subscribe(m => {
        console.log(m);
        const now : Moment = moment();
        const maxMoment : Moment = moment.max(m);
        this.hasDelayedReservation = now.isBefore(maxMoment);
        this.loading = false;
      })
    }
  }

}
