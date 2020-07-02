import {IDate} from '../../interfaces/IDate';
import {BehaviorSubject, Observable, of} from 'rxjs';
import {urls} from '../../environments/environment';
import {dateToString} from '../../interfaces/CustomDate';
import {ILocationReservation} from '../../interfaces/ILocationReservation';
import {ILocation} from '../../interfaces/ILocation';
import {IUser} from '../../interfaces/IUser';

export default class LocationReservationServiceStub {

  MAX_PENALTY_POINTS: BehaviorSubject<number>;

  reservations = [{ attended: null,
    'user': {
      'lastName': 'Bloemhof',
      'firstName': 'Lore',
      'mail': 'lb@HOGent.be',
      'password': 'lore',
      'institution': 'HOGent',
      'augentID': '2',
      'penaltyPoints': null,
      'birthDate': {'year': 1995, 'month': 1, 'day': 1, 'hrs': 0, 'min': 0, 'sec': 0, nextDay(): void {
        }},
      'barcode': '',
      'mifareID': '',
      'roles': ['STUDENT']
    }, 'location': {
      'name': 'Therminal',
      'address': 'Hoveniersberg 24, 9000 Gent',
      startPeriodLocker: null,
      endPeriodLocker: null,
      'numberOfSeats': 500,
      'numberOfLockers': 150,
      'mapsFrame': 'test',
      'descriptions': 'test1',
      'imageUrl': '<iframe src="https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d2508.4611638148913!2d3.725643615956718!3d51.044572052257635!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x47c371505918a89f%3A0x215e897a80bba60d!2sStudentenhuis%20De%20Therminal!5e0!3m2!1snl!2sbe!4v1582295840769!5m2!1snl!2sbe" width="600" height="450" frameborder="0" style="border:0;" allowfullscreen=""></iframe>',
      'startPeriodLockers': null,
      'endPeriodLockers': null,
      'calendar': [{
        'date': {'year': 2020, 'month': 4, 'day': 10, 'hrs': 0, 'min': 0, 'sec': 0},
        'openingHour': {'hours': 10, 'minutes': 0},
        'closingHour': {'hours': 18, 'minutes': 0},
        'openForReservationDate': {'year': 2020, 'month': 4, 'day': 10, 'hrs': 6, 'min': 0, 'sec': 0}
      }],
      'lockers': []
    }, 'date': {'year': 1970, 'month': 1, 'day': 1, 'hrs': 0, 'min': 0, 'sec': 0}
  } as ILocationReservation ,{
    attended: null,
    'user': {
      'lastName': 'Bloemhof',
      'firstName': 'Jan',
      'mail': 'lb@HOGent.be',
      'password': 'lore',
      'institution': 'HOGent',
      'augentID': '2',
      'penaltyPoints': null,
      'birthDate': {'year': 1995, 'month': 1, 'day': 1, 'hrs': 0, 'min': 0, 'sec': 0, nextDay(): void {
        }},
      'barcode': '',
      'mifareID': '',
      'roles': ['STUDENT']
    }, 'location': {
      'name': 'Therminal',
      'address': 'Hoveniersberg 24, 9000 Gent',
      startPeriodLocker: null,
      endPeriodLocker: null,
      'numberOfSeats': 500,
      'numberOfLockers': 150,
      'mapsFrame': 'test',
      'descriptions': 'test1',
      'imageUrl': '<iframe src="https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d2508.4611638148913!2d3.725643615956718!3d51.044572052257635!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x47c371505918a89f%3A0x215e897a80bba60d!2sStudentenhuis%20De%20Therminal!5e0!3m2!1snl!2sbe!4v1582295840769!5m2!1snl!2sbe" width="600" height="450" frameborder="0" style="border:0;" allowfullscreen=""></iframe>',
      'startPeriodLockers': null,
      'endPeriodLockers': null,
      'calendar': [{
        'date': {'year': 2020, 'month': 4, 'day': 10, 'hrs': 0, 'min': 0, 'sec': 0},
        'openingHour': {'hours': 10, 'minutes': 0},
        'closingHour': {'hours': 18, 'minutes': 0},
        'openForReservationDate': {'year': 2020, 'month': 4, 'day': 10, 'hrs': 6, 'min': 0, 'sec': 0}
      }],
      'lockers': []
    }, 'date': {'year': 1970, 'month': 1, 'day': 1, 'hrs': 0, 'min': 0, 'sec': 0}
  } as ILocationReservation]  as ILocationReservation[];

  constructor() {
    this.MAX_PENALTY_POINTS = new BehaviorSubject<number>(10);
  }

  getCountReservedSeats(location: string, date: IDate): Observable<number> {
    return of(100);
  }

  getAllLocationReservationsOfUser(augentID: string): Observable<ILocationReservation[]> {
    return of(this.reservations);
  }

  getAllLocationReservationsOfUserByName(name: string): Observable<ILocationReservation[]> {
    return of([]);
  }

  getAllLocationReservationsOfLocation(location: ILocation): Observable<ILocationReservation[]> {
    return of([]);
  }

  getAllLocationReservationsOfLocationByName(nameOfLocation: string): Observable<ILocationReservation[]> {
    return of([]);
  }

  getLocationReservation(user: IUser, date: IDate): Observable<ILocationReservation> {
    return of(null);
  }

  getAllAbsentLocationReservations(locationName: string, date: IDate): Observable<ILocationReservation[]> {
    return of([]);
  }

  getAllPresentLocationReservations(locationName: string, date: IDate): Observable<ILocationReservation[]> {
    return of([]);
  }
  sendMailsAndAddPenaltyPoints(names: Set<string>) {

  }

  addLocationReservation(request) {

  }
}
