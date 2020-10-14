import { TestBed } from '@angular/core/testing';

import { AuthoritiesService } from './authorities.service';

describe('AuthoritiesService', () => {
  let service: AuthoritiesService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AuthoritiesService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
