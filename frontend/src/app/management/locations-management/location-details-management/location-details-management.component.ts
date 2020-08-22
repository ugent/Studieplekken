import { Component, OnInit } from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {Observable} from 'rxjs';
import {Location} from '../../../shared/model/Location';
import {LocationService} from '../../../services/api/locations/location.service';

@Component({
  selector: 'app-location-details-management',
  templateUrl: './location-details-management.component.html',
  styleUrls: ['./location-details-management.component.css']
})
export class LocationDetailsManagementComponent implements OnInit {
  locationObs: Observable<Location>;

  Object = Object;

  constructor(private locationService: LocationService,
              private route: ActivatedRoute) { }

  ngOnInit(): void {
    const locationName = this.route.snapshot.paramMap.get('locationName');
    this.locationObs = this.locationService.getLocation(locationName);
  }
}
