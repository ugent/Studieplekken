import { Component, OnInit } from '@angular/core';
import {User} from '../../shared/model/User';
import {AuthenticationService} from '../../services/authentication/authentication.service';
import {Observable} from 'rxjs';
import {Penalty} from '../../shared/model/Penalty';
import {transition, trigger, useAnimation} from '@angular/animations';
import {rowsAnimation} from '../../shared/animations/RowAnimation';
import {CustomDate, toDateString, toDateTimeViewString} from '../../shared/model/helpers/CustomDate';
import {objectExists} from '../../shared/GeneralFunctions';

@Component({
  selector: 'app-profile-penalties',
  templateUrl: './profile-penalties.component.html',
  styleUrls: ['./profile-penalties.component.css'],
  animations: [trigger('rowsAnimation', [
    transition('void => *', [
      useAnimation(rowsAnimation)
    ])
  ])]
})
export class ProfilePenaltiesComponent implements OnInit {
  user: User;
  penalties: Observable<Penalty[]>;

  toDateTimeViewString = (date: CustomDate) => toDateTimeViewString(date);
  toDateString = (date: CustomDate) => toDateString(date);
  objectExists = (obj: any) => objectExists(obj);

  constructor(private authenticationService: AuthenticationService) {
    authenticationService.user.subscribe(next => {
      this.user = next;
      this.penalties = authenticationService.getPenalties();
    });
  }

  ngOnInit(): void {
  }

}
