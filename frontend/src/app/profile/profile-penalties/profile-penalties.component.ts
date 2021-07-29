import { Component } from '@angular/core';
import { User } from '../../shared/model/User';
import { AuthenticationService } from '../../services/authentication/authentication.service';
import { Observable } from 'rxjs';
import { Penalty } from '../../shared/model/Penalty';
import { Location } from '../../shared/model/Location';
import { LocationService } from '../../services/api/locations/location.service';

@Component({
  selector: 'app-profile-penalties',
  templateUrl: './profile-penalties.component.html',
  styleUrls: ['./profile-penalties.component.scss'],
})
export class ProfilePenaltiesComponent {
  user: User;
  penalties: Observable<Penalty[]>;

  constructor(
    private authenticationService: AuthenticationService,
    private locationService: LocationService
  ) {
    authenticationService.user.subscribe((next) => {
      this.user = next;
      this.penalties = authenticationService.getPenalties();
    });
  }

  getLocation(locationId: number): Observable<Location> {
    return this.locationService.getLocation(locationId);
  }
}
