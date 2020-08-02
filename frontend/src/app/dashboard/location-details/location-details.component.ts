import {Component, OnInit} from '@angular/core';
import {Location} from '../../shared/model/Location';
import {ActivatedRoute} from "@angular/router";
import {Observable} from "rxjs";
import {LocationService} from "../../services/api/location.service";
import {vars} from "../../../environments/environment";

@Component({
  selector: 'app-location-details',
  templateUrl: './location-details.component.html',
  styleUrls: ['./location-details.component.css']
})
export class LocationDetailsComponent implements OnInit {
  location: Observable<Location>;

  constructor(private locationService: LocationService,
              private route: ActivatedRoute) { }

  ngOnInit(): void {
    const locationName = this.route.snapshot.paramMap.get('locationName');
    this.location = this.locationService.getLocation(locationName);
  }

  handleImageError(location: Location): void {
    location.imageUrl = vars.defaultLocationImage;
  }
}
