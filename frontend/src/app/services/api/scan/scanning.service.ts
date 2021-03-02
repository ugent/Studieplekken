import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Location} from '../../../shared/model/Location';
import {api} from '../endpoints';

@Injectable({
  providedIn: 'root'
})
export class ScanningService {

  constructor(private http: HttpClient) { }

  getLocationsToScan(): Observable<Location[]> {
    return this.http.get<Location[]>(api.scanningLocations);
  }

}
