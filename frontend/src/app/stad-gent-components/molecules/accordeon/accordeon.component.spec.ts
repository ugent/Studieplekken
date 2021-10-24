import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AccordeonComponent } from './accordeon.component';

describe('AccordeonComponent', () => {
  let component: AccordeonComponent;
  let fixture: ComponentFixture<AccordeonComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AccordeonComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AccordeonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
