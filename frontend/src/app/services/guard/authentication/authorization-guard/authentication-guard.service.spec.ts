import { TestBed } from '@angular/core/testing';

import { AuthorizationGuardService } from './authorization-guard.service';

describe('AuthenticationGuardService', () => {
  let service: AuthorizationGuardService;

  beforeEach(() => {
    void TestBed.configureTestingModule({});
    service = TestBed.inject(AuthorizationGuardService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
