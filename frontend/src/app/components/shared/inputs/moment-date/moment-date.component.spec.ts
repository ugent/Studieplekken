import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { MomentDateComponent } from './moment-date.component';

describe('MomentDateComponent', () => {
  let component: MomentDateComponent;
  let fixture: ComponentFixture<MomentDateComponent>;

  beforeEach(waitForAsync(() => {
    void TestBed.configureTestingModule({
      declarations: [MomentDateComponent],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MomentDateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
