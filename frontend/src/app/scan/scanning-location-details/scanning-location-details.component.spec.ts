import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ScanningLocationDetailsComponent } from './scanning-location-details.component';

describe('ScanningLocationDetailsComponent', () => {
  let component: ScanningLocationDetailsComponent;
  let fixture: ComponentFixture<ScanningLocationDetailsComponent>;

  beforeEach(async(() => {
    void TestBed.configureTestingModule({
      declarations: [ScanningLocationDetailsComponent],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ScanningLocationDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
