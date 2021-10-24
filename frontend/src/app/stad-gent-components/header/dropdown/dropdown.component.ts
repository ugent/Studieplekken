import { Component, Input, OnInit } from '@angular/core';
import { isThisISOWeek } from 'date-fns';
import { Subject } from 'rxjs';
import { AuthenticationService } from 'src/app/services/authentication/authentication.service';
import { User } from 'src/app/shared/model/User';

@Component({
  selector: 'app-header-dropdown',
  templateUrl: './dropdown.component.html',
  styleUrls: ['./dropdown.component.scss']
})
export class DropdownComponent implements OnInit {
  @Input() user: User;
  @Input() accordion: Subject<boolean>;


  constructor(private authenticationService: AuthenticationService) { }

  ngOnInit(): void {
  }


  logout() {
    return this.authenticationService.logout();
  }

  showLoggedIn() {
    return !!this.user.userId;
  }

  showManagement() {
    return this.user.admin || this.user.userAuthorities.length != 0;
  }

  showScan() {
    return this.user.admin || this.user.userVolunteer.length != 0;
  }

  close() {
    this.accordion.next(false);
  }

}
