import {Observable, of} from 'rxjs';
import {ILockerReservation} from '../../interfaces/ILockerReservation';
import {IUser} from '../../interfaces/IUser';
import {IDate} from '../../interfaces/IDate';
import {ILocation} from '../../interfaces/ILocation';

export default class LockerReservationServiceStub {

  lockerReservations: ILockerReservation[];

  constructor() {

    this.lockerReservations = [{
      'owner': null,
      'locker': {
        number: 0,
        id: "0",
        location: "test",
        studentLimit:0
      },
      keyPickedUp: null,
      keyBroughtBack: null,
      'startDate': {day: 1, sec:1 , month:1, year: 1, hrs: 1, min:1},
      'endDate': {day: 1, sec:1 , month:1, year: 1, hrs: 1, min:1}
    }];


  }

  getAllLockerReservationsOfUser(augentID: string): Observable<ILockerReservation[]> {
    return of(this.lockerReservations);
  }

  getAllLockerReservationsOfUserByName(name: string): Observable<ILockerReservation[]> {
    return of(this.lockerReservations);
  }

  getAllLockerReservationsOfLocation(locationName: string): Observable<ILockerReservation[]> {
    return of(this.lockerReservations);
  }

  getLockerReservation(user: IUser, date: IDate): Observable<ILockerReservation> {
    return of(this.lockerReservations[0]);
  }

  getNumberOfLockersInUseOfLocation(location: ILocation): Observable<number> {
    return of(5);
  }

  getAllOngoingLockerReservationsOfLocation(location: ILocation): Observable<ILockerReservation[]> {
    //return this.http.get<ILockerReservation[]>(urls.lockerReservation + '/location/' + location.name + '/ongoing');
    return of(this.lockerReservations);
  }
}
