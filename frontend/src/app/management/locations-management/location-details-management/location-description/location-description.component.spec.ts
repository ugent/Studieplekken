import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { LocationDescriptionComponent } from './location-description.component';

describe('LocationDescriptionComponent', () => {
  let component: LocationDescriptionComponent;
  let fixture: ComponentFixture<LocationDescriptionComponent>;

  beforeEach(async(() => {
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
