import { TestBed } from '@angular/core/testing';

import { AuthorityToManageService } from './authority-to-manage.service';

describe('AuthorityToManageService', () => {
  let service: AuthorityToManageService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AuthorityToManageService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
