import {HttpClient, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import * as moment from 'moment';
import {Observable} from 'rxjs';
import {delay, tap} from 'rxjs/operators';

@Injectable({
    providedIn: 'root'
})
export class AddressResolverService {
    public static readonly ADDRESS_RESOLVER_URL = 'https://nominatim.openstreetmap.org/search';

    private lastSearch = moment();
    private delay = 1000;

    constructor(
        private http: HttpClient
    ) {
    }

    query(address: string): Observable<{ lat: string, lon: string }[]> {
        const params = new HttpParams()
            .set('q', address)
            .set('format', 'json');

        return this.http.get<{ lat: string, lon: string }[]>(AddressResolverService.ADDRESS_RESOLVER_URL, {params}).pipe(
            delay(
                Math.max(this.delay - moment().diff(this.lastSearch), 0)
            ),
            tap(() => {
                this.lastSearch = moment();
            })
        );
    }
}
