import {Observable, of} from 'rxjs';
import {ILocation} from '../../interfaces/ILocation';
import {urls} from "../../environments/environment";

export default class LocationServiceStub{

  locations : ILocation[];

  constructor(){
    this.locations = [{
      'name': 'Faculteit Bio-Ingenieurs-wetenschappen',
      'address': 'Resto Agora, Coupure 653 (gelijkvloers gebouw E)',
      'numberOfSeats': 400,
      'numberOfLockers': 67,
      startPeriodLockers:null,
      endPeriodLockers: null,
      'mapsFrame': 'https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d2508.461346449728!2d3.725643616041962!3d51.04456867956177!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x47c371505918a89f%3A0x215e897a80bba60d!2sStudentenhuis%20De%20Therminal!5e0!3m2!1snl!2sbe!4v1582414683281!5m2!1snl!2sbe',
      'descriptions': '',
      'imageUrl': 'https://www.schamper.ugent.be/files/styles/article/public/images/icoon_UGent_BW_NL_RGB_2400_kleur.png?itok=4mHGEwLM',
      'calendar': [{
        'date': {'year': 2020, 'month': 4, 'day': 10, 'hrs': 10, 'min': 0, 'sec': 0},
        'openingHour': {'hours': 10, 'minutes': 0},
        'closingHour': {'hours': 18, 'minutes': 0},
        'openForReservationDate': {'year': 2020, 'month': 4, 'day': 10, 'hrs': 6, 'min': 0, 'sec': 0}
      }],
      'lockers': []
    }] as ILocation[]
  }
  getAllLocations(): Observable<ILocation[]> {
    return of(this.locations);
  }

  getLocation(name: string): Observable<ILocation> {
    return of(this.locations[0]);
  }

  addLocation(loc: ILocation): Observable<any>{
    return of(loc);
  }

  saveLocation(name: string, location: ILocation): void {

  }

  deleteLocation(name: string): Observable<any> {
    return of(null);
  }

  updateLocker(location, locker){

  }

  getLocationWithoutCalendar(name: string): Observable<ILocation>{
    return of(this.locations[0]);
  }

  getAllLocationsWithoutLockersAndCalendar(): Observable<ILocation[]> {
    return of(this.locations);
  }

  getAllLocationsWithoutLockers(): Observable<ILocation[]> {
    return of(this.locations);
  }

  getAllLocationsWithoutCalendar(): Observable<ILocation[]> {
    return of(this.locations);
  }

  getAllAuthenticatedLocations(mail: string) {
   // return this.http.get<string[]>(urls.scanlocations + mail);
    return of(this.locations);
  }
}
