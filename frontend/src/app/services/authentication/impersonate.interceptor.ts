import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable()
export class ImpersonateInterceptor implements HttpInterceptor {


    public constructor() {}
    intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
      console.log(this.getUser())
      if(!this.getUser())
        return next.handle(req);

        const r = req.clone({
          headers: req.headers.append("AS-USER", this.getUser())
        });

        return next.handle(r);
    }

    private getUser() {
      return localStorage.getItem("impersonate");
    }
}
