import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import * as moment from 'moment';
import {Moment} from 'moment';
import {AuthenticationService} from 'src/app/extensions/services/authentication/authentication.service';
import {Timeslot} from 'src/app/model/Timeslot';
import {Location} from 'src/app/model/Location';
import {MatDialog} from '@angular/material/dialog';

@Component({
    selector: 'app-location-add-timeslot-dialog',
    templateUrl: './location-add-timeslot-dialog.component.html',
    styleUrls: ['./location-add-timeslot-dialog.component.css']
})
export class LocationAddTimeslotDialogComponent implements OnInit, OnChanges {
    public model: Timeslot = new Timeslot(null, null, null, null, true, null, null, null, null, null, false);
    @Output() onNewTimeslot: EventEmitter<Timeslot> = new EventEmitter();
    @Output() onUpdateTimeslot: EventEmitter<Timeslot> = new EventEmitter();
    @Input() location: Location;
    @Input() timeslot: Timeslot;

    displayErrorTime = false;
    displayErrorSeats = false;

    constructor(private authenticationService: AuthenticationService, private modalService: MatDialog) {
    }


    ngOnInit(): void {
    }

    ngOnChanges(changes: SimpleChanges): void {
        const location: Location = changes.location.currentValue;
        this.model.locationId = location.locationId;
        this.model.seatCount = location.numberOfSeats;

        if (changes.timeslot) {
            const oldTimeslot: Timeslot = changes.timeslot.currentValue;
            this.model.locationId = oldTimeslot.locationId;
            this.model.reservable = oldTimeslot.reservable;
            this.model.closingHour = oldTimeslot.closingHour;
            this.model.openingHour = oldTimeslot.openingHour;
            this.model.timeslotDate = oldTimeslot.timeslotDate;
            this.model.timeslotSequenceNumber = oldTimeslot.timeslotSequenceNumber;
            this.model.reservableFrom = oldTimeslot.reservableFrom;
            this.model.seatCount = oldTimeslot.seatCount;
            this.model.amountOfReservations = oldTimeslot.amountOfReservations;
        }
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

    getTimeslotTimeInMinutes(model) {
        if (!model.closingTime) {
            return null;
        }

        return model.openingTime?.diff(model.closingTime, 'minutes');
    }

    closeModal(): void {
        this.modalService.closeAll();
    }

    confirm(): void {
        if (this.model.closingHour.isAfter(this.model.openingHour)) {
            if (this.model.seatCount >= this.model.amountOfReservations) {
                this.displayErrorTime = false;
                this.displayErrorSeats = false;
                if (this.isUpdating()) {
                    this.onUpdateTimeslot.next(this.model);
                } else {
                    this.onNewTimeslot.next(this.model);
                }
            } else {
                this.displayErrorSeats = true;
            }
        } else {
            this.displayErrorTime = true;
        }
    }

    isUpdating(): boolean {
        return this.model.timeslotSequenceNumber != null;
    }

}
