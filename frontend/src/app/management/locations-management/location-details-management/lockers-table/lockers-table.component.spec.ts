import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { LockersTableComponent } from './lockers-table.component';

describe('LockersTableComponent', () => {
  let component: LockersTableComponent;
  let fixture: ComponentFixture<LockersTableComponent>;

  beforeEach(waitForAsync(() => {
    void TestBed.configureTestingModule({
      declarations: [LockersTableComponent],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LockersTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
