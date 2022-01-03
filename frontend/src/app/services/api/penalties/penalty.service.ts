import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Penalty } from '../../../shared/model/Penalty';
import { api } from '../endpoints';
import { Observable } from 'rxjs';
import { PenaltyEvent } from '../../../shared/model/PenaltyEvent';
import { map } from 'rxjs/operators';

export type PenaltyList = {points: number, penalties: Penalty[]}

@Injectable({
  providedIn: 'root',
})
export class PenaltyService {
  constructor(private http: HttpClient) {}

  /****************************************
   *    API calls concerning Penalties    *
   ****************************************/

  getPenaltiesOfUserById(id: string): Observable<PenaltyList> {
    return this.http.get<any>(api.penaltiesByUserId.replace('{id}', id))
    .pipe(
      map(a => ({points: a.currentPoints, penalties: a.penalties.map(Penalty.fromJSON)}))
      );
  }

  addPenalty(penalty: Penalty): Observable<void> {
    return this.http.post<void>(api.addPenalty, penalty);
  }

  deletePenalty(penalty: Penalty): Observable<void> {
    const options = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
      }),
      body: penalty.toJSON(),
    };
    return this.http.delete<void>(api.deletePenalty, options);
  }
}
