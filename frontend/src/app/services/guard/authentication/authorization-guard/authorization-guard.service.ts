import { Injectable } from '@angular/core';
import {
  ActivatedRouteSnapshot,
  CanActivate,
  Router,
  RouterStateSnapshot,
} from '@angular/router';
import { Observable } from 'rxjs';
import { map } from 'rxjs/internal/operators/map';
import { filter } from 'rxjs/operators';
import { LoginRedirectService } from 'src/app/services/authentication/login-redirect.service';
import { AuthenticationService } from '../../../authentication/authentication.service';

@Injectable({
  providedIn: 'root',
})
export class AuthorizationGuardService implements CanActivate {
  constructor(
    private router: Router,
    private authenticationService: AuthenticationService,
    private loginRedirect: LoginRedirectService
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean> {
    const url: string = state.url;
    let activate: boolean;

    return this.authenticationService.user.pipe(
      filter((f) => f != null),
      map(() => {
        console.log(url);
        if (url.startsWith('/login')) {
          activate = true;
        } else if (url.startsWith('/dashboard')) {
          activate = true;
        } else if (url.startsWith('/profile')) {
          activate = this.authenticationService.isLoggedIn();
          this.loginRedirect.registerUrl('/profile')
        } else if (url.startsWith('/scan')) {
          activate = this.isLoginAndAdminOrHasLocationsToScan();
          this.loginRedirect.registerUrl('/scan')
        } else if (url.startsWith('/management')) {
          if (this.authenticationService.isLoggedIn()) {
            if (
              url.includes('/tags') ||
              url.includes('/authorities') ||
              url.includes('/penalties') ||
              url.includes('/admins')
            ) {
              activate = this.authenticationService.isAdmin();
            } else {
              activate = this.isAdminOrHasAuthorities();
            }
            this.loginRedirect.registerUrl('/management')
          } else {
            this.loginRedirect.registerUrl('/management')
            activate = false;
          }
        } else if (url.startsWith('/information')) {
          activate = true;
        } else if (url.startsWith('/opening/overview')) {
          activate = true; // everybody is allowed to see this overview
        }

        if (!activate) {
          this.router.navigate(['/login']).catch(console.log);
        }

        console.log(activate);
        return activate;
      })
    );
  }

  isLoginAndAdminOrHasAuthorities(): boolean {
    return (
      this.authenticationService.isLoggedIn() && this.isAdminOrHasAuthorities()
    );
  }

  isLoginAndAdminOrHasLocationsToScan(): boolean {
    return (
      this.authenticationService.isLoggedIn() &&
      this.isAdminOrHasLocationsToScan()
    );
  }

  isAdminOrHasAuthorities(): boolean {
    return (
      this.authenticationService.isAdmin() ||
      this.authenticationService.hasAuthoritiesValue()
    );
  }

  isAdminOrHasLocationsToScan(): boolean {
    return (
      this.isAdminOrHasAuthorities() ||
      this.authenticationService.hasVolunteeredValue()
    );
  }
}
