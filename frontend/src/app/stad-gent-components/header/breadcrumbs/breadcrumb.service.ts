import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, Subject } from 'rxjs';
import { ReplaySubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { environment } from 'src/environments/environment';

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

export const dashboardBreadcrumb = !environment.useExternalDashboard ? {
  pageName: "Dashboard",
  url: "/dashboard",
  external: false
} : {
  pageName: "Dashboard",
  url: environment.externalDashboardUrl,
  external: true
};

console.log(dashboardBreadcrumb)

export const managementBreadcrumb = {
  pageName: "Management",
  url: "/management"
}

export interface Breadcrumb {
  pageName: string;
  url: string;
}
