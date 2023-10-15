import {
    Component,
    EventEmitter,
    Input, OnChanges,
    OnDestroy,
    OnInit,
    Output, SimpleChange, SimpleChanges,
    ViewChild
} from '@angular/core';
import * as moment from 'moment';
import {Moment} from 'moment';
import {Timeslot} from 'src/app/model/Timeslot';
import {Location} from 'src/app/model/Location';
import {ModalComponent} from '../../../../../../stad-gent-components/molecules/modal/modal.component';
import {BehaviorSubject, combineLatest, Observable, Subject, Subscription} from 'rxjs';

@Component({
    selector: 'app-location-add-timeslot-dialog',
    templateUrl: './location-add-timeslot-dialog.component.html',
    styleUrls: ['./location-add-timeslot-dialog.component.css']
})
export class LocationAddTimeslotDialogComponent implements OnChanges {

    @ViewChild(ModalComponent) modal: ModalComponent;

    @Input() location: Location;
    @Input() timeslot: Timeslot;

    @Output() onNewTimeslot = new EventEmitter<Timeslot>();
    @Output() onUpdateTimeslot = new EventEmitter<Timeslot>();

    protected displayErrorTime: boolean;
    protected displayErrorSeats: boolean;

    protected newTimeslot: Timeslot = new Timeslot();


    ngOnChanges(changes: SimpleChanges): void {
        if (changes.location || changes.timeslot) {
            this.setupForm(this.timeslot ?? new Timeslot(
                0, null, 0, this.location?.numberOfSeats, false, null, this.location?.locationId
            ));
        }
    }

    setupForm(timeslot: Timeslot = this.timeslot): void {
        this.newTimeslot = timeslot;
    }

    getMinStartDate(): Moment {
        return null;
    }

    getMinReservableFrom(model: Timeslot): Moment {
        if (!model.timeslotDate) {
            return null;
        } else {
            return moment();
        }
    }

    closeModal(): void {
        this.modal.close();
    }

    openModal(): void {
        this.modal.open();
    }

    confirm(): void {
        if (this.newTimeslot.closingHour.isAfter(this.newTimeslot.openingHour)) {
            if (this.newTimeslot.seatCount >= this.newTimeslot.amountOfReservations) {
                this.displayErrorTime = false;
                this.displayErrorSeats = false;

                if (this.isUpdating()) {
                    this.onUpdateTimeslot.next(this.newTimeslot);
                } else {
                    this.onNewTimeslot.next(this.newTimeslot);
                }
            } else {
                this.displayErrorSeats = true;
            }
        } else {
            this.displayErrorTime = true;
        }
    }

    isUpdating(): boolean {
        return !!this.newTimeslot?.timeslotSequenceNumber;
    }
}
