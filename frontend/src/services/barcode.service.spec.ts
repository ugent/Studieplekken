import { TestBed } from '@angular/core/testing';

import { BarcodeService } from './barcode.service';
import {HttpClientModule} from "@angular/common/http";
import {HttpClientTestingModule, HttpTestingController} from "@angular/common/http/testing";
import {urls} from "../environments/environment";

describe('BarcodeService', () => {

  let service: BarcodeService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [HttpClientModule, BarcodeService],
      imports: [HttpClientModule, HttpClientTestingModule]
    });
    service = TestBed.get(BarcodeService);
    httpMock = TestBed.get(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('getBarcodeImage() should do a GET request', () =>{
    let content: string = "505";

    service.getBarcodeImage(content).subscribe((r)=> { });

    const request = httpMock.expectOne( `${urls.userBarcode + '/' + content}`);
    expect(request.request.method).toBe('GET');
  });

});
