import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {api} from '../endpoints';
import {Location} from '../../../shared/model/Location';
import {LocationTag} from '../../../shared/model/LocationTag';
import { map } from 'rxjs/internal/operators/map';
import {Cache} from '../../../shared/cache/Cache';
import {Moment} from 'moment';
import {Pair} from '../../../shared/model/helpers/Pair';

@Injectable({
  providedIn: 'root'
})
export class LocationService {

  constructor(private http: HttpClient) { }

  locationCache: Cache<string, Location> = new Cache<string, Location>(this.http, (arg: Location) => arg.name);

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


  getLocation(locationName: string, invalidateCache: boolean = false): Observable<Location> {
    const url = api.location.replace('{locationName}', locationName);
    return this.locationCache.getValue(locationName, url, invalidateCache);
  }

  getAllLocationNextReservableFroms(): Observable<Pair<string, Moment>[]> {
    return this.http.get<Pair<string, Moment>[]>(api.allReservableFroms);
  }

  addLocation(location: Location): Observable<any> {
    return this.http.post(api.addLocation, location);
  }

  updateLocation(locationName: string, location: Location): Observable<any> {
    return this.http.put<void>(api.updateLocation.replace('{locationName}', locationName), location);
  }

  approveLocation(location: Location, approval: boolean): Observable<any> {
    return this.http.put<void>(api.approveLocation.replace('{locationName}', location.name), {location, approval});
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
