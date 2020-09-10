import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {api} from '../../../../environments/environment';
import {Location} from '../../../shared/model/Location';

@Injectable({
  providedIn: 'root'
})
export class LocationService {

  constructor(private http: HttpClient) { }

  getLocations(): Observable<Location[]> {
    return this.http.get<Location[]>(api.locations);
  }

  getLocation(locationName: string): Observable<Location> {
    return this.http.get<Location>(api.location.replace('{locationName}', locationName));
  }

  addLocation(location: Location): Observable<any> {
    return this.http.post(api.addLocation, location);
  }

  updateLocation(locationName: string, location: Location): Observable<any> {
    return this.http.put<void>(api.updateLocation.replace('{locationName}', locationName), location);
  }

  deleteLocation(locationName: string): Observable<any> {
    return this.http.delete(api.deleteLocation.replace('{locationName}', locationName));
  }

  getNumberOfReservations(location: Location): Observable<number> {
    return this.http.get<number>(api.numberOfReservations.replace('{locationName}', location.name));
  }
}
