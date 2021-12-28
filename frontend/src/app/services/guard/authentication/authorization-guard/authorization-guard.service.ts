import { Injectable } from '@angular/core';
import {
  ActivatedRouteSnapshot,
  CanActivate,
  Router,
  RouterStateSnapshot,
} from '@angular/router';
import { combineLatest, Observable, of } from 'rxjs';
import { map } from 'rxjs/internal/operators/map';
import { filter, switchMap, tap } from 'rxjs/operators';
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
      switchMap(() => {
        if (url.startsWith('/login')) {
          return of(true);
        } else if (url.startsWith('/dashboard')) {
          return of(true);
        } else if (url.startsWith('/profile')) {
          this.loginRedirect.registerUrl('/profile');

          return of(this.authenticationService.isLoggedIn());
        } else if (url.startsWith('/scan')) {
          this.loginRedirect.registerUrl('/scan');
          return this.isLoginAndAdminOrHasLocationsToScan();
        } else if (url.startsWith('/management')) {
          this.loginRedirect.registerUrl('/management');
          console.log(url);

          if (this.authenticationService.isLoggedIn()) {
            if (
              url.includes('/tags') ||
              url.includes('/authorities') ||
              url.includes('/penalties') ||
              url.includes('/admins')
            ) {
              return of(this.authenticationService.isAdmin());
            } else {
              return this.isAdminOrHasAuthorities();
            }
          } else {
            this.loginRedirect.registerUrl('/management');
            return of(false);
          }
        } else if (url.startsWith('/information')) {
          return of(true);
        } else if (url.startsWith('/opening/overview')) {
          return of(true); // everybody is allowed to see this overview
        }

        if (!activate) {
        }

        return of(activate);
      }),
      tap((t) => {
        if (!t) {
          this.router.navigate(['/login']).catch(console.log);
        }
      })
    );
  }

  isLoginAndAdminOrHasAuthorities(): Observable<boolean> {
    return (
      this.authenticationService.isLoggedIn() && this.isAdminOrHasAuthorities()
    );
  }

  isLoginAndAdminOrHasLocationsToScan(): Observable<boolean> {
    return (
      this.authenticationService.isLoggedIn() ? this.isAdminOrHasLocationsToScan() : of(false)
    );
  }

  isAdminOrHasAuthorities(): Observable<boolean> {
    this.authenticationService.hasAuthoritiesObs.subscribe(() => console.log());
    return (
      this.authenticationService.isLoggedIn() ? (this.authenticationService.isAdmin() ? of(true):this.authenticationService.hasAuthoritiesObs.pipe(filter(t => t !== null))) : of(false)
    );
  }

  isAdminOrHasLocationsToScan(): Observable<boolean> {
    return combineLatest([
      this.isAdminOrHasAuthorities(),
      this.authenticationService.hasVolunteeredObs.pipe(filter(t => t !== null))]
    ).pipe(map(([a, b]) => a || b))
  }
}
