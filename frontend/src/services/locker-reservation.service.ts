import {HttpClient} from "@angular/common/http";
import {ILockerReservation} from "../interfaces/ILockerReservation";
import {Observable} from "rxjs";
import {IUser} from "../interfaces/IUser";
import {IDate} from "../interfaces/IDate";
import {Injectable} from "@angular/core";
import {urls} from "../environments/environment";
import {ILocation} from "../interfaces/ILocation";
import {dateToString} from "../interfaces/CustomDate";

@Injectable()
export class LockerReservationService {
  constructor(private http: HttpClient) {
  }

  getAllLockerReservationsOfUser(augentID: string): Observable<ILockerReservation[]> {
    return this.http.get<ILockerReservation[]>(urls.lockerReservation + '/user/' + augentID);
  }

  getAllLockerReservationsOfUserByName(name: string): Observable<ILockerReservation[]> {
    return this.http.get<ILockerReservation[]>(urls.lockerReservation + '/userByName/' + name);
  }

  getAllLockerReservationsOfLocation(locationName: string): Observable<ILockerReservation[]> {
    return this.http.get<ILockerReservation[]>(urls.lockerReservation + '/location/' + locationName);
  }

  getAllOngoingLockerReservationsOfLocation(location: ILocation): Observable<ILockerReservation[]> {
    return this.http.get<ILockerReservation[]>(urls.lockerReservation + '/location/' + location.name + '/ongoing');
  }

  getNumberOfLockersInUseOfLocation(location: ILocation): Observable<number> {
    return this.http.get<number>(urls.lockerReservation + '/location/' + location.name + '/numberInUse');
  }

  getLockerReservation(user: IUser, date: IDate): Observable<ILockerReservation> {
    return this.http.get<ILockerReservation>(urls.lockerReservation + '/' + user.augentID + '/' + date.toString());
  }

  deleteLockerReservation(lockerReservation): Observable<any> {
    return this.http.delete(urls.lockerReservation + '/' + lockerReservation.owner.augentID + '/' + lockerReservation.locker.id + '/' +
      dateToString(lockerReservation.startDate) + '/' + dateToString(lockerReservation.endDate));
  }

  addLockerReservation(location: string, augentID: string): Observable<any> {
    return this.http.post(urls.lockerReservation + '/' + location + '/' + augentID, null);
  }

  updateLockerReservation(lockerReservation: ILockerReservation): Observable<any> {
    return this.http.put(urls.lockerReservation, JSON.stringify(lockerReservation), {
      headers: {'Content-Type': 'application/json'}
    });
  }
}
