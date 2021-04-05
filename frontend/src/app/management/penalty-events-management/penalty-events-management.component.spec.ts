import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PenaltyEventsManagementComponent } from './penalty-events-management.component';

describe('PenaltyEventsManagementComponent', () => {
  let component: PenaltyEventsManagementComponent;
  let fixture: ComponentFixture<PenaltyEventsManagementComponent>;

  beforeEach(async(() => {
    void TestBed.configureTestingModule({
      declarations: [PenaltyEventsManagementComponent],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PenaltyEventsManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
