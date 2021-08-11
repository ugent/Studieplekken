import { TestBed } from '@angular/core/testing';

import { AddressresolverService } from './addressresolver.service';

describe('AddressresolverService', () => {
  let service: AddressresolverService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AddressresolverService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
