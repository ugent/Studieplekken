import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { LockerReservation } from '@/model/LockerReservation';
import { api } from '../endpoints';

@Injectable({
  providedIn: 'root',
})
export class LockersService {
  constructor(private http: HttpClient) {}

  getLockersStatusesOfLocation(
    locationId: number
  ): Observable<LockerReservation[]> {
    return this.http.get<LockerReservation[]>(
      api.lockersStatusesOfLocation.replace('{locationId}', String(locationId))
    );
  }

  updateLockerReservation(
    lockerReservation: LockerReservation
  ): Observable<void> {
    return this.http.put<void>(api.updateLockerReservation, lockerReservation);
  }
}
