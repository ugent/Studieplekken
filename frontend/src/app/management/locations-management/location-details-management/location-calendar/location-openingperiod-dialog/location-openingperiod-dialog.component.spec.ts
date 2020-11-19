import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { LocationOpeningperiodDialogComponent } from './location-openingperiod-dialog.component';

describe('LocationOpeningperiodDialogComponent', () => {
  let component: LocationOpeningperiodDialogComponent;
  let fixture: ComponentFixture<LocationOpeningperiodDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ LocationOpeningperiodDialogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LocationOpeningperiodDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
