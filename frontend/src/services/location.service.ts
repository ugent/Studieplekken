import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {ILocation} from '../interfaces/ILocation';
import {Observable} from 'rxjs';
import {urls} from "../environments/environment";
import {ICalendar} from "../interfaces/ICalendar";

@Injectable()
export class LocationService {
  constructor(private http: HttpClient) {
  }

  getAllLocations(): Observable<ILocation[]> {
    return this.http.get<ILocation[]>(urls.locations);
  }

  getAllLocationsWithoutLockersAndCalendar(): Observable<ILocation[]> {
    return this.http.get<ILocation[]>(urls.locations + '/noLockersAndCalendar');
  }

  getAllLocationsWithoutLockers(): Observable<ILocation[]> {
    return this.http.get<ILocation[]>(urls.locations + '/noLockers');
  }

  getAllLocationsWithoutCalendar(): Observable<ILocation[]> {
    return this.http.get<ILocation[]>(urls.locations + '/noCalendar');
  }

  getLocation(name: string): Observable<ILocation> {
    return this.http.get<ILocation>(urls.locations + '/' + name);
  }

  getLocationWithoutCalendar(name: string): Observable<ILocation> {
    return this.http.get<ILocation>(urls.locations + '/' + name + '/noCalendar');
  }

  getLocationWithoutLockers(name: string): Observable<ILocation> {
    return this.http.get<ILocation>(urls.locations + '/' + name + '/noLockers');
  }

  getLocationWithoutLockersAndCalendar(name: string): Observable<ILocation> {
    return this.http.get<ILocation>(urls.locations + '/' + name + '/noLockersAndCalendar');
  }

  addLocation(location: ILocation): Observable<any> {
    return this.http.post(urls.locations, JSON.stringify(location), {headers: {'Content-Type': 'application/json'}});
  }

  saveLocation(name: string, location: ILocation): Observable<any> {
    return this.http.put(urls.locations + "/"+name, JSON.stringify(location), {headers: {'Content-Type': 'application/json'}});
  }

  deleteLocation(name: string): Observable<any> {
    return this.http.delete(urls.locations + "/" + name);
  }

  addCalendarDays(name: string, calendar: ICalendar): Observable<any> {
    return this.http.post(urls.locations + "/" + name, JSON.stringify(calendar), {headers: {'Content-Type': 'application/json'}});
  }

  deleteCalendarDays(name: string, startdate: string, enddate: string): Observable<any> {
    return this.http.delete(urls.locations + "/" + name + "/" + startdate + "/" + enddate);
  }

  getScannersFromLocation(locationName: string): Observable<any>{
    return this.http.get(urls.scanners+ locationName);
  }

  updateScanners(locationName: string, scanners: string[]) {
    return this.http.post(urls.scanners+ locationName, JSON.stringify(scanners), {headers: {'Content-Type': 'application/json'}});
  }

  /*updateLocker(name: string, locker: ILocker): Observable<any> {
    return this.http.put(urls.locations + '/' + name + '/locker/' + locker.number, JSON.stringify(locker), {headers: {'Content-Type': 'application/json'}});
  }*/

  getAllAuthenticatedLocations(mail: string) {
    return this.http.get<string[]>(urls.scanlocations + mail);
  }
}
