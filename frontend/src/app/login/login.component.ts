import { Route } from '@angular/compiler/src/core';
import { Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { environment } from '../../environments/environment';
import { userWantsTLogInLocalStorageKey } from '../app.constants';
import { AuthenticationService } from '../services/authentication/authentication.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
})
export class LoginComponent {
  casFlowTriggerUrl = environment.casFlowTriggerUrl;
  hoGentFlowTriggerUrl = environment.hoGentFlowTriggerUrl;
  arteveldeHSFlowTriggerUrl = environment.arteveldeHSFlowTriggerUrl;
  odiseeFlowTriggerUrl = environment.odiseeFlowTriggerUrl;
  lucaFlowTriggerUrl = environment.lucaFlowTriggerUrl;
  stadGentFlowTriggerUrl = environment.stadGentFlowTriggerUrl;
  kulFlowTriggerUrl = environment.kulFlowTriggerUrl;



  constructor(route: ActivatedRoute, authService: AuthenticationService, router: Router) {
    route.queryParamMap.subscribe((map) => {
      if (map.has('token')) {
        localStorage.setItem('access_token', map.get('token'));
        authService.login();
      }
    });
  }

  /**
   * When the user clicked on the login button, we set the localStorage's
   * key 'userWantsToLogin' to 'true', so that after redirect from the backend
   * to /dashboard, and the method AuthenticationService#login() is called,
   * the user can be logged in.
   */
  loginButtonClicked(): void {
    localStorage.setItem(userWantsTLogInLocalStorageKey, 'true');
  }

  getCallbackUrl() {
    const origin = window.location.origin;
    return `${origin}/login`
  }
}
