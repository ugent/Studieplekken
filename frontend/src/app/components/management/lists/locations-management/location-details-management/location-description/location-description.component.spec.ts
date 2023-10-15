import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { LocationDescriptionComponent } from './location-description.component';

describe('LocationDescriptionComponent', () => {
  let component: LocationDescriptionComponent;
  let fixture: ComponentFixture<LocationDescriptionComponent>;

  beforeEach(waitForAsync(() => {
    void TestBed.configureTestingModule({
      declarations: [LocationDescriptionComponent],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LocationDescriptionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
