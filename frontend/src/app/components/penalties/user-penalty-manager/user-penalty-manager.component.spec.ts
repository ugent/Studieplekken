import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UserPenaltyManagerComponent } from './user-penalty-manager.component';

describe('UserPenaltyManagerComponent', () => {
  let component: UserPenaltyManagerComponent;
  let fixture: ComponentFixture<UserPenaltyManagerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ UserPenaltyManagerComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(UserPenaltyManagerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
