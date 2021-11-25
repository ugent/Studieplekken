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
    this.currentBreadcrumbs.next([homeBreadcrumb, studentInGentBreadcrumb, studerenBreadcrumb, blokLocatiesBreadcrumb, ...breadcrumbs]);
  }

}

export const managementBreadcrumb = {
  pageName: "Management",
  url: "/management"
}

const homeBreadcrumb = {
  pageName: "Home",
  url: "https://stad.gent/nl"
}

const studentInGentBreadcrumb = {
  pageName: "Student in Gent",
  url: "https://stad.gent/nl/student-gent"
}

const studerenBreadcrumb = {
  pageName: "Studeren",
  url: "https://stad.gent/nl/student-gent/studeren"
}


const blokLocatiesBreadcrumb = {
  pageName: "Bloklocaties",
  url: "https://stad.gent/nl/student-gent/studeren/bloklocaties"
}

export interface Breadcrumb {
  pageName: string;
  url: string;
}
