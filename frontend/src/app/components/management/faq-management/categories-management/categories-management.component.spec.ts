import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CategoriesManagementComponent } from './categories-management.component';

describe('CategoriesManagementComponent', () => {
  let component: CategoriesManagementComponent;
  let fixture: ComponentFixture<CategoriesManagementComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CategoriesManagementComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CategoriesManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
