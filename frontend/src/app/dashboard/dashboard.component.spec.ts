import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DashboardComponent } from './dashboard.component';
import {HttpClientModule} from '@angular/common/http';
import {AuthenticationService} from '../../services/authentication.service';
import {LocationService} from '../../services/location.service';
import {FormBuilder} from '@angular/forms';
import LocationServiceStub from '../../services/stubs/LocationServiceStub';
import AuthenticationServiceStub from '../../services/stubs/AuthenticationServiceStub';

describe('DashboardComponent', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DashboardComponent ],
      providers: [HttpClientModule, { provide: AuthenticationService, useClass: AuthenticationServiceStub}, { provide: LocationService, useClass: LocationServiceStub}, FormBuilder],
      imports: [HttpClientModule]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
