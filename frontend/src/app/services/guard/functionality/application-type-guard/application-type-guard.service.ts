import { Injectable } from '@angular/core';
import {
  ActivatedRouteSnapshot,
  CanActivate,
  Router,
  RouterStateSnapshot,
} from '@angular/router';
import { ApplicationTypeFunctionalityService } from '../../../functionality/application-type/application-type-functionality.service';

@Injectable({
  providedIn: 'root',
})
export class ApplicationTypeGuardService implements CanActivate {
  constructor(
    private functionalityService: ApplicationTypeFunctionalityService,
    private router: Router
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): boolean {
    const url: string = state.url;
    let activate = false;
    if (url.startsWith('/profile')) {
      if (url.includes('/reservations')) {
        activate = this.functionalityService.showReservationsFunctionality();
      } else if (url.includes('/calendar')) {
        activate = this.functionalityService.showProfilePersonalCalendarFunctionality();
      } else if (url.includes('/password')) {
        activate = this.functionalityService.showChangePasswordFunctionality();
      } else if (url.includes('/penalties')) {
        activate = this.functionalityService.showPenaltyFunctionality();
      }
    } else if (url.startsWith('/scan')) {
      activate = this.functionalityService.showScanningFunctionality();
    } else if (url.startsWith('/management')) {
      if (url.includes('/reservations')) {
        activate = this.functionalityService.showReservationsFunctionality();
      } else if (url.includes('/penalties')) {
        activate = this.functionalityService.showPenaltyFunctionality();
      }
    }

    if (!activate) {
      this.router.navigate(['/']).catch(console.log);
    }

    return activate;
  }
}
