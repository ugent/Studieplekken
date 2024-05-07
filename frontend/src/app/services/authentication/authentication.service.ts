import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Router} from '@angular/router';
import {BehaviorSubject, Observable, ReplaySubject, Subject} from 'rxjs';
import {api} from '../api/endpoints';
import {LocationReservationsService} from '../api/location-reservations/location-reservations.service';
import {PenaltyList, PenaltyService} from '../api/penalties/penalty.service';
import {LoginRedirectService} from './login-redirect.service';
import {tap} from 'rxjs/operators';
import {User, UserConstructor} from '../../model/User';
import {authenticationWasExpiredUrlLSKey, userWantsTLogInLocalStorageKey} from '../../app.constants';
import {LocationReservation} from '../../model/LocationReservation';

/**
 * The structure of the authentication service has been based on this article:
 *   - https://jasonwatmore.com/post/2020/05/15/angular-9-role-based-authorization-tutorial-with-example
 *   - but without using the actual JWT authentication, this is not safe as mentioned in:
 *     - https://dev.to/rdegges/please-stop-using-local-storage-1i04
 *
 * The authentication of non UGent users will use HTTP-only cookies created by the backend.
 * The authentication of UGent users will use a cookie created from CAS, HTTP-only as well.
 *
 * Importance of HTTP-only cookies: https://blog.codinghorror.com/protecting-your-cookies-httponly/
 */
@Injectable({
    // Make the service a singleton service.
    providedIn: 'root'
})
export class AuthenticationService {
    public hasAttemptedLogin = false;

    private userSubject: BehaviorSubject<User>;
    public user: Observable<User>;

    private penaltySubject: Subject<PenaltyList>;
    public penaltyObservable: Observable<PenaltyList>;

    constructor(
        private http: HttpClient,
        private locationReservationService: LocationReservationsService,
        private router: Router,
        private loginRedirectService: LoginRedirectService,
        private penaltyService: PenaltyService
    ) {
        // Initialize subjects.
        this.userSubject = new BehaviorSubject<User>(UserConstructor.new());
        this.user = this.userSubject.asObservable();

        this.penaltySubject = new ReplaySubject();
        this.penaltyObservable = this.penaltySubject.asObservable();

        // When the access token is modified (e.g. in a different tab),
        // refresh the authentication faq.
        window.onstorage = (event: StorageEvent) => {
            if (event.key === 'access_token') {
                if (!event.newValue) {
                    this.logout();
                } else {
                    this.login();
                }
            }
        };
    }

    // **************************************************
    // *   Getters for values of the BehaviorSubjects   *
    // **************************************************
    userValue(): User {
        return this.userSubject.value;
    }

    /**
     * The flow of a cas login is as follows:
     *   1. frontend: sends the user to <backend-url>/login/cas
     *   2. backend: Spring Security CAS will notice that the user wants to log in
     *   3. backend: redirects the user to the cas login page of the CAS login provider (i.e. UGent)
     *   4. cas-server: verifies if the user credentials are valid and redirects
     *                  the user back to the backend server, with an ST ticket to be
     *                  validated by the backend as query-parameter
     *   5. backend: verifies the ST ticket with the cas-server
     *   6. backend: if successfully verified, Spring Security CAS sets an HTTP only
     *               cookie to be able to identify the user upon further requests to backend.
     *   7. backend: redirects the user back to /dashboard and asks the browser to set the
     *               HTTP only cookie.
     *   8. frontend: ngOnInit of dashboard is called, which asks to log in the user
     *   9. frontend: in the login() method here, a request will only be sent to the backend to
     *                get faq about the logged-in user if the variable userWantsToLogIn
     *                was set to 'true' by the LoginComponent.
     */
    login(redirect = false): void {
        this.http.get<User>(api.whoAmI).pipe(
            tap(() => this.hasAttemptedLogin = true)
        ).subscribe(
            (next) => {
                this.userSubject.next(
                    UserConstructor.newFromObj(next)
                );

                this.penaltyService.getPenaltiesOfUserById(next.userId).subscribe(penalties =>
                    this.penaltySubject.next(penalties)
                );

                if (next.userId && redirect) {
                    this.loginRedirectService.navigateToLastUrl();
                }

                /**
                 *  Spring's authentication ticket of a logged-in user expires before the CAS authentication has expired.
                 * This results in a lot of 302 HTTP responses triggering the Netdata monitoring tool in production.
                 * To avoid this, the AuthenticationInterceptor intercepts unknown exceptions and calls this.authExpired()
                 * so that a new authentication session is started in Spring.
                 * Since this.authExpired() saves the last visited url, we can redirect the user to the
                 * last visited url instead of to the dashboard.
                 */
                // const getPreviouslyAuthenticatedUrl = localStorage.getItem(authenticationWasExpiredUrlLSKey);
                // if (getPreviouslyAuthenticatedUrl) {
                //   localStorage.setItem(authenticationWasExpiredUrlLSKey, '');
                //   this.router.navigateByUrl(getPreviouslyAuthenticatedUrl).then();
                // }
            },
            () => {
                this.userSubject.next(
                    UserConstructor.new()
                );
            }
        );
    }

    /**
     * The flow of a cas logout is as follows:
     *   1. frontend: sends an HTTP POST to <backend-url>/logout
     *   2. backend: Spring Security CAS will notice that the user wants to log out
     *   3. backend: communicates with CAS server to log out the user
     *   4. backend: sends a HTTP 200 response if successfully logged out
     *   5. frontend: because the user is logged out, the userSubject needs to
     *      be updated. Therefore, we send a next() signal to all the subscribers
     *      of the observable connected to the userSubject.
     *   6. frontend: redirects the user to the login page
     */
    logout(): void {
        this.http.post(api.logout, {}).subscribe(() => {
            this.userSubject.next(
                UserConstructor.new()
            );

            void this.router.navigate(['/login']);
        });

        localStorage.removeItem('access_token');

        // to be sure, set the 'userWantsToLogin' variables to false
        localStorage.setItem(userWantsTLogInLocalStorageKey, String(false));
    }

    /**
     * This function immediately logs out the person and redirects it to the UGent login page.
     * If their login is still valid on the UGent login page, login will be resolved immediately.
     * If URL is given, then you will be redirected to your previous page on login. In the ideal case,
     * you were still logged in CAS, the redirect back to the app gets resolved instantly and you
     * are back where your authentication expired.
     * @param url url to redirect to after refresh.
     */
    authExpired(url: string): void {
        this.userSubject.next(
            UserConstructor.new()
        );

        if (url) {
            localStorage.setItem(authenticationWasExpiredUrlLSKey, url);
        }

        // Jump to login.ugent.be immediately. Chances are that this will instantly resolve any issues.
        void this.router.navigateByUrl('/login');
    }

    getUserObs(): Observable<User> {
        return this.userSubject;
    }

    // ********************************************************
    // *   Getters for faq about the logged in user   *
    // ********************************************************
    getLocationReservations(): Observable<LocationReservation[]> {
        return this.locationReservationService.getLocationReservationsOfUser(
            this.userSubject.value.userId
        );
    }

    // *******************
    // *   Auxiliaries   *
    // *******************
    updatePassword(from: string, to: string): Observable<void> {
        return this.http.put<void>(api.changePassword, {
            from, to,
            user: this.userValue()
        });
    }

    substituteLogin(email: string): void {
        localStorage.setItem('impersonate', email);

        this.http.get(api.whoAmI, {
            headers: new HttpHeaders().set('AS-USER', email)
        }).subscribe(() =>
            this.login()
        );
    }
}
