import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot, UrlTree} from "@angular/router";
import {Observable, Subject} from "rxjs";
import {AuthenticationService} from "./authentication.service";
import {IUser} from "../interfaces/IUser";
import {urls} from "../environments/environment";
import {HttpClient} from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class LoginGuardService implements CanActivate {

  constructor(private router: Router, private authenticationService: AuthenticationService, private http: HttpClient) {
  }

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    const sessionId = this.authenticationService.getCookie('mapping');

    return this.http.get<IUser>(urls.session + sessionId).toPromise().then((val) => {
      this.authenticationService.currentUser.next(val);
      console.log(val);
      if(val==null){
        return true;
      }
      this.router.navigateByUrl(urls.dashboard);
      return false;
    }).catch((err) => {
      return true;
    });
  }
}
