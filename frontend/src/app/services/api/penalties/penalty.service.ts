import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Penalty} from '../../../shared/model/Penalty';
import {api} from '../../../../environments/environment';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class PenaltyService {

  constructor(private http: HttpClient) { }

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
}
