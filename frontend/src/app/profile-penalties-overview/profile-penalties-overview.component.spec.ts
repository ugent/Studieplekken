import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {ProfilePenaltiesOverviewComponent} from './profile-penalties-overview.component';
import {AuthenticationService} from '../../services/authentication.service';
import AuthenticationServiceStub from '../../services/stubs/AuthenticationServiceStub';
import {PenaltyService} from '../../services/penalty.service';
import PenaltyServiceStub from '../../services/stubs/PenaltyServiceStub';

describe('ProfilePenaltiesOverviewComponent', () => {
  let component: ProfilePenaltiesOverviewComponent;
  let fixture: ComponentFixture<ProfilePenaltiesOverviewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ProfilePenaltiesOverviewComponent,],
      providers: [{provide: AuthenticationService, useClass: AuthenticationServiceStub},
        {provide: PenaltyService, useClass: PenaltyServiceStub}]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProfilePenaltiesOverviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
