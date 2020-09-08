import { TestBed } from '@angular/core/testing';

import { ApplicationTypeGuardService } from './application-type-guard.service';

describe('ApplicationTypeGuardService', () => {
  let service: ApplicationTypeGuardService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ApplicationTypeGuardService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
