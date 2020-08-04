import { Component, OnInit } from '@angular/core';
import {AuthenticationService} from '../../services/authentication/authentication.service';
import {User} from '../../shared/model/User';

@Component({
  selector: 'app-profile-overview',
  templateUrl: './profile-overview.component.html',
  styleUrls: ['./profile-overview.component.css']
})
export class ProfileOverviewComponent implements OnInit {
  user: User;

  constructor(private authenticationService: AuthenticationService) {
    authenticationService.user.subscribe(next => {
      this.user = next;
    });
  }

  ngOnInit(): void {
  }

}
