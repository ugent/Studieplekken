import {Component, Input, OnInit} from '@angular/core';
import {ILocation} from '../../interfaces/ILocation';
import {LocationService} from '../../services/location.service';
import {Subject} from 'rxjs';
import {LocationReservationService} from "../../services/location-reservation.service";

@Component({
  selector: 'app-dashboard-board',
  templateUrl: './dashboard-board.component.html',
  styleUrls: ['./dashboard-board.component.css']
})
export class DashboardBoardComponent implements OnInit {
  @Input() reloadSubject: Subject<boolean> = new Subject<boolean>();
  locations: ILocation[];
  count: Object;
  loaded: boolean;

  constructor(private locationService: LocationService, private locationReservationService: LocationReservationService) {
  }

  ngOnInit(): void {
    // get all locations
    this.fetchLocations();
    // get number of reserved seats for today
    this.countReservations();

    this.reloadSubject.subscribe(value => {
      if (value) {
        const that = this;
        setTimeout(() => {
          that.fetchLocations();
          that.countReservations();
        }, 1000);
      }
    });
  }

  fetchLocations(): void {
    this.locationService.getAllLocations().subscribe(value => {
      this.locations = value;
    });
  }

  countReservations(): void{
    this.locationReservationService.getCountReservedSeats().subscribe(value => {this.count = value;});
  }

}
