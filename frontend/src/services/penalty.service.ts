import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {tap} from "rxjs/operators";
import {IPenaltyEvent} from '../interfaces/IPenaltyEvent';
import {urls} from '../environments/environment';
import {IPenalty} from "../interfaces/IPenalty";
import {IPair} from "../interfaces/IPair";
import {IDate} from "../interfaces/IDate";
import {dateToString} from "../interfaces/CustomDate";

@Injectable({
  providedIn: 'root'
})
export class PenaltyService {

  constructor(private http: HttpClient) {
  }

  getAllPenaltyEvents(): Observable<IPenaltyEvent[]> {
    return this.http.get<IPenaltyEvent[]>(urls.penaltyEvent);
  }

  getCancelPoints(date: IDate): Observable<number> {
    return this.http.get<number>(urls.penaltyEvent + '/cancelPoints/' + dateToString(date));
  }

  changePenaltyEvent(penaltyEvent: IPenaltyEvent): Observable<IPenaltyEvent> {
    return this.http.put<IPenaltyEvent>(urls.penaltyEvent + '/'+ penaltyEvent.code, penaltyEvent);
  }

  addPenaltyEvent(penaltyEvent: IPenaltyEvent): Observable<IPenaltyEvent> {
    return this.http.post<IPenaltyEvent>(urls.penaltyEvent + '/' + penaltyEvent.code, penaltyEvent);
  }

  deletePenaltyEvent(code: number): Observable<IPenaltyEvent>{
    return this.http.delete<IPenaltyEvent>(urls.penaltyEvent + '/' + code);
  }

  getPenaltyEvent(code: number): Observable<IPenaltyEvent>{
    return this.http.get<IPenaltyEvent>(urls.penaltyEvent+'/'+code);
  }
  getPenalties(augentID: string): Observable<IPenalty[]> {
    return this.http.get<IPenalty[]>(urls.penaltyEvent + '/user/' + augentID);
  }

  updatePenalties(augentID: string, pair: IPair): Observable<any> {
    return this.http.put(urls.penaltyEvent + '/user/' + augentID, pair);
  }
}
