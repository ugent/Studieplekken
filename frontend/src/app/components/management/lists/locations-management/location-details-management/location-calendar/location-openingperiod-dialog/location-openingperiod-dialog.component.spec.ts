import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { LocationOpeningperiodDialogComponent } from './location-openingperiod-dialog.component';

describe('LocationOpeningperiodDialogComponent', () => {
  let component: LocationOpeningperiodDialogComponent;
  let fixture: ComponentFixture<LocationOpeningperiodDialogComponent>;

  beforeEach(waitForAsync(() => {
    void TestBed.configureTestingModule({
      declarations: [LocationOpeningperiodDialogComponent],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = void TestBed.createComponent(
      LocationOpeningperiodDialogComponent
    );
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
