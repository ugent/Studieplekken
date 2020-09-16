import { Injectable } from '@angular/core';
import {BehaviorSubject, Observable} from 'rxjs';
import {User, UserConstructor} from '../../shared/model/User';
import {HttpClient, HttpParams} from '@angular/common/http';
import {api} from '../../../environments/environment';
import {Penalty} from '../../shared/model/Penalty';
import {LocationReservation} from '../../shared/model/LocationReservation';
import {LockerReservation, LockerReservationConstructor} from '../../shared/model/LockerReservation';
import {map, tap} from 'rxjs/operators';
import {PenaltyService} from '../api/penalties/penalty.service';
import {LocationReservationsService} from '../api/location-reservations/location-reservations.service';
import {LockerReservationService} from '../api/locker-reservations/locker-reservation.service';

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
  providedIn: 'root'
})
export class AuthenticationService {
  // BehaviorSubject to be able to emit on changes
  // private so that only the AuthenticationService can modify the user
  private userSubject: BehaviorSubject<User> = new BehaviorSubject<User>(UserConstructor.new());
  // and other components can subscribe using the public observable
  // (which comes from the userSubject)
  public user: Observable<User> = this.userSubject.asObservable();

  constructor(private http: HttpClient,
              private penaltyService: PenaltyService,
              private locationReservationService: LocationReservationsService,
              private lockerReservationService: LockerReservationService) { }

  userValue(): User {
    return this.userSubject.value;
  }

  logout(): void {
    // put an empty user in userSubject
    this.userSubject.next(UserConstructor.new());
  }

  isLoggedIn(): boolean {
    return this.userSubject.value.augentID !== '';
  }

  updatePassword(from: string, to: string): Observable<any> {
    const body = { from, to, user: this.userValue() };
    return this.http.put(api.changePassword, body);
  }

  /********************************************************
   *   Getters for information about the logged in user   *
   ********************************************************/

  whoAmI(): Observable<User> {
    return this.http.get<User>(api.whoAmI).pipe(tap(next => this.userSubject.next(next)));
  }

  getLocationReservations(): Observable<LocationReservation[]> {
    return this.locationReservationService.getLocationReservationsOfUser(this.userSubject.value.augentID);
  }

  getLockerReservations(): Observable<LockerReservation[]> {
    const v = this.lockerReservationService.getLockerReservationsOfUser(this.userSubject.value.augentID);

    return v.pipe(map<LockerReservation[], LockerReservation[]>((value) => {
      const reservations: LockerReservation[] = [];

      value.forEach(reservation => {
        const obj = LockerReservationConstructor.newFromObj(reservation);
        reservations.push(obj);
      });

      return reservations;
    }));
  }

  getPenalties(): Observable<Penalty[]> {
    return this.penaltyService.getPenaltiesOfUserById(this.userSubject.value.augentID);
  }
}
