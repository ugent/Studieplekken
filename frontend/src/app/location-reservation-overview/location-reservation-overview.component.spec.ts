import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {LocationReservationOverviewComponent} from './location-reservation-overview.component';
import {LocationReservationService} from '../../services/location-reservation.service';
import {TranslateModule, TranslateService, TranslateStore} from '@ngx-translate/core';
import {HttpClientModule} from '@angular/common/http';
import LocationReservationServiceStub from '../../services/stubs/LocationReservationServiceStub';

describe('LocationReservationOverviewComponent', () => {
  let component: LocationReservationOverviewComponent;
  let fixture: ComponentFixture<LocationReservationOverviewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [LocationReservationOverviewComponent],
      providers: [{provide: LocationReservationService, useClass: LocationReservationServiceStub}, TranslateService, TranslateStore],
      imports: [TranslateModule.forChild()]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LocationReservationOverviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

});
