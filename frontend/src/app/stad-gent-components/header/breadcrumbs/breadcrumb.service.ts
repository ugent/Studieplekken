import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, Subject } from 'rxjs';
import { ReplaySubject } from 'rxjs';
import { tap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class BreadcrumbService {

  private currentBreadcrumbs: Subject<Breadcrumb[]> = new BehaviorSubject([]);

  constructor() { }

  public getCurrentBreadcrumbs(): Observable<Breadcrumb[]> {
    return this.currentBreadcrumbs;
  }

  public setCurrentBreadcrumbs(breadcrumbs: Breadcrumb[]) {
    this.currentBreadcrumbs.next(breadcrumbs);
  }

}

export const dashboardBreadcrumb = {
  pageName: "Dashboard",
  url: "/dashboard"
}

export interface Breadcrumb {
  pageName: string;
  url: string;
}