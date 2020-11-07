import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {api} from '../../../../environments/environment';
import {Location} from '../../../shared/model/Location';
import {LocationTag} from '../../../shared/model/LocationTag';
import { map } from 'rxjs/internal/operators/map';

@Injectable({
  providedIn: 'root'
})
export class LocationService {

  constructor(private http: HttpClient) { }

  /***********************************************************
   *   API calls for CRUD operations with public.LOCATIONS   *
   ***********************************************************/

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

  getNumberOfReservationsNow(locationName: string): Observable<number> {
    return this.http.get<any>(api.locationReservationCount.replace('{location}', locationName))
              .pipe(map(s => s.amount));
  }

  /***************************************************************
   *   API calls for CRUD operations with public.LOCATION_TAGS   *
   ***************************************************************/

  setupTagsForLocation(locationName: string, tags: LocationTag[]): Observable<any> {
    return this.http.put(api.setupTagsForLocation.replace('{locationName}', locationName), tags.map(v => v.tagId));
  }

}
