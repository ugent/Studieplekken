import {Component, Input, OnChanges, OnInit} from '@angular/core';
import * as moment from 'moment';
import {Moment} from 'moment';
import {Observable} from 'rxjs';

@Component({
    selector: 'app-after-reservation',
    templateUrl: './after-reservation.component.html',
    styleUrls: ['./after-reservation.component.scss']
})
export class AfterReservationComponent implements OnInit, OnChanges {

    @Input() newReservationCreator: Observable<Moment[]>;
    loading: boolean = true;
    failure: boolean = false;
    hasDelayedReservation = true;

    constructor() {
    }

    ngOnInit(): void {
    }

    ngOnChanges(): void {
        if (this.newReservationCreator) {
            this.loading = true;
            this.newReservationCreator.subscribe(m => {
                const now: Moment = moment();
                const maxMoment: Moment = moment.max(m);
                this.hasDelayedReservation = now.isBefore(maxMoment);
                this.loading = false;
            }, (error) => {
                console.log(error);
                this.failure = true;
                this.loading = false;
            });
        }
    }

}
