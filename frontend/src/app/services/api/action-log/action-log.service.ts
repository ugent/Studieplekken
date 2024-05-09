import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ActionLogEntry} from '@/model/ActionLogEntry';
import { api } from '../endpoints';
import { map } from 'rxjs/internal/operators/map';

@Injectable({
  providedIn: 'root'
})
export class ActionLogService {

  constructor(private http: HttpClient) { }


  getAllActions(): Observable<ActionLogEntry[]> {
    return this.http.get<any[]>(api.actions)
    .pipe(
      map(
        (list) => list.map(ActionLogEntry.fromJSON.bind(this))
      )
    );
  }

}
