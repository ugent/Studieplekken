import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ReservationsManagementComponent } from './reservations-management.component';

describe('ReservationsManagementComponent', () => {
  let component: ReservationsManagementComponent;
  let fixture: ComponentFixture<ReservationsManagementComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ReservationsManagementComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ReservationsManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
