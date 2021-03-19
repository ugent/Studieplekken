import { Injectable } from '@angular/core';
import { LocationReservation } from '../shared/model/LocationReservation';

@Injectable({
  providedIn: 'root'
})
export class BarcodeService {

  constructor() { }

  public isValid(rlist: LocationReservation[], code: string): boolean {
    return !!this.getReservation(rlist, code)
  }

  public getReservation(rlist: LocationReservation[], code: string): LocationReservation {
    const match = (f, r) => r.user.augentID === f(code)
    return rlist.find(r => match(this.fromEAN13, r) || match(this.fromCODE128, r) || match(this.fromUPCA, r))  || null
  }


  private fromEAN13(code: string) {
    return code.substring(0, code.length-1)
  }

  private fromCODE128(code: string) {
    return code
  }

  private fromUPCA(code: string) {
    return "0" + code.substring(0, code.length - 1)
  }
}
