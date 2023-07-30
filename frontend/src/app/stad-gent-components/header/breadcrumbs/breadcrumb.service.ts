import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable, Subject} from 'rxjs';
import {ReplaySubject} from 'rxjs';
import {tap} from 'rxjs/operators';
import {environment} from 'src/environments/environment';

@Injectable({
    providedIn: 'root'
})
export class BreadcrumbService {

    private currentBreadcrumbs: Subject<Breadcrumb[]> = new BehaviorSubject([]);

    constructor() {
    }

    public getCurrentBreadcrumbs(): Observable<Breadcrumb[]> {
        return this.currentBreadcrumbs;
    }

    public setCurrentBreadcrumbs(breadcrumbs: Breadcrumb[]): void {
        this.currentBreadcrumbs.next([homeBreadcrumb, studentInGentBreadcrumb, studerenBreadcrumb, blokLocatiesBreadcrumb, ...breadcrumbs]);
    }
}

export const blokLocatiesBreadcrumb = !environment.useExternalDashboard ? {
    pageName: 'Dashboard',
    url: '/dashboard',
    external: false
} : {
    pageName: 'Bloklocaties',
    url: environment.externalDashboardUrl,
    external: true
};

export const managementBreadcrumb = {
    pageName: 'Management',
    url: '/management'
};

const homeBreadcrumb = {
    pageName: 'Home',
    url: 'https://stad.gent/nl',
    external: true
};

const studentInGentBreadcrumb = {
    pageName: 'Student in Gent',
    url: 'https://stad.gent/nl/student-gent',
    external: true,
};

const studerenBreadcrumb = {
    pageName: 'Studeren',
    url: 'https://stad.gent/nl/student-gent/studeren',
    external: true
};

export interface Breadcrumb {
    pageName: string;
    url: string;
}
