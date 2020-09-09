import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {api} from '../../../../environments/environment';
import {LocationTag} from '../../../shared/model/LocationTag';

@Injectable({
  providedIn: 'root'
})
export class TagsService {

  constructor(private http: HttpClient) { }

  getAllTags(): Observable<LocationTag[]> {
    return this.http.get<LocationTag[]>(api.tags);
  }
}
