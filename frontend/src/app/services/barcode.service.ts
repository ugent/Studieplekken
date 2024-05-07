import { Injectable } from '@angular/core';
import { LocationReservation } from '../model/LocationReservation';

@Injectable({
  providedIn: 'root',
})
export class BarcodeService {
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

  public getReservation(
    rlist: LocationReservation[],
    code: string
  ): LocationReservation {
    const match = (f: (code: string) => string, r: LocationReservation) =>
      r.user.userId === f(code);
    return (
      rlist.find(
        (r) =>
          match(BarcodeService.fromEAN13.bind(this), r) ||
          match(BarcodeService.fromCODE128.bind(this), r) ||
          match(BarcodeService.fromUPCA.bind(this), r)
      ) || null
    );
  }
}
