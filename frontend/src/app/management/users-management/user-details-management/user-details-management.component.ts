import {Component, OnInit} from '@angular/core';
import {Observable} from 'rxjs';
import {User} from '../../../shared/model/User';
import {UserDetailsService} from '../../../services/user-details/user-details.service';
import {ActivatedRoute} from '@angular/router';

@Component({
  selector: 'app-user-details-management',
  templateUrl: './user-details-management.component.html',
  styleUrls: ['./user-details-management.component.css']
})
export class UserDetailsManagementComponent implements OnInit {
  userObs: Observable<User> = this.userDetailsService.userObs;

  userQueryingError: boolean = undefined;
  userId: string;

  constructor(private userDetailsService: UserDetailsService,
              private route: ActivatedRoute) { }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    this.userId = id;
    this.userDetailsService.loadUser(id);

    this.userObs.subscribe(
      (next) => {
        this.userQueryingError = false;
      }, (error) => {
        this.userQueryingError = true;
      }
    );
  }
}
