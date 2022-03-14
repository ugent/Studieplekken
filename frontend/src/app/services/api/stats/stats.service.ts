import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { LocationStat, LocationStatConstructor } from '../../../shared/model/LocationStat';
import { api } from '../endpoints';
import { map } from 'rxjs/internal/operators/map';

@Injectable({
  providedIn: 'root',
})
export class StatsService {
  constructor(private http: HttpClient) {
  }

  getStats(): Observable<LocationStat[]> {
    return this.http.get<LocationStat[]>(api.getStats).pipe(map(x => x.map(LocationStatConstructor.newFromObj)));
  }

}
