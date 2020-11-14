import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MomentDatetimeComponent } from './moment-datetime.component';

describe('MomentDatetimeComponent', () => {
  let component: MomentDatetimeComponent;
  let fixture: ComponentFixture<MomentDatetimeComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MomentDatetimeComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MomentDatetimeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
