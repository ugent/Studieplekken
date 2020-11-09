import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders, HttpParams} from '@angular/common/http';
import {api} from '../endpoints';
import {LocationReservation} from '../../../shared/model/LocationReservation';
import {Observable} from 'rxjs';
import {toISODateString, typeScriptDateToCustomDate} from '../../../shared/model/helpers/CustomDate';

@Injectable({
  providedIn: 'root'
})
export class LocationReservationsService {

  constructor(private http: HttpClient) {
  }

  getLocationReservationsOfUser(id: string): Observable<LocationReservation[]> {
    const params = new HttpParams().set('id', id);
    return this.http.get<LocationReservation[]>(api.locationReservationsOfUser, {params});
  }

  getLocationReservationsOfLocation(locationName: string, pastReservations: boolean): Observable<LocationReservation[]> {
    const params = new HttpParams()
      .set('locationName', locationName)
      .set('pastReservations', String(pastReservations));
    return this.http.get<LocationReservation[]>(api.locationReservationsOfLocation, {params});
  }

  getLocationReservationsOfLocationFrom(locationName: string, start: Date,
                                        pastReservations: boolean): Observable<LocationReservation[]> {
    const params = new HttpParams()
      .set('locationName', locationName)
      .set('start', toISODateString(typeScriptDateToCustomDate(start)))
      .set('pastReservations', String(pastReservations));
    return this.http.get<LocationReservation[]>(api.locationReservationsOfLocationFrom, {params});
  }

  getLocationReservationsOfLocationUntil(locationName: string, end: Date,
                                         pastReservations: boolean): Observable<LocationReservation[]> {
    const params = new HttpParams()
      .set('locationName', locationName)
      .set('end', toISODateString(typeScriptDateToCustomDate(end)))
      .set('pastReservations', String(pastReservations));
    return this.http.get<LocationReservation[]>(api.locationReservationsOfLocationUntil, {params});
  }

  getLocationReservationsOfLocationFromAndUntil(locationName: string, start: Date, end: Date,
                                                pastReservations: boolean): Observable<LocationReservation[]> {
    const params = new HttpParams()
      .set('locationName', locationName)
      .set('start', toISODateString(typeScriptDateToCustomDate(start)))
      .set('end', toISODateString(typeScriptDateToCustomDate(end)))
      .set('pastReservations', String(pastReservations));
    return this.http.get<LocationReservation[]>(api.locationReservationsOfLocationFromAndUntil, {params});
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
}
