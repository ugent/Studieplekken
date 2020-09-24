import {Component, Input, OnInit} from '@angular/core';
import {Observable} from 'rxjs';
import {User} from '../../../../shared/model/User';
import {Role} from '../../../../../environments/environment';

@Component({
  selector: 'app-user-authorities-management',
  templateUrl: './user-authorities-management.component.html',
  styleUrls: ['./user-authorities-management.component.css']
})
export class UserAuthoritiesManagementComponent implements OnInit {
  @Input() userObs: Observable<User>;

  constructor() { }

  ngOnInit(): void {
  }

  isUserAllowedToHaveAuthorities(user: User): boolean {
    return (user.roles.includes(Role.ADMIN) ||
      user.roles.includes(Role.EMPLOYEE));
  }
}
