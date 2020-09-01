import { TestBed } from '@angular/core/testing';

import { PenaltyService } from './penalty.service';

describe('PenaltyService', () => {
  let service: PenaltyService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PenaltyService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
