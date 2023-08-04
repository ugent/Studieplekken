import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { TimeslotTableComponent } from './timeslot-table.component';

describe('TimeslotTableComponent', () => {
  let component: TimeslotTableComponent;
  let fixture: ComponentFixture<TimeslotTableComponent>;

  beforeEach(waitForAsync(() => {
    void TestBed.configureTestingModule({
      declarations: [TimeslotTableComponent],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TimeslotTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
