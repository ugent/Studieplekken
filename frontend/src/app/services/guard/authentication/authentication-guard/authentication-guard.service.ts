import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot} from '@angular/router';
import {AuthenticationService} from '../../../authentication/authentication.service';
import {Role} from '../../../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthenticationGuardService implements CanActivate {

  constructor(private router: Router,
              private authenticationService: AuthenticationService) { }

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    const url = state.url;
    let activate: boolean;

    if (url.startsWith('/login')) {
      activate = !this.authenticationService.isLoggedIn();
    } else if (url.startsWith('/dashboard')) {
      activate = true;
    } else if (url.startsWith('/profile')) {
      activate = this.authenticationService.isLoggedIn();
    } else if (url.startsWith('/scan')) {
      activate = this.authenticationService.userValue().roles.includes(Role.EMPLOYEE) ||
        this.authenticationService.userValue().roles.includes(Role.ADMIN);
    } else if (url.startsWith('/management')) {
      if (url.includes('/tags')) {
        activate = this.authenticationService.userValue().roles.includes(Role.ADMIN);
      } else {
        activate = this.authenticationService.userValue().roles.includes(Role.EMPLOYEE) ||
          this.authenticationService.userValue().roles.includes(Role.ADMIN);
      }
    } else if (url.startsWith('/information')) {
      activate = true;
    }

    if (!activate) {
      this.router.navigate(['/dashboard']).catch(console.log);
    }

    return activate;
  }
}
