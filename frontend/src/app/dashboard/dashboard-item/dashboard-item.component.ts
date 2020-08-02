import {AfterViewInit, Component, Input, OnInit} from '@angular/core';
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
  numberOfReservations: Observable<number>;

  constructor(private locationService: LocationService) { }

  ngOnInit(): void {
    this.numberOfReservations = this.locationService.getNumberOfReservations(this.location);
  }

  ngAfterViewInit(): void {
    this.numberOfReservations.subscribe(next => {
      document.getElementById(this.location.name).style.width =
        (Math.floor((next / this.location.numberOfSeats) * 100)).toString() + '%';
    });
  }

  handleImageError(): void {
    this.location.imageUrl = vars.defaultLocationImage;
  }
}
