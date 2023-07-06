import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Authority} from '../../../model/Authority';
import {api} from '../endpoints';
import {User} from '../../../model/User';
import {Location} from '../../../model/Location';

@Injectable({
    providedIn: 'root',
})
export class AuthoritiesService {
    constructor(private http: HttpClient) {
    }

    // *************************************
    // *   CRUD operations for AUTHORITY   *
    // *************************************/

    getAllAuthorities(): Observable<Authority[]> {
        return this.http.get<Authority[]>(api.authorities);
    }

    getAuthority(authorityId: number): Observable<Authority> {
        return this.http.get<Authority>(
            api.authority.replace('{authorityId}', String(authorityId))
        );
    }

    addAuthority(authority: Authority): Observable<void> {
        return this.http.post<void>(api.addAuthority, authority);
    }

    updateAuthority(authorityId: number, authority: Authority): Observable<void> {
        return this.http.put<void>(
            api.updateAuthority.replace('{authorityId}', String(authorityId)),
            authority
        );
    }

    deleteAuthority(authorityId: number): Observable<void> {
        return this.http.delete<void>(
            api.deleteAuthority.replace('{authorityId}', String(authorityId))
        );
    }

    // ************************************************
    // *   CRUD operations for ROLES_USER_AUTHORITY   *
    // ************************************************/

    getUsersFromAuthority(authorityId: number): Observable<User[]> {
        return this.http.get<User[]>(
            api.usersInAuthority.replace('{authorityId}', String(authorityId))
        );
    }

    getAuthoritiesOfUser(userId: string): Observable<Authority[]> {
        return this.http.get<Authority[]>(
            api.authoritiesOfUser.replace('{userId}', btoa(userId))
        );
    }

    getLocationsInAuthoritiesOfUser(userId: string): Observable<Location[]> {
        return this.http.get<Location[]>(
            api.locationsInAuthoritiesOfUser.replace('{userId}', btoa(userId))
        );
    }

    addUserToAuthority(userId: string, authorityId: number): Observable<void> {
        return this.http.post<void>(
            api.addUserToAuthority
                .replace('{userId}', btoa(userId))
                .replace('{authorityId}', String(authorityId)),
            {}
        );
    }

    deleteUserFromAuthority(
        userId: string,
        authorityId: number
    ): Observable<void> {
        return this.http.delete<void>(
            api.deleteUserFromAuthority
                .replace('{userId}', btoa(userId))
                .replace('{authorityId}', String(authorityId))
        );
    }
}
