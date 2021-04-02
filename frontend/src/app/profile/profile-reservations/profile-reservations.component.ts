import { Component } from '@angular/core';
import { ApplicationTypeFunctionalityService } from '../../services/functionality/application-type/application-type-functionality.service';

@Component({
  selector: 'app-profile-reservations',
  templateUrl: './profile-reservations.component.html',
  styleUrls: ['./profile-reservations.component.css'],
})
export class ProfileReservationsComponent {
  showLockerReservationFunctionality: boolean;

  constructor(functionalityService: ApplicationTypeFunctionalityService) {
    this.showLockerReservationFunctionality = functionalityService.showLockersManagementFunctionality();
  }
}
