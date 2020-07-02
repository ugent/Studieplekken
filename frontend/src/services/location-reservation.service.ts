import {HttpClient} from "@angular/common/http";
import {ILocationReservation} from "../interfaces/ILocationReservation";
import {BehaviorSubject, Observable} from "rxjs";
import {IUser} from "../interfaces/IUser";
import {IDate} from "../interfaces/IDate";
import {Injectable} from "@angular/core";
import {urls} from "../environments/environment";
import {CustomDate, dateToString} from "../interfaces/CustomDate";
import {Time} from "@angular/common";
import {ILocationReservationResponse} from "../interfaces/ILocationReservationResponse";

@Injectable()
export class LocationReservationService {
  private _MAX_PENALTY_POINTS: BehaviorSubject<number>;

  constructor(private http: HttpClient) {
    //periodically refetch max value
    this._MAX_PENALTY_POINTS = new BehaviorSubject<number>(0);
    //this.MAX_CANCEL_DATE = new BehaviorSubject<IDate>(new CustomDate());
    const that = this;
    this.http.get(urls.locationReservation + '/maxPenaltyPoints').subscribe(value => {
      that._MAX_PENALTY_POINTS.next(+value);
    });
  }

  get MAX_PENALTY_POINTS(): BehaviorSubject<number> {
    return this._MAX_PENALTY_POINTS;
  }

  getMaxCancelDate(): Observable<IDate> {
    return this.http.get<IDate>(urls.locationReservation + '/maxCancelDate');
  }

  getAllLocationReservationsOfUser(augentID: string): Observable<ILocationReservation[]> {
    return this.http.get<ILocationReservation[]>(urls.locationReservation + '/user/' + augentID);
  }

  getAllLocationReservationsOfUserByName(name: string): Observable<ILocationReservation[]> {
    return this.http.get<ILocationReservation[]>(urls.locationReservation + "/userByName/" + name);
  }

  getAllLocationReservationsOfLocationByName(nameOfLocation: string): Observable<ILocationReservation[]> {
    return this.http.get<ILocationReservation[]>(urls.locationReservation + '/location/' + nameOfLocation);
  }

  getLocationReservation(user: IUser, date: IDate): Observable<ILocationReservation> {
    return this.http.get<ILocationReservation>(urls.locationReservation + '/user/' + user.augentID + '/date/' + date.toString());
  }

  deleteLocationReservation(augentID: string, date: IDate, openingHour: Time): Observable<any>{
    date.hrs = openingHour.hours;
    date.min = openingHour.minutes;
    return this.http.delete(urls.locationReservation + '/' + augentID + '/' + dateToString(date));
  }

  addLocationReservations(locationReservations: ILocationReservation[]): Observable<ILocationReservationResponse> {
    return this.http.post<ILocationReservationResponse>(urls.locationReservation, JSON.stringify(locationReservations), {
      headers: {'Content-Type': 'application/json'}
    });
  }

  getCountReservedSeats(): Observable<Object> {
    return this.http.get<Object>(urls.locationReservation + "/count/" + dateToString(this.currentDate()));
  }

  currentDate(): CustomDate {
    let date;
    date = new Date();
    let custom;
    custom = new CustomDate();
    custom.day = date.getUTCDate();
    custom.month = date.getMonth() + 1;
    custom.year = date.getFullYear();
    custom.hrs = 0;
    custom.min = 0;
    custom.sec = 0;
    return custom;
  }

  scanBarcode(location: string, barcode: string): Promise<Object> {
    return this.http.post(urls.locationReservation + '/scan/' + location + '/' +barcode, null).toPromise();
  }

  setAllStudentsOfLocationToAttended(locationName: string): void {
    this.http.post(urls.locationReservation + '/closeICE/' + locationName, null).subscribe(value => {
    });
  }

  getAllAbsentLocationReservations(locationName: string, date: IDate): Observable<ILocationReservation[]> {
    return this.http.get<ILocationReservation[]>(urls.locationReservation + "/absent/" + locationName + "/" + dateToString(date));
  }

  getAllPresentLocationReservations(locationName: string, date: IDate): Observable<ILocationReservation[]> {
    return this.http.get<ILocationReservation[]>(urls.locationReservation + "/present/" + locationName + "/" + dateToString(date));
  }

  sendMailsToAbsentStudents(names: Set<string>){
    let searchstring = urls.locationReservation + '/sendMails?mails=';
    for(let n of names){
      searchstring += n +',';
    }
    this.http.post(searchstring.substr(0, searchstring.length-1), null).subscribe(value => {

    });
  }

  setReservationsToUnattendedAndAddPenaltyPoints(ids: Set<string>, location: string){
    let searchstring = urls.locationReservation + '/addPenaltyPoints/'+ location+ '?ids=';
    for(let id of ids){
      searchstring += id +',';
    }
    this.http.post(searchstring.substr(0, searchstring.length-1), null).subscribe(value => {
    });
  }


  setReservationToUnattended(location: string, augentId: string){
    this.http.post(urls.locationReservation + "/cancelScan/" + location + "/"+ augentId, null).subscribe(value => {

    });
  }
}
