import { Component } from '@angular/core';
import { environment } from '../../environments/environment';
import { userWantsTLogInLocalStorageKey } from '../app.constants';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
})
export class LoginComponent {
  casFlowTriggerUrl = environment.casFlowTriggerUrl;
  hoGentFlowTriggerUrl = environment.hoGentFlowTriggerUrl;
  arteveldeHSFlowTriggerUrl = environment.arteveldeHSFlowTriggerUrl;

  /**
   * When the user clicked on the login button, we set the localStorage's
   * key 'userWantsToLogin' to 'true', so that after redirect from the backend
   * to /dashboard, and the method AuthenticationService#login() is called,
   * the user can be logged in.
   */
  loginButtonClicked(): void {
    localStorage.setItem(userWantsTLogInLocalStorageKey, 'true');
  }
}
