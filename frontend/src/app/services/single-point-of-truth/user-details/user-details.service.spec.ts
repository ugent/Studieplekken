import { TestBed } from '@angular/core/testing';

import { UserDetailsService } from './user-details.service';

describe('UserDetailsService', () => {
  let service: UserDetailsService;

  beforeEach(() => {
    void TestBed.configureTestingModule({});
    service = TestBed.inject(UserDetailsService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
