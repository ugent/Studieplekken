import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {LockerReservation} from '../../../shared/model/LockerReservation';
import {api} from '../endpoints';

@Injectable({
  providedIn: 'root'
})
export class LockersService {

  constructor(private http: HttpClient) {
  }

  getLockersStatusesOfLocation(locationId: number): Observable<LockerReservation[]> {
    return this.http.get<LockerReservation[]>(api.lockersStatusesOfLocation
      .replace('{locationId}', String(locationId)));
  }

  updateLockerReservation(lockerReservation: LockerReservation): Observable<any> {
    return this.http.put(api.updateLockerReservation, lockerReservation);
  }
}
