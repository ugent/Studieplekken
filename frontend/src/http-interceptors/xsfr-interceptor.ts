import {Injectable} from "@angular/core";
import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from "@angular/common/http";
import {Observable} from "rxjs";
import {baseHref} from "../environments/environment";

@Injectable()
export class XsfrInterceptor implements HttpInterceptor {

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {

    // add XSRF-TOKEN header
    const newReq = req.clone( {
      headers: req.headers.set('X-XSRF-TOKEN',this.getCookie("XSRF-TOKEN") ),
      url: baseHref + (req.url[0] === '.' && baseHref.length > 0 ? req.url.substr(1) : req.url)
    });

    // send request to next handler
    return next.handle(newReq);
  }

  getCookie(name: string) : string{
    let match = document.cookie.match(new RegExp('(^| )' + name + '=([^;]+)'));
    if (match) {
      return match[2];
    }
  }

}
