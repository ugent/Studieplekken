import { Component, OnInit } from '@angular/core';
import { BreadcrumbService } from '../stad-gent-components/header/breadcrumbs/breadcrumb.service';

@Component({
    selector: 'app-scan',
    templateUrl: './scan.component.html',
    styleUrls: ['./scan.component.scss'],
})
export class ScanComponent implements OnInit {

    constructor(private breadcrumbs: BreadcrumbService) { }


    ngOnInit(): void {
        this.breadcrumbs.setCurrentBreadcrumbs([{ pageName: "Scan Overview", url: "/scan/locations" }])
    }
}
