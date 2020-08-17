import { Component, OnInit } from '@angular/core';
import {LocationService} from '../services/api/locations/location.service';
import {Observable} from 'rxjs';
import {Location} from '../shared/model/Location';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  locations: Observable<Location[]>;

  constructor(private locationService: LocationService) { }

  ngOnInit(): void {
    this.locations = this.locationService.getLocations();
  }

}
