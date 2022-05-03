import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { User, UserConstructor, UserSettings } from '../../../shared/model/User';
import { api } from '../endpoints';
import { Location, LocationConstructor } from '../../../shared/model/Location';
import { map } from 'rxjs/internal/operators/map';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  constructor(private http: HttpClient) {}

  getUserByAUGentId(id: string): Observable<User> {
    const params = new HttpParams().set('id', id);
    return this.http.get<User>(api.userByAUGentId, { params }).pipe(map(UserConstructor.newFromObj));
  }

  getUsersByFirstName(firstName: string): Observable<User[]> {
    const params = new HttpParams().set('firstName', firstName.trim());
    return this.http.get<User[]>(api.usersByFirstName, { params }).pipe(map(x => x.map(UserConstructor.newFromObj)));
  }

  getUsersByLastName(lastName: string): Observable<User[]> {
    const params = new HttpParams().set('lastName', lastName.trim());
    return this.http.get<User[]>(api.usersByLastName, { params }).pipe(map(x => x.map(UserConstructor.newFromObj)));
  }

  getUsersByFirstAndLastName(
    firstName: string,
    lastName: string
  ): Observable<User[]> {
    const params = new HttpParams()
      .set('firstName', firstName.trim())
      .set('lastName', lastName.trim());
    return this.http.get<User[]>(api.usersByFirstAndLast, { params }).pipe(map(x => x.map(UserConstructor.newFromObj)));
  }

  getUserByBarcode(barcode: string): Observable<User> {
    const params = new HttpParams().set('barcode', barcode.trim());
    return this.http.get<User>(api.userByBarcode, { params }).pipe(map(UserConstructor.newFromObj));
  }

  getManageableLocations(userId: string): Observable<Location[]> {
    return this.http
      .get<Location[]>(api.getManageableLocations.replace('{userId}', btoa(userId)))
      .pipe(
        map<Location[], Location[]>((value) => {
          const locations: Location[] = [];
          for (const location of value) {
            locations.push(LocationConstructor.newFromObj(location));
          }
          return locations;
        })
      );
  }

  updateUser(userId: string, user: User): Observable<void> {
    return this.http.put<void>(
      api.updateUser.replace('{userId}', btoa(userId)),
      user
    );
  }

  updateUserSettings(userId: string, userSettings: UserSettings): Observable<void> {
    console.log(userId);
    console.log(userSettings);
    const a = this.http.put<void>(
      api.updateUserSettings.replace('{userId}', btoa(userId)),
      userSettings
    );
      a.subscribe(thing => console.log(thing));

    return a;
  }

  hasUserAuthorities(userId: string): Observable<boolean> {
    if (userId === '') {
      return of(false);
    }

    return this.http.get<boolean>(
      api.hasUserAuthorities.replace('{userId}', btoa(userId))
    );
  }

  hasUserVolunteered(userId: string): Observable<boolean> {
    if (userId === '') {
      return of(false);
    }
    return this.http.get<boolean>(
      api.hasUserVolunteered.replace('{userId}', btoa(userId))
    );
  }

  getAdmins(): Observable<User[]> {
    return this.http.get<User[]>(api.getAdmins).pipe(map(x => x.map(UserConstructor.newFromObj)));
  }
}
