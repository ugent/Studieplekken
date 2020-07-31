import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { LockerOverviewComponent } from './locker-overview.component';
import {LockerReservationService} from '../../services/locker-reservation.service';
import {HttpClientModule} from '@angular/common/http';
import {IUser} from '../../interfaces/IUser';
import {AuthenticationService} from '../../services/authentication.service';
import AuthenticationServiceStub from '../../services/stubs/AuthenticationServiceStub';
import LockerReservationServiceStub from '../../services/stubs/LockerReservationServiceStub';

describe('LockerOverviewComponent', () => {
  let component: LockerOverviewComponent;
  let fixture: ComponentFixture<LockerOverviewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ LockerOverviewComponent ],
      providers: [{ provide: LockerReservationService, useClass: LockerReservationServiceStub}, {provide: AuthenticationService, useClass: AuthenticationServiceStub}],
      imports: []
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LockerOverviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
