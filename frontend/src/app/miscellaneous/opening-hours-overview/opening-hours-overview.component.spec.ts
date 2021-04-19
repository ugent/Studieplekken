import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { OpeningHoursOverviewComponent } from './opening-hours-overview.component';

describe('OpeningHoursOverviewComponent', () => {
  let component: OpeningHoursOverviewComponent;
  let fixture: ComponentFixture<OpeningHoursOverviewComponent>;

  beforeEach(async(() => {
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
