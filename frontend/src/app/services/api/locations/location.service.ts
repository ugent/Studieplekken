import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {api} from '../endpoints';
import {Location, LocationConstructor} from '../../../shared/model/Location';
import {LocationTag} from '../../../shared/model/LocationTag';
import { map } from 'rxjs/internal/operators/map';
import {Cache} from '../../../shared/cache/Cache';
import {Moment} from 'moment';
import {Pair} from '../../../shared/model/helpers/Pair';
import {User} from '../../../shared/model/User';

@Injectable({
  providedIn: 'root'
})
export class LocationService {

  constructor(private http: HttpClient) { }

  locationCache: Cache<number, Location> = new Cache<number, Location>(this.http,
                                                                            (arg: Location) => arg.locationId,
                                                                            (json: any) => LocationConstructor.newFromObj(json));

  /***********************************************************
   *   API calls for CRUD operations with public.LOCATIONS   *
   ***********************************************************/

  getLocations(): Observable<Location[]> {
    return this.locationCache.getAllValues(api.locations);
  }

  /**
   * Special treatment, because otherwise this messes with the cache
   */
  getUnapprovedLocations(): Observable<Location[]> {
    return this.http.get<Location[]>(api.locationsUnapproved);
  }

  getLocation(locationId: number, invalidateCache: boolean = false): Observable<Location> {
    const url = api.location.replace('{locationId}', String(locationId));
    return this.locationCache.getValue(locationId, url, invalidateCache);
  }

  getAllLocationNextReservableFroms(): Observable<Pair<string, Moment>[]> {
    return this.http.get<Pair<string, Moment>[]>(api.allReservableFroms);
  }

  getVolunteers(locationId: number): Observable<User[]> {
    const url = api.locationVolunteers.replace('{locationId}', String(locationId));
    return this.http.get<User[]>(url);
  }

  addVolunteer(locationId: number, userId: string): Observable<void> {
    const url = api.addLocationVolunteer.replace('{locationId}', String(locationId)).replace("{userId}", String(userId));
    return this.http.post<void>(url, {});
  }

  deleteVolunteer(locationId: number, userId: string): Observable<void> {
    const url = api.addLocationVolunteer.replace('{locationId}', String(locationId)).replace("{userId}", String(userId));
    return this.http.delete<void>(url, {});
  }


  addLocation(location: Location): Observable<any> {
    return this.http.post(api.addLocation, location);
  }

  updateLocation(locationId: number, location: Location): Observable<any> {
    return this.http.put<void>(api.updateLocation.replace('{locationId}', String(locationId)), location);
  }

  approveLocation(location: Location, approval: boolean): Observable<any> {
    console.log(location);
    return this.http.put<void>(api.approveLocation.replace('{locationId}', String(location.locationId)), {location, approval});
  }

  deleteLocation(locationId: number): Observable<any> {
    return this.http.delete(api.deleteLocation.replace('{locationId}', String(locationId)));
  }

  getNumberOfReservationsNow(locationId: number): Observable<number> {
    return this.http.get<any>(api.locationReservationCount.replace('{locationId}', String(locationId)))
              .pipe(map(s => s.amount));
  }

  /***************************************************************
   *   API calls for CRUD operations with public.LOCATION_TAGS   *
   ***************************************************************/

  setupTagsForLocation(locationId: number, tags: LocationTag[]): Observable<any> {
    return this.http.put(api.setupTagsForLocation.replace('{locationId}', String(locationId)), tags.map(v => v.tagId));
  }

}
