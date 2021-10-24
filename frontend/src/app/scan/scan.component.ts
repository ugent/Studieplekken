import { Component, OnInit } from '@angular/core';
import { BreadcrumbService, dashboardBreadcrumb } from '../stad-gent-components/header/breadcrumbs/breadcrumb.service';

@Component({
  selector: 'app-scan',
  templateUrl: './scan.component.html',
  styleUrls: ['./scan.component.scss'],
})
export class ScanComponent implements OnInit {

  constructor(private breadcrumbs: BreadcrumbService) {}


  ngOnInit(): void {
    this.breadcrumbs.setCurrentBreadcrumbs([dashboardBreadcrumb, {pageName: "Scan Overview", url:"/scan/locations"}])
  }
}
