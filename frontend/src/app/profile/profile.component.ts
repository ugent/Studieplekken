import { Component, OnInit } from '@angular/core';
import { ApplicationTypeFunctionalityService } from '../services/functionality/application-type/application-type-functionality.service';
import { BreadcrumbService, dashboardBreadcrumb } from '../stad-gent-components/header/breadcrumbs/breadcrumb.service';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.scss'],
})
export class ProfileComponent implements OnInit {
  showReservations: boolean;
  showPenalties: boolean;
  showPersonalCalendar: boolean;
  showChangePassword: boolean;

  constructor(
    private functionalityService: ApplicationTypeFunctionalityService,
    private breadcrumbService: BreadcrumbService
  ) {}

  ngOnInit(): void {
    this.showReservations = this.functionalityService.showReservationsFunctionality();
    this.showPenalties = this.functionalityService.showPenaltyFunctionality();
    this.showChangePassword = this.functionalityService.showChangePasswordFunctionality();
    this.showPersonalCalendar = this.functionalityService.showProfilePersonalCalendarFunctionality();
    this.breadcrumbService.setCurrentBreadcrumbs([dashboardBreadcrumb, {pageName:"Profile", url:"/profile/overview"}])
  }
}
