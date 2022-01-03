import { Component } from '@angular/core';
import { User } from '../../shared/model/User';
import { AuthenticationService } from '../../services/authentication/authentication.service';
import { Observable } from 'rxjs';
import { tap } from "rxjs/operators"
import { Penalty } from '../../shared/model/Penalty';
import { Location } from '../../shared/model/Location';
import { LocationService } from '../../services/api/locations/location.service';
import { PenaltyList } from 'src/app/services/api/penalties/penalty.service';

@Component({
  selector: 'app-profile-penalties',
  templateUrl: './profile-penalties.component.html',
  styleUrls: ['./profile-penalties.component.scss'],
})
export class ProfilePenaltiesComponent {
  user: User;
  penalties: Observable<PenaltyList>;

  constructor(
    private authenticationService: AuthenticationService,
    private locationService: LocationService
  ) {
    this.penalties = authenticationService.penaltyObservable.pipe(tap(console.log))
  }

  getLocation(locationId: number): Observable<Location> {
    return this.locationService.getLocation(locationId);
  }

  getIssuedBy(penalty: Penalty) {
    if(!penalty.issuer)
      return "profile.penalties.table.system"

    else
      return penalty.issuer.firstName + " " + penalty.issuer.lastName
  }

  getPenaltyDescription(penalty: Penalty) {
    if(penalty.penaltyClass === "custom")
      return penalty.description;

      return penalty.penaltyClass
  }
}
