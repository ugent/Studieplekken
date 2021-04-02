import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TimeslotTableComponent } from './timeslot-table.component';

describe('TimeslotTableComponent', () => {
  let component: TimeslotTableComponent;
  let fixture: ComponentFixture<TimeslotTableComponent>;

  beforeEach(async(() => {
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
