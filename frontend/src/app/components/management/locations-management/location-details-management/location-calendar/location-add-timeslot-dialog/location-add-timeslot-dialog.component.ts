import {
    Component,
    EventEmitter,
    Input, OnChanges,
    Output, SimpleChanges,
    ViewChild
} from '@angular/core';
import * as moment from 'moment';
import {Moment} from 'moment';
import {Timeslot} from 'src/app/model/Timeslot';
import {Location} from 'src/app/model/Location';
import {ModalComponent} from '@/components/stad-gent-components/molecules/modal/modal.component';

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

    protected _displayErrorTime: boolean;
    protected _displayErrorSeats: boolean;

    protected _timeslot: Timeslot = new Timeslot();

    ngOnChanges(changes: SimpleChanges): void {
        if (changes.location || changes.timeslot) {
            // We take a copy of the input timeslot to prevent updating the prop!
            const input = this.timeslot;

            const timeslot = new Timeslot(
                input?.timeslotSequenceNumber ?? 0,
                input?.timeslotDate ?? null,
                input?.amountOfReservations ?? 0,
                input?.seatCount ?? this.location.numberOfSeats,
                input?.reservable ?? false,
                input?.reservableFrom ?? null,
                this.location?.locationId,
                input?.openingHour ?? null,
                input?.closingHour ?? null,
                input?.timeslotGroup ?? null,
                input?.repeatable ?? false
            );

            this.setupForm(timeslot);
        }
    }

    setupForm(timeslot: Timeslot = this.timeslot): void {
        console.log(timeslot);
        this._timeslot = timeslot;
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
        const timeslot = this._timeslot;

        if (timeslot.closingHour.isAfter(timeslot.openingHour)) {
            if (timeslot.seatCount >= timeslot.amountOfReservations) {
                this._displayErrorTime = false;
                this._displayErrorSeats = false;

                if (this.isUpdating()) {
                    this.onUpdateTimeslot.next(timeslot);
                } else {
                    this.onNewTimeslot.next(timeslot);
                }
            } else {
                this._displayErrorSeats = true;
            }
        } else {
            this._displayErrorTime = true;
        }
    }

    isUpdating(): boolean {
        return !!this._timeslot?.timeslotSequenceNumber;
    }
}
