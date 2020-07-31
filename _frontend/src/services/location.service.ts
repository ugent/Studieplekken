import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {ILocation} from '../interfaces/ILocation';
import {Observable} from 'rxjs';
import {urls} from "../environments/environment";
import {ICalendar} from "../interfaces/ICalendar";
import {IDay} from "../interfaces/IDay";

@Injectable()
export class LocationService {
  constructor(private http: HttpClient) {
  }

  getAllLocations(): Observable<ILocation[]> {
    return this.http.get<ILocation[]>(urls.locations);
  }

  getLocation(name: string): Observable<ILocation> {
    return this.http.get<ILocation>(urls.locations + '/' + name);
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

  getCalendarDays(locationName: string): Observable<IDay[]> {
    return this.http.get<IDay[]>(urls.locations + "/calendar" + locationName);
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
