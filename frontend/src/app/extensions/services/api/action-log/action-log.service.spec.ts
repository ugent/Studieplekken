import { TestBed } from '@angular/core/testing';

import { ActionLogService } from '../actions-managament.service';

describe('ActionLogService', () => {
  let service: ActionLogService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ActionLogService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
