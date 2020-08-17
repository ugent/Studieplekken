import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable, of} from 'rxjs';
import {api} from '../../../../environments/environment';
import {Location} from '../../../shared/model/Location';
import {tap} from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class LocationService {
  locations: Map<string, Location> = new Map<string, Location>();

  constructor(private http: HttpClient) { }

  getLocations(): Observable<Location[]> {
    // clear all locations ...
    this.locations.clear();

    return this.http.get<Location[]>(api.locations)
      // ... and add the new locations
      .pipe(tap<Location[]>(locations => {
        for (const location of locations) {
          this.locations.set(location.name, location);
        }
      }));
  }

  getLocation(locationName: string): Observable<Location> {
    if (this.locations.has(locationName)) {
      return of(this.locations.get(locationName));
    } else {
      return this.http.get<Location>(api.location.replace('{locationName}', locationName))
        .pipe(tap<Location>(l => {
          this.locations.set(locationName, l);
        }));
    }
  }

  updateLocation(locationName: string, location: Location): Observable<void> {
    const ret = this.http.put<void>(api.updateLocation.replace('{locationName}', locationName), location);

    // let an update follow with a getLocations() so that the new information is retrieved
    ret.subscribe(next => {
      this.getLocations();
    });

    return ret;
  }

  getNumberOfReservations(location: Location): Observable<number> {
    return this.http.get<number>(api.numberOfReservations.replace('{locationName}', location.name));
  }
}
