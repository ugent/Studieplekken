import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { Location } from '../../../shared/model/Location';
import { ApplicationTypeFunctionalityService } from '../../../services/functionality/application-type/application-type-functionality.service';
import { LocationService } from '../../../services/api/locations/location.service';

@Component({
  selector: 'app-location-details-management',
  templateUrl: './location-details-management.component.html',
  styleUrls: ['./location-details-management.component.css'],
})
export class LocationDetailsManagementComponent implements OnInit {
  locationObs: Observable<Location>;

  Object = Object;

  showLockersManagement: boolean;

  constructor(
    private locationService: LocationService,
    private route: ActivatedRoute,
    private functionalityService: ApplicationTypeFunctionalityService
  ) {}

  ngOnInit(): void {
    const locationId = Number(this.route.snapshot.paramMap.get('locationId'));
    // Note: invalidating cache in management
    this.locationObs = this.locationService.getLocation(locationId, true);
    this.showLockersManagement = this.functionalityService.showLockersManagementFunctionality();
  }
}
