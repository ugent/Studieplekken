import {
    Component,
    EventEmitter,
    Input, OnChanges,
    Output, SimpleChanges,
    ViewChild
} from '@angular/core';
import * as moment from 'moment';
import {Moment, now} from 'moment';
import {Timeslot} from '@/model/Timeslot';
import {Location} from '@/model/Location';
import {ModalComponent} from '@/components/stad-gent-components/molecules/modal/modal.component';
import { FormBuilder, FormGroup } from '@angular/forms';
import { SEAT_COUNT_THRESHOLD } from '@/app.constants';

@Component({
    selector: 'app-location-add-timeslot-dialog',
    templateUrl: './location-add-timeslot-dialog.component.html',
    styleUrls: ['./location-add-timeslot-dialog.component.css']
})
export class LocationAddTimeslotDialogComponent implements OnChanges {
    @ViewChild(ModalComponent) modal: ModalComponent;

    @Input() location: Location;
    @Input() timeslot?: Timeslot;

    @Output() onNewTimeslot = new EventEmitter<Timeslot>();
    @Output() onUpdateTimeslot = new EventEmitter<Timeslot>();

    protected displayErrorTime: boolean;
    protected displayErrorSeats: boolean;

    protected timeslotFormGroup: FormGroup;
    protected timeslotInstance: Timeslot;

    protected seatThreshold: number = SEAT_COUNT_THRESHOLD;

    public constructor(
        private formBuilder: FormBuilder
    ) {
        this.timeslotInstance = new Timeslot();

        this.timeslotFormGroup = this.formBuilder.group({
            timeslotSequenceNumber: null,
            timeslotDate: null,
            openingHour: null,
            closingHour: null,
            seatCount: 0,
            reservable: false,
            reservableFrom: now(),
            repeatable: false
        });

        this.timeslotFormGroup.valueChanges.subscribe(values =>
            Object.assign(this.timeslotInstance, values)
        );
    }

    public ngOnChanges(changes: SimpleChanges): void {
        const reservableControl = this.timeslotFormGroup.controls.reservable;

        if (changes.location || changes.timeslot) {            
            // Update the timeslot form upon component input changes.
            this.timeslotFormGroup.patchValue(
                this.timeslotInstance = Timeslot.fromObject(
                    this.timeslot ?? new Timeslot()
                )
            );

            // Prepopulate the seatcount when we are updating.
            if (this.isUpdating()) {
                this.timeslotFormGroup.patchValue({
                    seatCount: this.location?.numberOfSeats
                });
            }

            // Force the reservable field in case the number of seats
            // exceeds a threshold defined in app.constants.ts.
            if (this.lockReservable()) {
                reservableControl.setValue(true);
                reservableControl.disable();
            } else {
                reservableControl.enable();
            }
        }
    }

    /**
     * Retrieves the minimum start date for a timeslot.
     *
     * @returns {Moment | null} The minimum start date as a Moment object, or null if not set.
     */
    protected getMinStartDate(): Moment|null {
        return null;
    }

    /**
     * Retrieves the minimum closing time for a timeslot.
     * 
     * @returns {Moment} The opening hour of the timeslot instance, which serves as the minimum closing time.
     */
    protected getMinClosingTime(): Moment {
        return this.timeslotInstance.openingHour;
    }

    /**
     * Gets the minimum reservable time slot start time.
     * 
     * @returns {Moment | null} - The current moment if the timeslot is reservable, otherwise null.
     */
    protected getMinReservableFrom(): Moment {
        return moment();
    }

    /**
     * Retrieves the maximum reservable date and time for a timeslot.
     *
     * @returns {Moment | null} The maximum reservable date and time as a Moment object, or null if not available.
     */
    protected getMaxReservableFrom(): Moment|null {
        return this.timeslotInstance.timeslotDate;
    }

    /**
     * Retrieves the minimum seat count for a timeslot.
     * 
     * @returns {number} The minimum seat count for a timeslot.
     */
    protected getMinSeatCount(): number {
        if (!this.timeslotInstance.amountOfReservations) {
            return 0;
        }

        return this.timeslotInstance.amountOfReservations + 30;
    }

    /**
     * Confirms the addition or update of a timeslot.
     * 
     * This method validates the timeslot by checking if the closing hour is after the opening hour
     * and if the seat count is greater than or equal to the number of reservations. If the timeslot
     * is valid, it triggers either the update or creation of the timeslot. If the timeslot is invalid,
     * it sets the appropriate error flags.
     * 
     * @returns {void}
     */
    protected confirm(): void {
        let hasErrors = false;        

        if (!this.timeslotInstance.closingHour.isAfter(this.timeslotInstance.openingHour)) {
            this.displayErrorTime = hasErrors = true;
        }

        if (this.timeslotInstance.seatCount < this.timeslotInstance.amountOfReservations) {
            this.displayErrorSeats = hasErrors = true;
        }

        if (hasErrors === false) {
            this.displayErrorTime = false;
            this.displayErrorSeats = false;
    
            if (this.isUpdating()) {                
                this.onUpdateTimeslot.next(this.timeslotInstance);
            } else {
                this.onNewTimeslot.next(this.timeslotInstance);
            }
        }
    }

    protected lockReservable(): boolean {
        return this.location && this.location.numberOfSeats > SEAT_COUNT_THRESHOLD;
    }

    protected isUpdating(): boolean {
        return !! this.timeslot?.timeslotSequenceNumber;
    }

    public openModal(): void {
        this.modal.open();
    }

    public closeModal(): void {
        this.modal.close();
    }
}
