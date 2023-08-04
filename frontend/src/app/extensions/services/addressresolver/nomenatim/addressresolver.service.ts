import {HttpClient, HttpHeaders, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import * as moment from 'moment';
import {Observable, of} from 'rxjs';
import {delay, switchMap, tap} from 'rxjs/operators';
import {Location} from '@angular/common';
import {now} from 'd3';

@Injectable({
    providedIn: 'root'
})
export class AddressResolverService {
    public static readonly ADDRESS_RESOLVER_URL = 'https://geocode.maps.co/search';

    private lastSearch = moment();
    private delay = 1000;

    constructor(
        private http: HttpClient
    ) {
    }

    query(address: string): Observable<{ lat: string, lon: string }[]> {
        const params = new HttpParams().append('q', address);

        return this.http.get<{ lat: string, lon: string }[]>(
            AddressResolverService.ADDRESS_RESOLVER_URL, {params}
        ).pipe(
            delay(
                Math.max(this.delay - moment().diff(this.lastSearch), 0)
            ),
            tap(() => {
                this.lastSearch = moment();
            })
        );
    }
}
