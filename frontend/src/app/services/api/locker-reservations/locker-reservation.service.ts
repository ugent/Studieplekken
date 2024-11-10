import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { LockerReservation } from '@/model/LockerReservation';
import { api } from '../endpoints';

@Injectable({
  providedIn: 'root',
})
export class LockerReservationService {
  constructor(private http: HttpClient) {}

  getLockerReservationsOfUser(id: string): Observable<LockerReservation[]> {
    const params = new HttpParams().set('id', id);
    return this.http.get<LockerReservation[]>(api.lockerReservationsOfUser, {
      params,
    });
  }

  getLockerReservationsOfLocation(
    locationName: string,
    pastReservations: boolean
  ): Observable<LockerReservation[]> {
    const params = new HttpParams()
      .set('locationName', locationName)
      .set('pastReservations', String(pastReservations));
    return this.http.get<LockerReservation[]>(
      api.lockerReservationsOfLocation,
      { params }
    );
  }

  deleteLockerReservation(
    lockerReservation: LockerReservation
  ): Observable<void> {
    const options = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
      }),
      body: lockerReservation,
    };
    return this.http.delete<void>(api.deleteLockerReservation, options);
  }
}
