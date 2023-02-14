import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { MomentDateTimeComponent } from './moment-datetime.component';

describe('MomentDateTimeComponent', () => {
  let component: MomentDateTimeComponent;
  let fixture: ComponentFixture<MomentDateTimeComponent>;

  beforeEach(waitForAsync(() => {
    void TestBed.configureTestingModule({
      declarations: [MomentDateTimeComponent],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MomentDateTimeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
