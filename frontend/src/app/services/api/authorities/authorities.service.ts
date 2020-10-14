import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Authority} from '../../../shared/model/Authority';
import {api} from '../../../../environments/environment';
import {User} from '../../../shared/model/User';
import {Location} from '../../../shared/model/Location';

@Injectable({
  providedIn: 'root'
})
export class AuthoritiesService {

  constructor(private http: HttpClient) { }

  // *************************************
  // *   CRUD operations for AUTHORITY   *
  // *************************************/

  getAllAuthorities(): Observable<Authority[]> {
    return this.http.get<Authority[]>(api.authorities);
  }

  getAuthority(authorityId: number): Observable<Authority> {
    return this.http.get<Authority>(api.authority.replace('{authorityId}', String(authorityId)));
  }

  addAuthority(authority: Authority): Observable<any> {
    return this.http.post(api.addAuthority, authority);
  }

  updateAuthority(authorityId: number, authority: Authority): Observable<any> {
    return this.http.put(api.updateAuthority.replace('{authorityId}', String(authorityId)), authority);
  }

  deleteAuthority(authorityId: number): Observable<any> {
    return this.http.delete(api.deleteAuthority.replace('{authorityId}', String(authorityId)));
  }

  // ************************************************
  // *   CRUD operations for ROLES_USER_AUTHORITY   *
  // ************************************************/

  getUsersFromAuthority(authorityId: number): Observable<User[]> {
    return this.http.get<User[]>(api.usersInAuthority.replace('{authorityId}', String(authorityId)));
  }

  getAuthoritiesOfUser(userId: string): Observable<Authority[]> {
    return this.http.get<Authority[]>(api.authoritiesOfUser.replace('{userId}', userId));
  }

  getLocationsInAuthoritiesOfUser(userId: string): Observable<Location[]> {
    return this.http.get<Location[]>(api.locationsInAuthoritiesOfUser.replace('{userId}', userId));
  }

  addUserToAuthority(userId: string, authorityId: number): Observable<any> {
    return this.http.post(api.addUserToAuthority
      .replace('{userId}', userId)
      .replace('{authorityId}', String(authorityId)), {});
  }

  deleteUserFromAuthority(userId: string, authorityId: number): Observable<any> {
    return this.http.delete(api.deleteUserFromAuthority
      .replace('{userId}', userId)
      .replace('{authorityId}', String(authorityId)));
  }
}
