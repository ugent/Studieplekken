import {Component, OnInit} from '@angular/core';
import {BreadcrumbService} from '../stad-gent-components/header/breadcrumbs/breadcrumb.service';

@Component({
    selector: 'app-profile',
    templateUrl: './profile.component.html',
    styleUrls: ['./profile.component.scss'],
})
export class ProfileComponent implements OnInit {
    constructor(
        private breadcrumbService: BreadcrumbService
    ) {
    }

    ngOnInit(): void {
        this.breadcrumbService.setCurrentBreadcrumbs([{pageName: 'Profile', url: '/profile/overview'}]);
    }
}
