import {Injectable} from '@angular/core';
import {BehaviorSubject} from 'rxjs';
import {authenticationTypes, roles, urls} from '../environments/environment';
import {IUser} from "../interfaces/IUser";
import {IRoles} from "../interfaces/IRoles";
import {HttpClient, HttpHeaders, HttpResponse} from "@angular/common/http";
import {UserService} from "./user.service";

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {
  currentUser: BehaviorSubject<IUser>;
  roles: IRoles;
  sessionId;

  constructor(private http: HttpClient, private userService: UserService) {
    this.roles = roles;
    this.currentUser = new BehaviorSubject<IUser>(null);
/*
    if (!environment.production) {
      this.login('admin', 'admin', () => {
      }, () => {
      });
    }
*/
/*
    const sessionId = this.getCookie('mapping');
    if (sessionId.length > 0) {
      this.http.get<IUser>(urls.session + sessionId).toPromise().then((val) => {
        this.currentUser.next(val);
      }).catch((err) => {
        this.currentUser.next(null);
      });
    }*/
    this.sessionId = this.getCookie('mapping');
    this.getRequestUser();
  }

    getRequestUser(){
      this.sessionId = this.getCookie('mapping');
    if (this.sessionId.length > 0) {
      this.http.get<IUser>(urls.session + this.sessionId).toPromise().then((val) => {
        this.currentUser.next(val);
      }).catch((err) => {
        this.currentUser.next(null);
      });
    }
  }


  login(mail: string, password: string, succes?, fail?) {
    /*
    * What happens here might seem a bit strange at first: the HTTP response is handled within the
    * error-clause. The reason is that the HttpResponse wants to parse the body of the answer of
    * the server and the HttpResponse expects a JSON object but it finds HTML text. So the HttpResponse
    * throws an exception. That's the reason why the HTTP-response is handled in the error-clause.
    *
    * Spring Security will always respond with a HTTP page after a login has succeeded or failed.
    * When I set the responseType to 'text', the res variable will be a string and I won't be able
    * to parse the url sent back from the server. This is the only solution I've found, but it works...*/
    const headers = new HttpHeaders({
      'Content-Type': 'application/x-www-form-urlencoded',
      'Authentication-Type': authenticationTypes.augent
    });

    const body = 'mail=' + mail + '&password=' + password;
    this.http.post<HttpResponse<any>>(urls.signin, body, {headers}).subscribe(res => {
    }, r => {
      if (r.url.indexOf('dashboard') !== -1) {
        this.userService.getUserByEmail(mail).subscribe(value => {
          this.currentUser.next(value);
            succes(urls.dashboard);
        });
      } else {
        fail();
      }
    });
  }

  async logout(): Promise<void> {
    this.http.post(urls.signout, {}, {
      headers: new HttpHeaders({
        'Accept': 'text/html',
        'Content-Type': 'application/json'
      }),
      responseType: 'text'
    }).subscribe((r) => {
      this.currentUser.next(null);
    });
  }

  getCurrentUser(): IUser {
    return this.currentUser.getValue();
  }

  currentUserHasRole(...roles : string[]): boolean {
    if(this.currentUser === null || this.currentUser.getValue() === null) return false;
    for(let role of roles){
      if(this.currentUser.getValue().roles.includes(role)){
        return true;
      }
    }
    return false;
  }

  async updateOwnProfile(user: IUser){
    await this.userService.updateUser(user).subscribe(updatedUser => {
      this.currentUser.next(updatedUser);
      this.login(user.mail,user.password);
    });
  }

  getCookie(cname): string {
    const name = cname + "=";
    const decodedCookie = decodeURIComponent(document.cookie);
    const ca = decodedCookie.split(';');
    for (let i = 0; i < ca.length; i++) {
      let c = ca[i];
      while (c.charAt(0) == ' ') {
        c = c.substring(1);
      }
      if (c.indexOf(name) == 0) {
        return c.substring(name.length, c.length);
      }
    }
    return "";
  }
}
