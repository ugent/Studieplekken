import { Component, OnInit } from '@angular/core';
import {AuthenticationService} from '../../../services/authentication/authentication.service';
import {Observable} from 'rxjs';
import {transition, trigger, useAnimation} from '@angular/animations';
import {rowsAnimation} from '../../../shared/animations/RowAnimation';
import {LocationReservation} from '../../../shared/model/LocationReservation';
import {compareDates, CustomDate, nowAsCustomDate, toDateString} from '../../../shared/model/helpers/CustomDate';

@Component({
  selector: 'app-profile-location-reservations',
  templateUrl: './profile-location-reservations.component.html',
  styleUrls: ['./profile-location-reservations.component.css'],
  animations: [trigger('rowsAnimation', [
    transition('void => *', [
      useAnimation(rowsAnimation)
    ])
  ])]
})
export class ProfileLocationReservationsComponent implements OnInit {
  locationReservations: Observable<LocationReservation[]>;

  toDateString = (date: CustomDate) => toDateString(date);
  compareDates = (date1: CustomDate, date2: CustomDate) => compareDates(date1, date2);
  nowAsCustomDate = () => nowAsCustomDate();

  constructor(private authenticationService: AuthenticationService) {
    authenticationService.user.subscribe(next => {
      this.locationReservations = authenticationService.getLocationReservations();
    });
  }

  ngOnInit(): void {
  }
}
