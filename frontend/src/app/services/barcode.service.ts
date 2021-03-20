import { Injectable } from '@angular/core';
import { LocationReservation } from '../shared/model/LocationReservation';

@Injectable({
  providedIn: 'root'
})
export class BarcodeService {

  constructor() { }

  private static fromEAN13(code: string): string {
    return code.substring(0, code.length - 1);
  }

  private static fromCODE128(code: string): string {
    return code;
  }

  private static fromUPCA(code: string): string {
    return '0' + code.substring(0, code.length - 1);
  }

  public isValid(rlist: LocationReservation[], code: string): boolean {
    return !!this.getReservation(rlist, code);
  }

  public getReservation(rlist: LocationReservation[], code: string): LocationReservation {
    const match = (f, r) => r.user.augentID === f(code);
    return rlist.find(r => match(BarcodeService.fromEAN13, r) || match(BarcodeService.fromCODE128, r)
      || match(BarcodeService.fromUPCA, r)) || null;
  }
}
