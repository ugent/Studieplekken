import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuthenticationService } from './authentication.service';

@Injectable()
export class TokenInterceptor implements HttpInterceptor {

    public constructor(
        private authenticationService: AuthenticationService
    ) {
    }

    /**
     * Intercepts HTTP requests to add an authentication token to the headers if available.
     * If the request fails with a 401 Unauthorized status, logs out the user.
     *
     * @param req - The outgoing HTTP request.
     * @param next - The next interceptor in the chain, or the backend if no interceptors remain.
     * @returns An observable of the HTTP event.
     */
    public intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        const token = localStorage.getItem("access_token");

        const authReq = !token ? req : req.clone({
            headers: req.headers.set('X-AUTH', token)
        });

        const s = next.handle(authReq);

        const handleUnauthorized = (e: { ok: any; status: number; }) => {
            if (!e.ok && e.status === 401) {
                // This is an unknown error. I'm assuming it was CORS blocking the redirect to login.ugent.be due to expiring ticket times.
                // I'm going to log out the user and redirect to login page.

                this.authenticationService.logout();
            }

            throw e;
        };

        return s.pipe(catchError(handleUnauthorized));
    }

}
