import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import * as moment from 'moment';
import { Moment } from 'moment';
import { BsModalService } from 'ngx-bootstrap/modal';
import { AuthenticationService } from 'src/app/services/authentication/authentication.service';
import { Timeslot } from 'src/app/shared/model/Timeslot';
import { Location } from 'src/app/shared/model/Location';

@Component({
  selector: 'app-location-add-timeslot-dialog',
  templateUrl: './location-add-timeslot-dialog.component.html',
  styleUrls: ['./location-add-timeslot-dialog.component.css']
})
export class LocationAddTimeslotDialogComponent implements OnInit, OnChanges {
  public model: Timeslot = new Timeslot(null, null, null, null, true, null, null, null, null);
  @Output() onNewTimeslot: EventEmitter<Timeslot> = new EventEmitter();
  @Input() location: Location;

  constructor(private authenticationService: AuthenticationService, private modalService: BsModalService) { }


  ngOnInit(): void {}

  ngOnChanges(changes: SimpleChanges): void {
    const location: Location = changes.location.currentValue;
    this.model.locationId = location.locationId;
    this.model.seatCount = location.numberOfSeats;

    if(changes.timeslot) {
      const oldTimeslot: Timeslot = changes.timeslot.currentValue;
      this.model.locationId = oldTimeslot.locationId;
      this.model.reservable = oldTimeslot.reservable;
      this.model.closingHour = oldTimeslot.closingHour;
      this.model.openingHour = oldTimeslot.openingHour;
      this.model.timeslotDate = oldTimeslot.timeslotDate;
      this.model.timeslotSequenceNumber = oldTimeslot.timeslotSequenceNumber;
      this.model.reservableFrom = oldTimeslot.reservableFrom;  
    }
  }

  getMinStartDate(): Moment {
    if (this.authenticationService.isAdmin()) {
      return null;
    } else {
      return moment().add(3, 'weeks').day(8);
    }
  }

  getMinReservableFrom(model: Timeslot): Moment {
    if (!model.timeslotDate) {
      return null;
    } else {
      return moment(model.timeslotDate).subtract(3, 'weeks').day(2);
    }
  }

  getTimeslotTimeInMinutes(model) {
    if (!model.closingTime) { return null; }

    return model.openingTime?.diff(model.closingTime, 'minutes')
  }

  closeModal(): void {
    this.modalService.hide();
  }

  confirm() {
    this.onNewTimeslot.next(this.model);
  }

}
