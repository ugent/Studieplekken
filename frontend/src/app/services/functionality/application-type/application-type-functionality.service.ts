import {Injectable} from '@angular/core';
import {APPLICATION_TYPE, environment} from '../../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ApplicationTypeFunctionalityService {

  constructor() { }

  showPenaltyFunctionality(): boolean {
    return environment.applicationType === APPLICATION_TYPE.BLOK_AT;
  }

  showScanningFunctionality(): boolean {
    return environment.applicationType === APPLICATION_TYPE.BLOK_AT;
  }

  showReservationsFunctionality(): boolean {
    return environment.applicationType === APPLICATION_TYPE.BLOK_AT;
  }
}
