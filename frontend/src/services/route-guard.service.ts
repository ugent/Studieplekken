import {Injectable} from '@angular/core';
import {CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router, UrlTree} from '@angular/router';
import {Observable} from 'rxjs';
import {AuthenticationService} from './authentication.service';
import {HttpClient} from '@angular/common/http';
import {IUser} from '../interfaces/IUser';
import {urls} from '../environments/environment';
import {tick} from '@angular/core/testing';


@Injectable()
export class RouteGuardService implements CanActivate {

  constructor(private router: Router, private authenticationService: AuthenticationService, private http: HttpClient) {
  }

  public canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot)
    : Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    const sessionId = this.authenticationService.getCookie('mapping');
    if (sessionId.length > 0) {
      // return as an observable because else the route guard wont wait for it
      return this.http.get<IUser>(urls.session + sessionId).toPromise().then((val) => {
        this.authenticationService.currentUser.next(val);
        return true;
      }).catch((err) => {
        this.authenticationService.currentUser.next(null);
        return this.router.navigateByUrl(urls.login);
      });
    } else {
      return this.router.navigateByUrl(urls.login);
    }

    /*console.log("test in rout guard");
    if (this.authenticationService.currentUser.getValue() === null) {
      this.router.navigateByUrl(urls.login);
      return false;
    }
    return true;
     */
  }

}
