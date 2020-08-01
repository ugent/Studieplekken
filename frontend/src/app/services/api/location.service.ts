import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {api} from '../../../environments/environment';
import {Location} from '../../shared/model/Location';

@Injectable({
  providedIn: 'root'
})
export class LocationService {

  constructor(private http: HttpClient) { }

  getLocations(): Observable<Location[]> {
    return this.http.get<Location[]>(api.locations);
  }

  getNumberOfReservations(location: Location): Observable<number> {
    return this.http.get<number>(api.numberOfReservations.replace('{locationName}', location.name));
  }
}
