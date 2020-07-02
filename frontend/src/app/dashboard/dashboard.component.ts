import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {Subject} from 'rxjs';
import {AuthenticationService} from "../../services/authentication.service";
import {LocationService} from "../../services/location.service";
import {ILocation} from "../../interfaces/ILocation";

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  reloadSubject: Subject<boolean> = new Subject<boolean>();

  constructor(public authenticationService: AuthenticationService) {
    this.authenticationService = authenticationService;
  }

  ngOnInit(): void {
  }

}
