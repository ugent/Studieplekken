import { TestBed } from '@angular/core/testing';

import { LocationDetailsService } from './location-details.service';

describe('LocationDetailsService', () => {
  let service: LocationDetailsService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(LocationDetailsService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
