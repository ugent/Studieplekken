import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders, HttpParams} from '@angular/common/http';
import {api} from '../endpoints';
import {LocationReservation} from '../../../shared/model/LocationReservation';
import {combineLatest, Observable} from 'rxjs';
import { getTimeslotsOnDay, Timeslot } from 'src/app/shared/model/Timeslot';
import { CalendarPeriod } from 'src/app/shared/model/CalendarPeriod';
import { map } from 'rxjs/internal/operators/map';
import { tap } from 'rxjs/internal/operators/tap';
import { of } from 'rxjs/internal/observable/of';
import { Moment } from 'moment';

@Injectable({
  providedIn: 'root'
})
export class LocationReservationsService {

  constructor(private http: HttpClient) {
  }

  getLocationReservationsOfUser(id: string): Observable<LocationReservation[]> {
    const params = new HttpParams().set('id', id);
    return this.http.get<LocationReservation[]>(api.locationReservationsOfUser, { params })
                                                          .pipe(map(ls => ls.map(LocationReservation.fromJSON)));
  }

  getLocationReservationsOfTimeslot(timeslot: Timeslot): Observable<LocationReservation[]> {
    return this.http.get<any[]>(api.locationReservationsOfLocation
                                                  .replace('{calendarid}', `${timeslot.calendarId}`)
                                                  .replace('{date}', timeslot.timeslotDate.format('YYYY-MM-DD'))
                                                  .replace('{seqnr}', `${timeslot.timeslotSeqnr}`))
                                                  .pipe(map(ls => ls.map(LocationReservation.fromJSON)));
  }

  getLocationReservationsOfDay(calendarPeriod: CalendarPeriod, date: Moment): Observable<LocationReservation[]> {
    return combineLatest(getTimeslotsOnDay(calendarPeriod, date)
                .map(p => this.getLocationReservationsOfTimeslot(p)))
                .pipe(map(s => s.reduce((a, b) => [...a, ...b])));
  }


  deleteLocationReservations(locationReservations: LocationReservation[]): Observable<void[]> {
    return locationReservations.length > 0 ? combineLatest(locationReservations.map(l => this.deleteLocationReservation(l))) : of([]);
   }

  deleteLocationReservation(locationReservation: LocationReservation): Observable<any> {
    const options = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json'
      }),
      body: locationReservation
    };
    return this.http.delete(api.deleteLocationReservation, options);
  }

  postLocationReservations(locationReservations: LocationReservation[]): Observable<void[]> {
    return locationReservations.length > 0 ? combineLatest(locationReservations.map(l => this.postLocationReservation(l))) : of([]);
   }

  postLocationReservation(locationReservation: LocationReservation): Observable<void> {
    return this.http.post<void>(api.addLocationReservation, locationReservation.timeslot, {withCredentials: true});
  }

  postLocationReservationAttendance(locationReservation: LocationReservation, attended: boolean): Observable<void> {
    return this.http.post<void>(api.updateAttendance.replace('{userid}', locationReservation.user.augentID)
                                                          .replace('{calendarid}', `${locationReservation.timeslot.calendarId}`)
                                                          .replace('{date}', locationReservation.timeslot.timeslotDate.format('YYYY-MM-DD'))
                                                          .replace('{seqnr}', `${locationReservation.timeslot.timeslotSeqnr}`)
                                    , {attended}, {withCredentials: true});
  }
}
