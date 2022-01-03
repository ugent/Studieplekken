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
    authenticationService: AuthenticationService,
  ) {
    this.penalties = authenticationService.penaltyObservable
  }

}
