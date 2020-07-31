import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { UserOverviewPenaltyComponent } from './user-overview-penalty.component';
import {AuthenticationService} from '../../services/authentication.service';
import AuthenticationServiceStub from '../../services/stubs/AuthenticationServiceStub';
import {PenaltyService} from '../../services/penalty.service';
import PenaltyServiceStub from '../../services/stubs/PenaltyServiceStub';
import {of} from 'rxjs';

describe('UserOverviewPenaltyComponent', () => {
  let component: UserOverviewPenaltyComponent;
  let fixture: ComponentFixture<UserOverviewPenaltyComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ UserOverviewPenaltyComponent ],
      providers: [{provide: AuthenticationService, useClass: AuthenticationServiceStub},
        {provide: PenaltyService, useClass: PenaltyServiceStub}]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UserOverviewPenaltyComponent);
    component = fixture.componentInstance;
    component.display = of("test");
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
