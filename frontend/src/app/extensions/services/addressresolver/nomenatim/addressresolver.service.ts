import {HttpClient, HttpHeaders, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import * as moment from 'moment';
import {Observable, of} from 'rxjs';
import {delay, switchMap, tap} from 'rxjs/operators';

@Injectable({
    providedIn: 'root'
})
export class AddressResolverService {
    public static readonly ADDRESS_RESOLVER_URL = 'https://nominatim.openstreetmap.org/search';

    private lastSearch = moment();

    constructor(private http: HttpClient) {
    }

    query(address: string): Observable<{ lat: number, lon: number }[]> {
        const params = new HttpParams()
            .set('q', address)
            .set('format', 'json');
        const headers = new HttpHeaders().set('Referer', 'https://studieplekken.ugent.be');

        return this.withDelay(this.http.get<{
            lat: number,
            lon: number
        }[]>(AddressResolverService.ADDRESS_RESOLVER_URL, {headers, params}));
    }


    /**
     * If necessary, we are adding extra delay between our request.
     *
     * This is so we comply with the usage policy (https://operations.osmfoundation.org/policies/nominatim/) for sure.
     */
    private withDelay<T>(o: Observable<T>): Observable<T> {
        const now = moment();
        const allowedFrom = this.lastSearch;
        const diff = now.diff(allowedFrom, 'milliseconds', true);

        if (now.isBefore(allowedFrom) && diff > 0) {
            return of(null).pipe(delay(diff), switchMap(() => o), tap(_ => this.lastSearch = moment().add(2, 'seconds')));
        } else {
            return o.pipe(tap(_ => this.lastSearch = moment().add(2, 'seconds')));
        }
    }
}
