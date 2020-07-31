import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { LockerReservationOverviewComponent } from './locker-reservation-overview.component';
import {LockerReservationService} from '../../services/locker-reservation.service';
import {TranslateModule, TranslateService, TranslateStore} from '@ngx-translate/core';
import {HttpClientModule} from '@angular/common/http';
import {IUser} from '../../interfaces/IUser';
import LockerReservationServiceStub from '../../services/stubs/LockerReservationServiceStub';

describe('LockerReservationOverviewComponent', () => {
  let component: LockerReservationOverviewComponent;
  let fixture: ComponentFixture<LockerReservationOverviewComponent>;



  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ LockerReservationOverviewComponent ],
      providers: [{ provide: LockerReservationService, useClass: LockerReservationServiceStub}, TranslateService, HttpClientModule, TranslateStore],
      imports: [HttpClientModule, TranslateModule.forChild()]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LockerReservationOverviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });


});
