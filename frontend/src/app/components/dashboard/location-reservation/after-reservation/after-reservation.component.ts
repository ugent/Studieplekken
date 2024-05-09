import {Component, Input, OnChanges, ViewChild} from '@angular/core';
import * as moment from 'moment';
import {Moment} from 'moment';
import {Observable} from 'rxjs';
import {ModalComponent} from '../../../stad-gent-components/molecules/modal/modal.component';

@Component({
    selector: 'app-after-reservation',
    templateUrl: './after-reservation.component.html',
    styleUrls: ['./after-reservation.component.scss']
})
export class AfterReservationComponent implements OnChanges {

    @ViewChild('modal') modal: ModalComponent;

    @Input() newReservationCreator: Observable<Moment[]>;

    protected loading = true;
    protected failure = false;
    protected hasDelayedReservation = true;

    constructor() {
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

    open(): void {
        this.modal.open();
    }
}
