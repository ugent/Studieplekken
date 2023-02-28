import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { OpeningHoursOverviewComponent } from './opening-hours-overview.component';

describe('OpeningHoursOverviewComponent', () => {
  let component: OpeningHoursOverviewComponent;
  let fixture: ComponentFixture<OpeningHoursOverviewComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ OpeningHoursOverviewComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(OpeningHoursOverviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
