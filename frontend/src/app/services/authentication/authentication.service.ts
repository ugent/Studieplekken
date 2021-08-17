import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { User, UserConstructor } from '../../shared/model/User';
import { HttpClient } from '@angular/common/http';
import { Penalty } from '../../shared/model/Penalty';
import { LocationReservation } from '../../shared/model/LocationReservation';
import {
  LockerReservation,
  LockerReservationConstructor,
} from '../../shared/model/LockerReservation';
import { map } from 'rxjs/operators';
import { PenaltyService } from '../api/penalties/penalty.service';
import { LocationReservationsService } from '../api/location-reservations/location-reservations.service';
import { LockerReservationService } from '../api/locker-reservations/locker-reservation.service';
import { Router } from '@angular/router';
import { UserService } from '../api/users/user.service';
import { api } from '../api/endpoints';
import { authenticationWasExpiredUrlLSKey, userWantsTLogInLocalStorageKey } from '../../app.constants';
import { Pair } from '../../shared/model/helpers/Pair';
import { CalendarPeriod } from '../../shared/model/CalendarPeriod';
import { environment } from 'src/environments/environment';

/**
 * The structure of the authentication service has been based on this article:
 *   - https://jasonwatmore.com/post/2020/05/15/angular-9-role-based-authorization-tutorial-with-example
 *   - but without using the actual JWT authentication, this is not save as mentioned in:
 *     - https://dev.to/rdegges/please-stop-using-local-storage-1i04
 *
 * The authentication of non UGent users will use HTTP-only cookies created by the backend.
 * The authentication of UGent users will use a cookie created from CAS, HTTP-only as well.
 *
 * Importance of HTTP-only cookies: https://blog.codinghorror.com/protecting-your-cookies-httponly/
 */
@Injectable({
  providedIn: 'root',
})
export class AuthenticationService {
  // BehaviorSubject to be able to emit on changes
  // private so that only the AuthenticationService can modify the user
  private userSubject: BehaviorSubject<User> = new BehaviorSubject<User>(null);
  // and other components can subscribe using the public observable
  // (which comes from the userSubject)
  public user: Observable<User> = this.userSubject.asObservable();

  private hasAuthoritiesSubject: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(
    false
  );
  public hasAuthoritiesObs: Observable<boolean> = this.hasAuthoritiesSubject.asObservable();
  private hasVolunteeredSubject: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(
    false
  );
  public hasVolunteeredObs: Observable<boolean> = this.hasVolunteeredSubject.asObservable();

  constructor(
    private http: HttpClient,
    private penaltyService: PenaltyService,
    private locationReservationService: LocationReservationsService,
    private lockerReservationService: LockerReservationService,
    private router: Router,
    private userService: UserService
  ) { }

  // **************************************************
  // *   Getters for values of the BehaviorSubjects   *
  // **************************************************

  userValue(): User {
    return this.userSubject.value;
  }

  hasAuthoritiesValue(): boolean {
    return this.hasAuthoritiesSubject.value;
  }

  hasVolunteeredValue(): boolean {
    return this.hasVolunteeredSubject.value;
  }

  /**
   * The flow of a cas login is as follows:
   *   1. frontend: sends the user to <backend-url>/login/cas
   *   2. backend: Spring Security CAS will notice that the user wants to log in
   *   3. backend: redirects the user to the cas login page of the CAS login provider (i.e. UGent)
   *   4. cas-server: verifies if the user credentials are valid and redirects
   *                  the user back to the backend server, with a ST ticket to be
   *                  validated by the backend as query-parameter
   *   5. backend: verifies the ST ticket with the cas-server
   *   6. backend: if successfully verified, Spring Security CAS sets a HTTP only
   *               cookie to be able to identify the user upon further requests to backend.
   *   7. backend: redirects the user back to /dashboard and asks the browser to set the
   *               HTTP only cookie.
   *   8. frontend: ngOnInit of dashboard is called, which asks to login the user
   *   9. frontend: in the login() method here, a request will only be sent to the backend to
   *                get information about the logged in user if the variable userWantsToLogIn
   *                was set to 'true' by the LoginComponent.
   */
  login(): void {
    this.http.get<User>(api.whoAmI).subscribe(
      (next) => {
        this.userSubject.next(next);
        this.updateHasAuthoritiesSubject(next);
        this.updateHasVolunteeredSubject(next);

        /**
         *  Spring's authentication ticket of a logged in user expires before the CAS authentication has expired.
         * This results in a lot of 302 HTTP responses triggering the Netdata monitoring tool in production.
         * To avoid this, the AuthenticationInterceptor intercepts unknown exceptions and calls this.authExpired() so that a new authentication session is started in Spring.
         * Since this.authExpired() saves the last visited url, we can redirect the user to the last visited url instead of to the dashboard.
         */
        const getPreviouslyAuthenticatedUrl = localStorage.getItem(authenticationWasExpiredUrlLSKey);
        if (getPreviouslyAuthenticatedUrl) {
          localStorage.setItem(authenticationWasExpiredUrlLSKey, '');
          this.router.navigateByUrl(getPreviouslyAuthenticatedUrl).then();
        }
      },
      () => {
        this.userSubject.next(UserConstructor.new());
        this.hasAuthoritiesSubject.next(false);
      }
    );
  }

  /**
   * The flow of a cas logout is as follows:
   *   1. frontend: sends a HTTP POST to <backend-url>/logout
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
      this.userSubject.next(UserConstructor.new());
      this.router.navigate(['/login']).then();
    });

    // to be sure, set the 'userWantsToLogin' variables to false
    localStorage.setItem(userWantsTLogInLocalStorageKey, 'false');
  }

  /**
   * Note that the variable '_userHasLoggedIn' is not used. The reason being
   * that after the user clicked on the login button, the user
   * is not yet logged in. Clicking on the login button starts the cas flow.
   * Only if the /whoAmI endpoint sent a HTTP 200 response, and the
   * userSubject has a valid user as value, we can consider the user as logged in.
   */
  isLoggedIn(): boolean {
    return this.userSubject.value && this.userSubject.value.userId !== '';
  }

  /**
   * This function immediately logs out the person and redirects it to the UGent login page.
   * If their login is still valid on the UGent login page, login will be resolved immediately.
   * If URL is given, then you will be redirected to your previous page on login. In the ideal case,
   * you were still logged in in CAS, the redirect back to the app gets resolved instantly and you
   * are back where your authentication expired.
   * @param url url to redirect to after refresh.
   */
  authExpired(url: string): void {
    this.userSubject.next(UserConstructor.new());

    if (url) {
      localStorage.setItem(authenticationWasExpiredUrlLSKey, url);
    }

    // Jump to login.ugent.be immediately. Chances are that this will instantly resolve any issues.
    window.location.href = environment.casFlowTriggerUrl;
  }

  isAdmin(): boolean {
    return this.userSubject.value && this.isLoggedIn()
      ? this.userSubject.value.admin
      : false;
  }

  updatePassword(from: string, to: string): Observable<void> {
    const body = { from, to, user: this.userValue() };
    return this.http.put<void>(api.changePassword, body);
  }

  // ********************************************************
  // *   Getters for information about the logged in user   *
  // ********************************************************

  getLocationReservations(): Observable<LocationReservation[]> {
    return this.locationReservationService.getLocationReservationsOfUser(
      this.userSubject.value.userId
    );
  }

  getLocationReservationsAndCalendarPeriods(): Observable<LocationReservation[]> {
    return this.locationReservationService.getLocationReservationsOfUser(
      this.userSubject.value.userId
    );
  }

  getLockerReservations(): Observable<LockerReservation[]> {
    const v = this.lockerReservationService.getLockerReservationsOfUser(
      this.userSubject.value.userId
    );

    return v.pipe(
      map<LockerReservation[], LockerReservation[]>((value) => {
        const reservations: LockerReservation[] = [];

        value.forEach((reservation) => {
          const obj = LockerReservationConstructor.newFromObj(reservation);
          reservations.push(obj);
        });

        return reservations;
      })
    );
  }

  getPenalties(): Observable<Penalty[]> {
    return this.penaltyService.getPenaltiesOfUserById(
      this.userSubject.value.userId
    );
  }

  // *******************
  // *   Auxiliaries   *
  // *******************
  updateHasAuthoritiesSubject(user: User): void {
    this.userService.hasUserAuthorities(user.userId).subscribe((next) => {
      this.hasAuthoritiesSubject.next(next);
    });
  }

  updateHasVolunteeredSubject(user: User): void {
    this.userService.hasUserVolunteered(user.userId).subscribe((next) => {
      this.hasVolunteeredSubject.next(next);
    });
  }
}
