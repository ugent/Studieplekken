import {
    Component,
    EventEmitter,
    Input,
    OnDestroy,
    OnInit,
    Output,
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
export class LocationAddTimeslotDialogComponent implements OnInit, OnDestroy {

    @ViewChild(ModalComponent) modal: ModalComponent;

    @Input() location: Observable<Location>;
    @Input() timeslot: Observable<Timeslot>;
    @Output() onNewTimeslot: EventEmitter<Timeslot> = new EventEmitter();
    @Output() onUpdateTimeslot: EventEmitter<Timeslot> = new EventEmitter();

    protected displayErrorTime: Subject<boolean>;
    protected displayErrorSeats: Subject<boolean>;

    protected newTimeslot: Timeslot = new Timeslot();
    protected subscription: Subscription;

    constructor() {
        this.displayErrorTime = new BehaviorSubject(false);
        this.displayErrorSeats = new BehaviorSubject(false);
        this.subscription = new Subscription();
    }

    ngOnInit(): void {
        this.subscription.add(
            combineLatest([
                this.location, this.timeslot
            ]).subscribe(([location, timeslot]) => {
               this.setupForm(timeslot ?? new Timeslot(
               0, null, 0, location.numberOfSeats, false, null, location.locationId
               ));
            })
        );
    }

    setupForm(timeslot: Timeslot): void {
        this.newTimeslot = timeslot;
    }

    ngOnDestroy(): void {
        this.subscription.unsubscribe();
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
                this.displayErrorTime.next(false);
                this.displayErrorSeats.next(false);

                if (this.isUpdating()) {
                    this.onUpdateTimeslot.next(this.newTimeslot);
                } else {
                    this.onNewTimeslot.next(this.newTimeslot);
                }
            } else {
                this.displayErrorSeats.next(true);
            }
        } else {
            this.displayErrorTime.next(true);
        }
    }

    isUpdating(): boolean {
        return !!this.newTimeslot?.timeslotSequenceNumber;
    }
}
