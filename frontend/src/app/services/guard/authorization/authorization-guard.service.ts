import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot, UrlTree} from '@angular/router';
import {Observable} from 'rxjs';
import {AuthenticationService} from '../../authentication/authentication.service';
import {LoginRedirectService} from '../../authentication/login-redirect.service';
import {filter} from 'rxjs/internal/operators/filter';
import {tap} from 'rxjs/operators';
import {User} from '../../../shared/model/User';
import {map} from 'rxjs/internal/operators/map';

/**
 * Authorization Service
 *
 * | Usage |
 * Add this service as a `CanActivate` guard on a route. In the route's data, add a field `guards`.
 * This field represents what guards should comply in order for the route to be available.
 * | Example |
 * A `guards` value of [['role1'], ['role2']] means that the current user should either have `role1` OR `role2`.
 * A `guards` value of [['role1', 'role2'], ['role3']] means that the current user should either have `role1` AND `role2` OR `role3`.
 * | Goal |
 * This implementation ensures the best flexibility when adding new roles/guards in the future
 * that don't follow the current standards (a role hierarchy).
 * | Adding guards |
 * Just add your new guards in the `hasGuard` function.
 */
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
        return this.authenticationService.user.pipe(
            // Only run when fetching of the user object is complete.
            filter((user: User) => user != null),
            // Map the user observable on a boolean.
            map(() => {
                const authorizations: string[][] = route.data.guards || [];
                if (authorizations.length > 0) {
                    let authorized = false;
                    for (const authorization of authorizations) {
                        authorized = authorized || authorization.every(guard => this.hasGuard(guard));
                    }
                    return authorized;
                }
                return true;
            }),
            // Redirect if necessary.
            tap((authorized: boolean) => {
                // We only redirect if the current user isn't logged-in.
                if (!authorized && !this.hasGuard('user')) {
                    this.loginRedirect.registerUrl(
                        this.router.getCurrentNavigation().finalUrl.toString()
                    );
                    void this.router.navigate(['login']);
                }
            })
        );
    }

    /**
     * Check whether the current request
     * complies with the given guard.
     * @param guard the guard to check.
     * @return whether the guard complies out.
     */
    private hasGuard(guard: string): boolean {
        // The custom defined guards.
        const service = this.authenticationService;
        // In this implementation, roles follow a hierarchy.
        const guards = {
            user: service.isLoggedIn(),
            scanner: service.isScanner(),
            authorities: service.isAuthority(),
            admin: service.isAdmin()
        };
        return guards[guard];
    }
}
