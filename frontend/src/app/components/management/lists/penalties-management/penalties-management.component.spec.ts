import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PenaltiesManagementComponent } from './penalties-management.component';

describe('PenaltiesManagementComponent', () => {
  let component: PenaltiesManagementComponent;
  let fixture: ComponentFixture<PenaltiesManagementComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PenaltiesManagementComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PenaltiesManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
