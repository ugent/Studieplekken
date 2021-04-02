import { Component } from '@angular/core';
import { LockerReservation } from '../../../shared/model/LockerReservation';
import { Observable } from 'rxjs';
import { AuthenticationService } from '../../../services/authentication/authentication.service';

@Component({
  selector: 'app-profile-locker-reservations',
  templateUrl: './profile-locker-reservations.component.html',
  styleUrls: ['./profile-locker-reservations.component.css'],
})
export class ProfileLockerReservationsComponent {
  lockerReservations: Observable<LockerReservation[]>;

  constructor(authenticationService: AuthenticationService) {
    authenticationService.user.subscribe(() => {
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
}
