import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ProfileHelpComponent } from './profile-help.component';
import {LocationReservationService} from '../../services/location-reservation.service';
import LocationReservationServiceStub from '../../services/stubs/LocationReservationServiceStub';
import {TranslateModule, TranslateService, TranslateStore} from '@ngx-translate/core';
import {PenaltyService} from '../../services/penalty.service';
import PenaltyServiceStub from '../../services/stubs/PenaltyServiceStub';

describe('ProfileHelpComponent', () => {
  let component: ProfileHelpComponent;
  let fixture: ComponentFixture<ProfileHelpComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ProfileHelpComponent ],
      providers: [ {provide: LocationReservationService, useClass: LocationReservationServiceStub}, TranslateService, TranslateStore,
        {provide: PenaltyService, useClass: PenaltyServiceStub}],
      imports: [TranslateModule.forChild()]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProfileHelpComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
