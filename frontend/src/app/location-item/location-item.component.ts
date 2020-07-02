import {AfterViewInit, Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {ILocation} from "../../interfaces/ILocation";
import {AuthenticationService} from "../../services/authentication.service";


@Component({
  selector: 'app-location-item',
  templateUrl: './location-item.component.html',
  styleUrls: ['./location-item.component.css']
})
export class LocationItemComponent implements OnInit,AfterViewInit {
  @Input() location: ILocation;
  @Input() reservedSeats: number;
  authenticationService: AuthenticationService;

  constructor(authenticationService: AuthenticationService) {
    this.authenticationService = authenticationService;
  }

  ngOnInit(): void {
  }
  ngAfterViewInit(): void {
    // set progress bar
    document.getElementById(this.location.name).style.width = (Math.floor((this.reservedSeats / this.location.numberOfSeats) * 100)).toString() + '%';
  }
}
