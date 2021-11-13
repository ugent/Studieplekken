import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MobileTableComponent } from './mobile-table.component';

describe('MobileTableComponent', () => {
  let component: MobileTableComponent;
  let fixture: ComponentFixture<MobileTableComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MobileTableComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MobileTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
