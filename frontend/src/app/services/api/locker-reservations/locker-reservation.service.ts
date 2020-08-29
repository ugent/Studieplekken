import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {LockerReservation} from '../../../shared/model/LockerReservation';
import {api} from '../../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class LockerReservationService {

  constructor(private http: HttpClient) { }

  getLockerReservationsOfUser(id: string): Observable<LockerReservation[]> {
    const params = new HttpParams().set('id', id);
    return this.http.get<LockerReservation[]>(api.lockerReservationsOfUser, { params });
  }

  getLockerReservationsOfLocation(locationName: string, pastReservations: boolean): Observable<LockerReservation[]> {
    const params = new HttpParams()
      .set('locationName', locationName)
      .set('pastReservations', String(pastReservations));
    return this.http.get<LockerReservation[]>(api.lockerReservationsOfLocation, { params });
  }

  getLockerReservationsOfLocationFrom(locationName: string, start: Date,
                                      pastReservations: boolean): Observable<LockerReservation[]> {
    const params = new HttpParams()
      .set('locationName', locationName)
      .set('start', start.toDateString())
      .set('pastReservations', String(pastReservations));
    return this.http.get<LockerReservation[]>(api.lockerReservationsOfLocationFrom, { params });
  }

  getLockerReservationsOfLocationUntil(locationName: string, end: Date,
                                       pastReservations: boolean): Observable<LockerReservation[]> {
    const params = new HttpParams()
      .set('locationName', locationName)
      .set('end', end.toDateString())
      .set('pastReservations', String(pastReservations));
    return this.http.get<LockerReservation[]>(api.lockerReservationsOfLocationUntil, { params });
  }

  getLockerReservationsOfLocationFromAndUntil(locationName: string, start: Date, end: Date,
                                              pastReservations: boolean): Observable<LockerReservation[]> {
    const params = new HttpParams()
      .set('locationName', locationName)
      .set('start', start.toDateString())
      .set('end', end.toDateString())
      .set('pastReservations', String(pastReservations));
    return this.http.get<LockerReservation[]>(api.lockerReservationsOfLocationFromAndUntil, { params });
  }

  deleteLockerReservation(lockerReservation: LockerReservation): Observable<any> {
    const options = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json'
      }),
      body: lockerReservation
    };
    return this.http.delete(api.deleteLockerReservation, options);
  }
}
