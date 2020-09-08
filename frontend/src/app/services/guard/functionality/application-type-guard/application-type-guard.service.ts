import { Injectable } from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, Router} from '@angular/router';
import {ApplicationTypeFunctionalityService} from '../../../functionality/application-type/application-type-functionality.service';

@Injectable({
  providedIn: 'root'
})
export class ApplicationTypeGuardService implements CanActivate {

  constructor(private functionalityService: ApplicationTypeFunctionalityService,
              private router: Router) { }

  canActivate(route: ActivatedRouteSnapshot): boolean {
    const applicationPart = route.data.applicationPart;
    let activate = false;

    switch (applicationPart) {
      case 'penalties':
        activate = this.functionalityService.showPenaltyFunctionality();
        break;
      case 'reservations':
        activate = this.functionalityService.showReservationsFunctionality();
        break;
      case 'scanning':
        activate = this.functionalityService.showScanningFunctionality();
        break;
    }

    if (!activate) {
      this.router.navigate(['/']).catch();
    }

    return activate;
  }
}
