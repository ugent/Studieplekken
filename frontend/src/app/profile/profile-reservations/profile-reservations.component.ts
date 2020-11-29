import { Component, OnInit } from '@angular/core';
import {ApplicationTypeFunctionalityService} from '../../services/functionality/application-type/application-type-functionality.service';

@Component({
  selector: 'app-profile-reservations',
  templateUrl: './profile-reservations.component.html',
  styleUrls: ['./profile-reservations.component.css']
})
export class ProfileReservationsComponent implements OnInit {

  showLockerReservationFunctionality: boolean;

  constructor(private functionalityService: ApplicationTypeFunctionalityService) {
    this.showLockerReservationFunctionality = functionalityService.showLockersManagementFunctionality();
  }

  ngOnInit(): void {
  }

}
