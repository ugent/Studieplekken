import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DashboardBoardComponent } from './dashboard-board.component';
import {LocationService} from '../../services/location.service';
import {HttpClientModule} from '@angular/common/http';
import LocationServiceStub from '../../services/stubs/LocationServiceStub';
import {LocationReservationService} from "../../services/location-reservation.service";
import LocationReservationServiceStub from "../../services/stubs/LocationReservationServiceStub";

describe('DashboardBoardComponent', () => {
  let component: DashboardBoardComponent;
  let fixture: ComponentFixture<DashboardBoardComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DashboardBoardComponent ],
      providers: [{ provide: LocationService, useClass: LocationServiceStub}, {provide: LocationReservationService, useClass: LocationReservationServiceStub}, HttpClientModule],
      imports: [HttpClientModule]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DashboardBoardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
