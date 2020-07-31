import { Component, OnInit } from '@angular/core';
import {ILockerReservation} from "../../interfaces/ILockerReservation";
import {LockerReservationService} from "../../services/locker-reservation.service";
import {AuthenticationService} from "../../services/authentication.service";
import {IDate} from "../../interfaces/IDate";
import {compareDates, CustomDate, dateToString} from "../../interfaces/CustomDate";

@Component({
  selector: 'app-locker-overview',
  templateUrl: './locker-overview.component.html',
  styleUrls: ['./locker-overview.component.css']
})
export class LockerOverviewComponent implements OnInit {
  results: ILockerReservation[];
  displayCancel: any = 'none';
  selectedReservation: ILockerReservation;

  constructor(private lockerReservationService: LockerReservationService, private authenticationService: AuthenticationService) { }

  ngOnInit(): void {
    this.lockerReservationService.getAllLockerReservationsOfUser(this.authenticationService.getCurrentUser().augentID).subscribe((value) => {
      this.results = value;
      this.sortResults();
    })
  }

  getStatus(lockerReservation: ILockerReservation): string {
    if(!lockerReservation.keyPickedUp){
      return "keyStatus1";
    }
    if(lockerReservation.keyPickedUp && !lockerReservation.keyBroughtBack){
      return "keyStatus2";
    }
    return "keyStatus3";
  }

  cancelLockerReservation(): void {
    this.lockerReservationService.deleteLockerReservation(this.selectedReservation).subscribe(value => {
      value = value;
      this.ngOnInit();
    });
  }

  sortResults(): void {
    this.results.sort((res1, res2) => {
      return compareDates(res2.startDate, res1.startDate);
    });
  }
}
