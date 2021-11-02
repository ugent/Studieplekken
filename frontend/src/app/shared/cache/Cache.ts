import { BehaviorSubject, EMPTY, Observable, of } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { catchError, filter, map, tap } from 'rxjs/operators';

export class Cache<I, V> {
  constructor(
    private http: HttpClient,
    private idcallback: (arg: unknown) => I,
    private mapFunction?: (arg: unknown) => V
  ) {}

  cacheMap: Map<I, V> = new Map<I, V>();
  cacheSubject: BehaviorSubject<Map<I, V>> = new BehaviorSubject<Map<I, V>>(
    this.cacheMap
  );

  /**
   * Update the cache from the backend
   * @param url the url to get the resource
   * @param id the id of the resource to be updated
   */
  private updateCache(url: string, id: I): void {
    this.http
      .get<V>(url)
      .pipe(
        map((n) => (this.mapFunction ? this.mapFunction(n) : n)),
        tap((n) => this.cacheMap.set(id, n)),
        tap(() => this.cacheSubject.next(this.cacheMap)),
        catchError((error) => {
          this.cacheSubject.error(error);
          return of(EMPTY);
        })
      )
      .subscribe();
  }

  /**
   * Perform a cache reload by updating the values in the url
   * @param url the url to fetch the resources from
   */
  private cacheReload(url: string): void {
    this.http
      .get<V[]>(url)
      .pipe(
        map((n) => n.map((v) => (this.mapFunction ? this.mapFunction(v) : v))),
        tap((n) => {
          this.cacheMap = new Map();
          n.forEach((element) =>
            this.cacheMap.set(this.idcallback(element), element)
          )

        }
        ),
        tap(() => {
          console.log(this.cacheMap)
          this.cacheSubject.next(this.cacheMap);
        }),
        catchError((error) => {
          this.cacheSubject.error(error);
          return of(EMPTY);
        })
      )
      .subscribe();
  }

  /**
   * Get the value from the cache
   * Important: default behaviour is NOT invalidating the cache!
   * @param id the id to identify the resource
   * @param url the url to get the resource
   * @param invalidateCache should the cache line for the resource be invalidated or not
   */
  getValue(
    id: I,
    url: string,
    invalidateCache: boolean = false
  ): Observable<V> {
    if (invalidateCache || !this.cacheMap.has(id)) {
      this.updateCache(url, id);
    }

    return this.cacheSubject.pipe(
      map((valueMap) => valueMap.get(id)),
      filter((value) => !!value)
    );
  }

  /**
   * Get all the resources from an url and update the cache.
   * Important: default behaviour is invalidating the cache!
   * @param url the url to fetch the resources from
   * @param invalidateCache should the cache line for the resource be invalidated or not
   */
  getAllValues(url: string, invalidateCache: boolean = true): Observable<V[]> {
    if (invalidateCache || Object.keys(this.cacheMap).length === 0) {
      // if the cache is being reloaded, the current value can be skipped
      this.cacheReload(url);
    }
    // if the cache is not being reloaded, the current value suffices
    return this.cacheSubject.pipe(
      map((valueMap) => [...valueMap.values()]),
      filter((v) => !(v === null || v === undefined || v.length === 0))
    );
  }
}
