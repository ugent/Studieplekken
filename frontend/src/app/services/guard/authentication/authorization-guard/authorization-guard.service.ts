import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot} from '@angular/router';
import {AuthenticationService} from '../../../authentication/authentication.service';

@Injectable({
  providedIn: 'root'
})
export class AuthorizationGuardService implements CanActivate {

  constructor(private router: Router,
              private authenticationService: AuthenticationService) { }

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    const url: string = state.url;
    let activate: boolean;

    if (url.startsWith('/login')) {
      activate = !this.authenticationService.isLoggedIn();
    } else if (url.startsWith('/dashboard')) {
      activate = true;
    } else if (url.startsWith('/profile')) {
      activate = this.authenticationService.isLoggedIn();
    } else if (url.startsWith('/scan')) {
      activate = this.isLoginAndAdminOrHasAuthorities();
    } else if (url.startsWith('/management')) {
      if (this.authenticationService.isLoggedIn()) {
        if (url.includes('/tags') ||
            url.includes('/authorities') ||
            url.includes('/penalties')) {
          activate = this.authenticationService.isAdmin();
        } else {
          activate = this.isAdminOrHasAuthorities();
        }
      } else {
        activate = false;
      }
    } else if (url.startsWith('/information')) {
      activate = true;
    }

    if (!activate) {
      this.router.navigate(['/dashboard']).catch(console.log);
    }

    return activate;
  }

  isLoginAndAdminOrHasAuthorities(): boolean {
    return this.authenticationService.isLoggedIn() && this.isAdminOrHasAuthorities();
  }

  isAdminOrHasAuthorities(): boolean {
    return this.authenticationService.isAdmin() ||
      this.authenticationService.hasAuthoritiesValue();
  }
}
