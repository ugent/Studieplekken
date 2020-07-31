import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {IUser} from "../interfaces/IUser";
import {urls} from "../environments/environment";

@Injectable({
  providedIn: 'root'
})

export class UserService {
  private flag: boolean;

  constructor(private http: HttpClient) { }

  getUserByEmail(email: string): Observable<IUser>{
    return this.http.get<IUser>(urls.accountEmail+email);
  }

  getUsersByFirstName(firstName: string): Observable<IUser[]>{
    return this.http.get<IUser[]>(urls.accountFirstName+firstName);
  }

  getUsersByName(name:string): Observable<IUser[]>{
    return this.http.get<IUser[]>(urls.accountName+name);
  }

  getUserByAugentID(augentID: string): Observable<IUser>{
    return this.http.get<IUser>(urls.account+augentID);
  }

  getUsersNamesByRole(role: string): Observable<string[]>{
    return this.http.get<string[]>(urls.accountRole+role);
  }

  /** PUT: update the user on the server */
  updateUser (user: IUser): Observable<IUser> {
    return this.http.put<IUser>(urls.account + user.mail, JSON.stringify(user), {headers: {'Content-Type': 'application/json'}});
  }

  /** POST: add the user to the server */
  addUser(user: IUser): Observable<IUser> {
    return this.http.post<IUser>(urls.newAccount, user);
  }

  /** POST: add the user created by an employee (no verification needed) */
  addUserByEmployee(user: IUser): Observable<IUser> {
    return this.http.post<IUser>(urls.newAccount + '/by/employee', user);
  }

  /** checks whether a user with the given email already exists */
  async existsUser(email: string): Promise<boolean> {
    await this.getUserByEmail(email).toPromise().then((u => {
      if (u == undefined) {
        this.flag = false;
      } else {
        this.flag = true;
      }
    }));
    return this.flag
  }
}
