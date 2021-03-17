import { Component, Input, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { LocationService } from 'src/app/services/api/locations/location.service';
import { User } from 'src/app/shared/model/User';
import { Location } from 'src/app/shared/model/Location';

@Component({
  selector: 'app-volunteer-management-panel',
  templateUrl: './volunteer-management-panel.component.html',
  styleUrls: ['./volunteer-management-panel.component.css']
})
export class VolunteerManagementPanelComponent implements OnInit {

  @Input() location: Location

  volunteerObs: Observable<User[]>

  constructor(private locationService: LocationService) { }

  ngOnInit(): void {
    this.volunteerObs = this.locationService.getVolunteers(this.location.locationId)
  }

}
