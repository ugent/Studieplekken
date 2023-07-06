import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { MomentTimeslotSizeComponent } from './moment-timeslot-size.component';

describe('MomentTimeslotSizeComponent', () => {
  let component: MomentTimeslotSizeComponent;
  let fixture: ComponentFixture<MomentTimeslotSizeComponent>;

  beforeEach(waitForAsync(() => {
    void TestBed.configureTestingModule({
      declarations: [MomentTimeslotSizeComponent],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MomentTimeslotSizeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
