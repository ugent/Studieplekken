import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {urls} from "../environments/environment";
import {Observable} from "rxjs";

/**
 * The 'newUser' variable is set when a new student has registered at this application.
 * The data is received from RegistrationComponent and used by VerificationComponent
 * to let the new user know there has been send a mail to the given email address.
 * This VerificationService can be seen as the glue for Inter Component Communication.
 */

@Injectable({
  providedIn: 'root'
})
export class VerificationService {
  newUser: {email: string, password: string};

  constructor(private http: HttpClient) { }

  setNewUser(newUser) {
    this.newUser = newUser;
  }

  getNewUser() {
    return this.newUser;
  }

  verify(code: string): Observable<void> {
    return  this.http.get<void>(urls.accountVerify+'?code='+code);
  }
}
