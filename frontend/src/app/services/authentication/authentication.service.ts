import { Injectable } from '@angular/core';
import {BehaviorSubject, Observable} from 'rxjs';
import {User} from '../../shared/model/User';
import {HttpClient} from '@angular/common/http';
import {api} from '../../../environments/environment';

/**
 * The structure of the authentication service has been based on this article:
 *   - https://jasonwatmore.com/post/2020/05/15/angular-9-role-based-authorization-tutorial-with-example
 *   - but without using the actual JWT authentication, this is not save as mentioned in:
 *     - https://dev.to/rdegges/please-stop-using-local-storage-1i04
 *
 * The authentication of non UGent users will use HTTP-only cookies created by the backend.
 * The authentication of UGent users will use a cookie created from CAS, HTTP-only as well.
 *
 * Importance of HTTP-only cookies: https://blog.codinghorror.com/protecting-your-cookies-httponly/
 */
@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {
  // BehaviorSubject to be able to emit on changes
  // private so that only the AuthenticationService can modify the user
  private userSubject: BehaviorSubject<User>;
  // and other components can subscribe using the public observable
  // (which comes from the userSubject)
  public user: Observable<User>;

  constructor(private http: HttpClient) {
    this.userSubject = new BehaviorSubject<User>(null);
    this.user = this.userSubject.asObservable();

    // TODO: try to obtain a user object based on a HTTP-only session cookie, if provided
    //   this way, if a user was logged in previously, he/she doesn't have to do it again
    http.get<User>(api.user_by_mail.replace('{mail}', 'bram.vandewalle@ugent.be'))
      .subscribe(next => {
        this.userSubject.next(next);
    });
  }

  userValue(): User {
    return this.userSubject.value;
  }

  login(mail: string, password: string): void {
    // TODO: login
  }

  logout(): void {
    // TODO: logout
  }
}
