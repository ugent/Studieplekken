import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Penalty} from '../../../shared/model/Penalty';
import {api} from '../endpoints';
import {Observable} from 'rxjs';
import {PenaltyEvent} from '../../../shared/model/PenaltyEvent';

@Injectable({
  providedIn: 'root'
})
export class PenaltyService {

  constructor(private http: HttpClient) {
  }

  /****************************************
   *    API calls concerning Penalties    *
   ****************************************/

  getPenaltiesOfUserById(id: string): Observable<Penalty[]> {
    return this.http.get<Penalty[]>(api.penaltiesByUserId.replace('{id}', id));
  }

  addPenalty(penalty: Penalty): Observable<any> {
    return this.http.post(api.addPenalty, penalty);
  }

  deletePenalty(penalty: Penalty): Observable<any> {
    const options = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json'
      }),
      body: penalty
    };
    return this.http.delete(api.deletePenalty, options);
  }

  /********************************************
   *    API calls concerning PenaltyEvents    *
   ********************************************/

  getPenaltyEvents(): Observable<PenaltyEvent[]> {
    return this.http.get<PenaltyEvent[]>(api.penaltyEvents);
  }

  addPenaltyEvent(penaltyEvent: PenaltyEvent): Observable<any> {
    return this.http.post(api.addPenaltyEvent, penaltyEvent);
  }

  updatePenaltyEvent(code: number, penaltyEvent: PenaltyEvent): Observable<any> {
    return this.http.put(api.updatePenaltyEvent.replace('{code}', String(code)), penaltyEvent);
  }

  deletePenaltyEvent(penaltyEvent: PenaltyEvent): Observable<any> {
    const options = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json'
      }),
      body: penaltyEvent
    };
    return this.http.delete(api.deletePenaltyEvent, options);
  }
}
