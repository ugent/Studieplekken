import {Injectable} from '@angular/core';

import { APPLICATION_TYPE, environment } from '../../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ApplicationTypeFunctionalityService {

  constructor() { }

  showPenaltyFunctionality(): boolean {
    switch (environment.applicationType) {
      case APPLICATION_TYPE.BLOK_AT:
        return true;
      case APPLICATION_TYPE.MINI_THERMIS:
        return false;
      default:
        return true;
    }
  }

  showScanningFunctionality(): boolean {
    switch (environment.applicationType) {
      case APPLICATION_TYPE.BLOK_AT:
        return true;
      case APPLICATION_TYPE.MINI_THERMIS:
        return true;
      default:
        return true;
    }
  }

  showReservationsFunctionality(): boolean {
    switch (environment.applicationType) {
      case APPLICATION_TYPE.BLOK_AT:
        return true;
      case APPLICATION_TYPE.MINI_THERMIS:
        return true;
      default:
        return true;
    }
  }

  showLockersManagementFunctionality(): boolean {
    switch (environment.applicationType) {
      case APPLICATION_TYPE.BLOK_AT:
        return true;
      case APPLICATION_TYPE.MINI_THERMIS:
        return false;
      default:
        return true;
    }
  }

  showProfilePersonalCalendarFunctionality(): boolean {
    switch (environment.applicationType) {
      case APPLICATION_TYPE.BLOK_AT:
        return true;
      case APPLICATION_TYPE.MINI_THERMIS:
        return false;
      default:
        return true;
    }
  }

  showChangePasswordFunctionality(): boolean {
    switch (environment.applicationType) {
      case APPLICATION_TYPE.BLOK_AT:
        return true;
      case APPLICATION_TYPE.MINI_THERMIS:
        return false;
      default:
        return true;
    }
  }
}
