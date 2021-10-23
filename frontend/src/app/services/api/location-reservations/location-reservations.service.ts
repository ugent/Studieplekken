import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { api } from '../endpoints';
import { LocationReservation } from '../../../shared/model/LocationReservation';
import { combineLatest, Observable } from 'rxjs';
import { Timeslot } from 'src/app/shared/model/Timeslot';
import { map } from 'rxjs/internal/operators/map';
import { of } from 'rxjs/internal/observable/of';
import { Pair } from '../../../shared/model/helpers/Pair';

@Injectable({
  providedIn: 'root',
})
export class LocationReservationsService {
  constructor(private http: HttpClient) {}

  getLocationReservationsOfUser(id: string): Observable<LocationReservation[]> {
    if (id === '') {
      return of<LocationReservation[]>([]);
    }
    const params = new HttpParams().set('id', id);
    return this.http
      .get<LocationReservation[]>(api.locationReservationsOfUser, { params })
      .pipe(map((ls) => ls.map(LocationReservation.fromJSON.bind(this))));
  }

  getLocationReservationsOfTimeslot(
    timeslot: Timeslot
  ): Observable<LocationReservation[]> {
    return this.http
      .get<unknown[]>(
        api.locationReservationsOfLocation
          .replace('{seqnr}', `${timeslot.timeslotSequenceNumber}`)
      )
      .pipe(map((ls) => ls.map(LocationReservation.fromJSON.bind(this))));
  }

  deleteLocationReservations(
    locationReservations: LocationReservation[]
  ): Observable<Array<void>> {
    return locationReservations.length > 0
      ? combineLatest(
          locationReservations.map((l) => this.deleteLocationReservation(l))
        )
      : of<Array<void>>([]);
  }

  deleteLocationReservation(
    locationReservation: LocationReservation
  ): Observable<void> {
    const options = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
      }),
      body: locationReservation,
    };
    return this.http.delete<void>(api.deleteLocationReservation, options);
  }

  postLocationReservations(
    locationReservations: LocationReservation[]
  ): Observable<void[]> {
    return locationReservations.length > 0
      ? combineLatest(
          locationReservations.map((l) => this.postLocationReservation(l))
        )
      : of<Array<void>>([]);
  }

  postLocationReservation(
    locationReservation: LocationReservation
  ): Observable<void> {
    return this.http.post<void>(
      api.addLocationReservation,
      locationReservation.timeslot,
      { withCredentials: true }
    );
  }

  postLocationReservationAttendance(
    locationReservation: LocationReservation,
    attended: boolean
  ): Observable<void> {
    return this.http.post<void>(
      api.updateAttendance2
        .replace('{userid}', locationReservation.user.userId)
        /*.replace(
          '{date}',
          locationReservation.timeslot.timeslotDate.format('YYYY-MM-DD')
        )*/
        .replace('{seqnr}', `${locationReservation.timeslot.timeslotSequenceNumber}`),
      { attended },
      { withCredentials: true }
    );
  }

  setAllNotScannedAsUnattended(
    timeslot: Timeslot
  ): Observable<void> {
    return this.http.put<void>(
      api.locationReservationsOfNotScannedUsers,
      timeslot
    );
  }
}
