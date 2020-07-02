import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ScanForStudentComponent } from './scan-for-student.component';
import {LocationReservationService} from '../../services/location-reservation.service';
import LocationReservationServiceStub from '../../services/stubs/LocationReservationServiceStub';
import {TranslateModule, TranslateService, TranslateStore} from '@ngx-translate/core';
import {AuthenticationService} from '../../services/authentication.service';
import AuthenticationServiceStub from '../../services/stubs/AuthenticationServiceStub';
import {RouterModule} from '@angular/router';

describe('ScanForStudentComponent', () => {
  let component: ScanForStudentComponent;
  let fixture: ComponentFixture<ScanForStudentComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ScanForStudentComponent ],
      providers: [{provide: LocationReservationService, useClass: LocationReservationServiceStub},
      TranslateService, TranslateStore,
        {provide: AuthenticationService, useClass: AuthenticationServiceStub}],
      imports: [TranslateModule.forChild(), RouterModule.forRoot([])]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ScanForStudentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
