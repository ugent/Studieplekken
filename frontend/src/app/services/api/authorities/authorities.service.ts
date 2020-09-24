import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Authority} from '../../../shared/model/Authority';
import {api} from '../../../../environments/environment';
import {User} from '../../../shared/model/User';

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

  addAuthority(authority: Authority): Observable<any> {
    return this.http.post(api.authorities, authority);
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

  getUsersFromAuthority(authority: Authority): Observable<User[]> {
    return this.http.get<User[]>(api.usersInAuthority.replace('{authorityId}', String(authority.authorityId)));
  }

  getAuthoritiesOfUser(user: User): Observable<Authority[]> {
    return this.http.get<Authority[]>(api.authoritiesOfUser.replace('{userId}', user.augentID));
  }

  addUserToAuthority(user: User, authority: Authority): Observable<any> {
    return this.http.post(api.addUserToAuthority
      .replace('{userId}', user.augentID)
      .replace('{authorityId}', String(authority.authorityId)), {});
  }

  deleteUserFromAuthority(user: User, authority: Authority): Observable<any> {
    return this.http.delete(api.deleteUserFromAuthority
      .replace('{userId}', user.augentID)
      .replace('{authorityId}', String(authority.authorityId)));
  }
}
