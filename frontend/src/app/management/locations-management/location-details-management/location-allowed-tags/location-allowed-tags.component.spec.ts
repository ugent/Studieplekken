import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { LocationAllowedTagsComponent } from './location-allowed-tags.component';

describe('LocationAllowedTagsComponent', () => {
  let component: LocationAllowedTagsComponent;
  let fixture: ComponentFixture<LocationAllowedTagsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ LocationAllowedTagsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LocationAllowedTagsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
