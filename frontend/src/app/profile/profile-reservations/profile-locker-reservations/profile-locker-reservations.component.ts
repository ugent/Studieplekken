import { Component, OnInit } from '@angular/core';
import {transition, trigger, useAnimation} from '@angular/animations';
import {rowsAnimation} from '../../../shared/animations/RowAnimation';
import {LockerReservation} from '../../../shared/model/LockerReservation';
import {Observable} from 'rxjs';
import {AuthenticationService} from '../../../services/authentication/authentication.service';
import {CustomDate, toDateString} from '../../../shared/model/helpers/CustomDate';

@Component({
  selector: 'app-profile-locker-reservations',
  templateUrl: './profile-locker-reservations.component.html',
  styleUrls: ['./profile-locker-reservations.component.css'],
  animations: [trigger('rowsAnimation', [
    transition('void => *', [
      useAnimation(rowsAnimation)
    ])
  ])]
})
export class ProfileLockerReservationsComponent implements OnInit {
  lockerReservations: Observable<LockerReservation[]>;

  toDateString = (date: CustomDate) => toDateString(date);

  constructor(private authenticationService: AuthenticationService) {
    authenticationService.user.subscribe(next => {
      // only change the lockerReservations if the user is logged in.
      // If you would omit the if-clause, a redudant API call will
      // be made with {userId} = '' (and thus requesting for all locker
      // reservations stored in the database, this is not something
      // we want...
      if (authenticationService.isLoggedIn()) {
        this.lockerReservations = authenticationService.getLockerReservations();
      }
    });
  }

  ngOnInit(): void {
  }

}
