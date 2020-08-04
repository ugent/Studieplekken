import {AfterViewInit, Component, ElementRef, Input, OnInit, ViewChild} from '@angular/core';
import {Location} from '../../shared/model/Location';
import {vars} from '../../../environments/environment';
import {LocationService} from '../../services/api/location.service';
import {Observable} from 'rxjs';

@Component({
  selector: 'app-dashboard-item',
  templateUrl: './dashboard-item.component.html',
  styleUrls: ['./dashboard-item.component.css']
})
export class DashboardItemComponent implements OnInit, AfterViewInit {
  @Input() location: Location;

  occupation: number;

  constructor(private locationService: LocationService) { }

  ngOnInit(): void {
    this.locationService.getNumberOfReservations(this.location).subscribe(next => {
      this.occupation = Math.round(100 * next / this.location.numberOfSeats);
    });
  }

  ngAfterViewInit(): void {
  }

  handleImageError(): void {
    this.location.imageUrl = vars.defaultLocationImage;
  }
}
