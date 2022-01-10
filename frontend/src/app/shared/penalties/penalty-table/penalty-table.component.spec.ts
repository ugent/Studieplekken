import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PenaltyTableComponent } from './penalty-table.component';

describe('PenaltyTableComponent', () => {
  let component: PenaltyTableComponent;
  let fixture: ComponentFixture<PenaltyTableComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PenaltyTableComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PenaltyTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
