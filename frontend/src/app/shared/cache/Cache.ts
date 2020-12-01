import {BehaviorSubject, EMPTY, Observable, of} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import { catchError, filter, map, tap } from 'rxjs/operators';

export class Cache<I, V> {

  constructor(private http: HttpClient,
              private idcallback: (arg: any) => I) { }

  cacheMap: Map<I, V> = new Map<I, V>();
  cacheSubject: BehaviorSubject<Map<I, V>> = new BehaviorSubject<Map<I, V>>(this.cacheMap);

  /**
   * Update the cache from the backend
   * @param url the url to get the resource
   */
  private updateCache(url: string): void {
    this.http.get<V>(url)
      .pipe(
        tap(n => this.cacheMap.set(this.idcallback(n), n)),
        tap(() => this.cacheSubject.next(this.cacheMap)),
        catchError(error => {
          this.cacheSubject.error(error);
          return of(EMPTY);
        })
      )
      .subscribe();
  }

  /**
   * Perform a cachereload by updating the values in the url
   * @param url the url to fetch the resources from
   */
  private cacheReload(url: string): void {
    this.http.get<V[]>(url)
      .pipe(
        tap(n => n.forEach(element => this.cacheMap.set(this.idcallback(element), element))),
        tap(() => this.cacheSubject.next(this.cacheMap)),
        catchError(error => {
          this.cacheSubject.error(error);
          return of(EMPTY);
        })
      )
      .subscribe();
  }

  /**
   * Get the value from the cache
   * @param id the id to identify the resource
   * @param url the url to get the resource
   * @param invalidateCache should the cacheline for the resouce be invalidated or not
   */
  getValue(id: I, url: string, invalidateCache: boolean = false): Observable<V> {
    if (invalidateCache || !this.cacheMap.has(id)) {
      this.updateCache(url);
    }

    return this.cacheSubject.pipe(
      map(valueMap => valueMap.get(id)),
      filter(value => !!value)
    );
  }

  /**
   * Get all the resources from an url and update the cache
   * @param url the url to fetch the resources from
   */
  getAllValues(url: string): Observable<V[]> {
    this.cacheReload(url);

    return this.cacheSubject.pipe(
      map(valueMap => [ ...valueMap.values() ])
    );
  }
}
