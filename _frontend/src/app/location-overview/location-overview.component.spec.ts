import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { LocationOverviewComponent } from './location-overview.component';
import {HttpClientModule} from '@angular/common/http';
import {LocationReservationService} from '../../services/location-reservation.service';
import {IUser} from '../../interfaces/IUser';
import {AuthenticationService} from '../../services/authentication.service';
import AuthenticationServiceStub from '../../services/stubs/AuthenticationServiceStub';
import LocationReservationServiceStub from '../../services/stubs/LocationReservationServiceStub';

describe('LocationOverviewComponent', () => {
  let component: LocationOverviewComponent;
  let fixture: ComponentFixture<LocationOverviewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ LocationOverviewComponent ],
      providers: [{ provide: LocationReservationService, useClass: LocationReservationServiceStub}, HttpClientModule, {provide: AuthenticationService, useClass: AuthenticationServiceStub}],
      imports: [HttpClientModule]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LocationOverviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
