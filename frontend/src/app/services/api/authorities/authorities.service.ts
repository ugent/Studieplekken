import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Authority} from '../../../shared/model/Authority';
import {api} from '../../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthoritiesService {

  constructor(private http: HttpClient) { }

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
}
