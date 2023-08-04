import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Location, LocationConstructor } from '../../../../model/Location';
import { api } from '../endpoints';
import { map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class ScanningService {
  constructor(private http: HttpClient) {}

  getLocationsToScan(): Observable<Location[]> {
    return this.http.get<Location[]>(api.scanningLocations).pipe(map(v => v.map(o => LocationConstructor.newFromObj(o))));
  }
}
