import { Component, OnInit } from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {LocationDetailsService} from '../../../services/location-details/location-details.service';
import {Observable} from 'rxjs';
import {Location} from '../../../shared/model/Location';

@Component({
  selector: 'app-location-details-management',
  templateUrl: './location-details-management.component.html',
  styleUrls: ['./location-details-management.component.css']
})
export class LocationDetailsManagementComponent implements OnInit {
  locationObs: Observable<Location> = this.locationDetailsService.locationObs;

  Object = Object;

  constructor(private locationDetailsService: LocationDetailsService,
              private route: ActivatedRoute) { }

  ngOnInit(): void {
    const locationName = this.route.snapshot.paramMap.get('locationName');
    this.locationDetailsService.loadLocation(locationName);
  }
}
