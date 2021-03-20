import { TestBed } from '@angular/core/testing';

import { ScanningService } from './scanning.service';

describe('ScanningService', () => {
  let service: ScanningService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ScanningService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
