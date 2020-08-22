import { TestBed } from '@angular/core/testing';

import { PenaltiesService } from './penalties.service';

describe('PenaltiesService', () => {
  let service: PenaltiesService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PenaltiesService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
