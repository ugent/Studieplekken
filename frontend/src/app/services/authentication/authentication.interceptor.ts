import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from "@angular/common/http"
import { Injectable } from "@angular/core";
import { Router } from "@angular/router";
import { Observable, of } from "rxjs";
import { catchError, tap } from "rxjs/operators";
import { AuthenticationService } from "./authentication.service";

@Injectable()
export class AuthenticationInterceptor implements HttpInterceptor {

    public constructor(private authenticationService: AuthenticationService, private router: Router) {}
    intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        const startingUrl = req.url;
        const s = next.handle(req);

        const handleUnauthorized = (e) => {
            if (!e.ok && e.status == 0) {
                // This is an unknown error. I'm assuming it was CORS blocking the redirect to login.ugent.be due to expiring ticket times.
                // I'm going to log out the user and redirect to login page.

                this.authenticationService.authExpired(this.router.url)
            }
            throw e
        }
        return s.pipe(catchError(handleUnauthorized))
    }

}