import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot, UrlTree} from '@angular/router';
import {Observable} from 'rxjs';
import {of} from 'rxjs/internal/observable/of';
import {AuthenticationService} from '../../authentication/authentication.service';
import {LoginRedirectService} from '../../authentication/login-redirect.service';

@Injectable({
    providedIn: 'root'
})
export class AuthorizationGuardService implements CanActivate {
    constructor(
        protected router: Router,
        protected authenticationService: AuthenticationService,
        protected loginRedirect: LoginRedirectService
    ) {}

    canActivate(
        route: ActivatedRouteSnapshot,
        state: RouterStateSnapshot
    ): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
        let authorized = false;

        const authorizations: string[][] = route.data.guards;

        if (authorizations && authorizations.length) {
            for (const authorization of authorizations) {
                let currentAuthorized = true;

                for (const guard of authorization) {
                    currentAuthorized = currentAuthorized && this.hasGuard(
                        guard
                    );
                }

                authorized = authorized || currentAuthorized;
            }
        }
        // We only redirect if the current user isn't logged-in.
        if (!authorized && !this.hasGuard('user')) {
            this.loginRedirect.registerUrl(
                this.router.getCurrentNavigation().finalUrl.toString()
            );
            void this.router.navigate(['login']);
        }

        return of(authorized);
    }

    /**
     * Check whether the current request
     * complies with the given guard.
     * @param guard the guard to check.
     * @return whether the guard complies out.
     */
    private hasGuard(guard: string): boolean {
        // The custom defined guards.
        // Todo: make this run asynchronously,
        //  so reloading the page ensures the user object has been fetched before attempting to authorize.
        const service = this.authenticationService;
        const guards = {
            user: service.isLoggedIn(),
            admin: service.isAdmin(),
            scanner: service.hasLocationsToScan(),
            authorities: service.hasAuthorities()
        };
        return guards[guard];
    }
}
