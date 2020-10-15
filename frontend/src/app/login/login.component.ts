import { Component, OnInit } from '@angular/core';
import {vars} from '../../environments/environment';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

  casFlowTriggerUrl = vars.casFlowTriggerUrl;

  constructor() { }

  ngOnInit(): void {
  }

  /**
   * When the user clicked on the login button, we set the localStorage's
   * key 'userWantsToLogin' to 'true', so that after redirect from the backend
   * to /dashboard, and the method AuthenticationService#login() is called,
   * the user can be logged in.
   */
  loginButtonClicked(): void {
    localStorage.setItem(vars.userWantsTLogInLocalStorageKey, 'true');
  }
}
