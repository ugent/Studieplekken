import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ScanningLocationsComponent } from './scanning-locations.component';

describe('ScanningLocationsComponent', () => {
  let component: ScanningLocationsComponent;
  let fixture: ComponentFixture<ScanningLocationsComponent>;

  beforeEach(waitForAsync(() => {
    void TestBed.configureTestingModule({
      declarations: [ScanningLocationsComponent],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ScanningLocationsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
