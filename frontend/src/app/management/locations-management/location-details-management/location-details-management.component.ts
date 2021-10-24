import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { Location } from '../../../shared/model/Location';
import { ApplicationTypeFunctionalityService } from '../../../services/functionality/application-type/application-type-functionality.service';
import { LocationService } from '../../../services/api/locations/location.service';

@Component({
  selector: 'app-location-details-management',
  templateUrl: './location-details-management.component.html',
  styleUrls: ['./location-details-management.component.scss'],
})
export class LocationDetailsManagementComponent implements OnInit {
  locationObs: Observable<Location>;

  Object = Object;

  showLockersManagement: boolean;

  constructor(
    private locationService: LocationService,
    private route: ActivatedRoute,
    private functionalityService: ApplicationTypeFunctionalityService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const locationId = Number(this.route.snapshot.paramMap.get('locationId'));

    // Check if locationId is a Number before proceeding. If NaN, redirect to management locations.
    if (isNaN(locationId)) {
      this.router.navigate(['/management/locations']).catch(console.log);
      return;
    }

    // Note: invalidating cache in management
    this.locationObs = this.locationService.getLocation(locationId, true);
    this.showLockersManagement = this.functionalityService.showLockersManagementFunctionality();
  }
}
