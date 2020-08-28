import { Injectable } from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {User} from '../../../shared/model/User';
import {api} from '../../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  constructor(private http: HttpClient) { }

  getUserByAUGentId(id: string): Observable<User> {
    const params = new HttpParams().set('id', id);
    return this.http.get<User>(api.userByAUGentId, { params });
  }

  getUsersByFirstName(firstName: string): Observable<User[]> {
    const params = new HttpParams().set('firstName', firstName.trim());
    return this.http.get<User[]>(api.usersByFirstName, { params });
  }

  getUsersByLastName(lastName: string): Observable<User[]> {
    const params = new HttpParams().set('lastName', lastName.trim());
    return this.http.get<User[]>(api.usersByLastName, { params });
  }

  getUsersByFirstAndLastName(firstName: string, lastName: string): Observable<User[]> {
    const params = new HttpParams()
      .set('firstName', firstName.trim())
      .set('lastName', lastName.trim());
    return this.http.get<User[]>(api.usersByFirstAndLast, { params });
  }

  getUserByBarcode(barcode: string): Observable<User> {
    const params = new HttpParams().set('barcode', barcode.trim());
    return this.http.get<User>(api.userByBarcode, { params });
  }

  updateUser(id: string, user: User): Observable<void> {
    return this.http.put<void>(api.updateUser.replace('{id}', id), user);
  }
}
