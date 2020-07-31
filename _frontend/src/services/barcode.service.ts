import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {urls} from "../environments/environment";
import {Observable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class BarcodeService {
  constructor(private http: HttpClient) {
  }

  getBarcodeImage(content: string): Observable<any> {
    return this.http.get(urls.userBarcode + '/'+ content, {responseType: 'blob'});
  }

}
