import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MomentDateComponent } from './moment-date.component';

describe('MomentDateComponent', () => {
  let component: MomentDateComponent;
  let fixture: ComponentFixture<MomentDateComponent>;

  beforeEach(async(() => {
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
