import {Observable, of} from 'rxjs';
import {urls} from '../../environments/environment';

export default class BarcodeServiceStub{
  getBarcodeImage(content: string): Observable<any> {
    return of(null);
  }
}
