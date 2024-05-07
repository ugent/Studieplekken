import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, Router} from '@angular/router';
import {Observable} from 'rxjs';
import {AuthenticationService} from '../../authentication/authentication.service';
import {LoginRedirectService} from '../../authentication/login-redirect.service';
import {filter, tap} from 'rxjs/operators';
import {User} from '@/model/User';
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
    ) {
    }

    /**
     * This function is called when the route is being activated.
     *
     * @param route
     */
    canActivate(route: ActivatedRouteSnapshot): Observable<boolean> {
        return this.authenticationService.user.pipe(
            // Wait until the user has been fetched from the backend (can be empty).
            filter(() => this.authenticationService.hasAttemptedLogin),
            // Map the user observable on a boolean.
            map((user: User) => {
                const authorizations: string[][] = route.data.guards || [];

                if (authorizations.length > 0) {
                    let authorized = false;

                    for (const authorization of authorizations) {
                        authorized = authorized || authorization.every(guard =>
                            user.hasGuard(guard)
                        );
                    }

                    return authorized;
                }

                return true;
            }),
            // Redirect if necessary.
            tap((authorized: boolean) => {
                // We only redirect if the current user isn't logged-in.
                if (!authorized) {
                    this.loginRedirect.registerUrl(
                        this.router.getCurrentNavigation().finalUrl.toString()
                    );

                    void this.router.navigate([
                        'login'
                    ]);
                }
            })
        );
    }
}
