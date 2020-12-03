import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {api} from '../endpoints';
import {Location} from '../../../shared/model/Location';
import {LocationTag} from '../../../shared/model/LocationTag';
import { map } from 'rxjs/internal/operators/map';
import {Cache} from '../../../shared/cache/Cache';
import { filter } from 'rxjs/operators';

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
    console.log('getLocations');
    return this.locationCache.getAllValues(api.locations);
  }

  getUnapprovedLocations(): Observable<Location[]> {
    console.log('getUnapprovedLocations');
    return this.locationCache.getAllValues(api.locationsUnapproved);
  }


  getLocation(locationName: string, invalidateCache: boolean = false): Observable<Location> {
    console.log('getLocation');
    const url = api.location.replace('{locationName}', locationName);
    return this.locationCache.getValue(locationName, url, invalidateCache);
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
    console.log('getNumberOfReservationsNow');
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
