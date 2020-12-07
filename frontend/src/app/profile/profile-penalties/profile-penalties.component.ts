import {Component, OnInit} from '@angular/core';
import {User} from '../../shared/model/User';
import {AuthenticationService} from '../../services/authentication/authentication.service';
import {Observable} from 'rxjs';
import {Penalty} from '../../shared/model/Penalty';

@Component({
  selector: 'app-profile-penalties',
  templateUrl: './profile-penalties.component.html',
  styleUrls: ['./profile-penalties.component.css'],
})
export class ProfilePenaltiesComponent implements OnInit {
  user: User;
  penalties: Observable<Penalty[]>;


  constructor(private authenticationService: AuthenticationService) {
    authenticationService.user.subscribe(next => {
      this.user = next;
      this.penalties = authenticationService.getPenalties();
    });
  }

  ngOnInit(): void {
  }

}
