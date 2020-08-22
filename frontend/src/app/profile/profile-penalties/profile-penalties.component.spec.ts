import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ProfilePenaltiesComponent } from './profile-penalties.component';

describe('ProfilePenaltiesComponent', () => {
  let component: ProfilePenaltiesComponent;
  let fixture: ComponentFixture<ProfilePenaltiesComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ProfilePenaltiesComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProfilePenaltiesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
