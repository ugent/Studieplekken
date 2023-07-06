import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LocationAddTimeslotDialogComponent } from './location-add-timeslot-dialog.component';

describe('LocationAddTimeslotDialogComponent', () => {
  let component: LocationAddTimeslotDialogComponent;
  let fixture: ComponentFixture<LocationAddTimeslotDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ LocationAddTimeslotDialogComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(LocationAddTimeslotDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
