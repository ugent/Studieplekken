import { TestBed } from '@angular/core/testing';

import { ApplicationTypeFunctionalityService } from './application-type-functionality.service';

describe('ApplicationTypeFunctionalityService', () => {
  let service: ApplicationTypeFunctionalityService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ApplicationTypeFunctionalityService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
