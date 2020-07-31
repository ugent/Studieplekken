import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ManagementComponent } from './management.component';
import {HttpClientModule} from '@angular/common/http';
import {FormBuilder} from '@angular/forms';
import {AuthenticationService} from '../../services/authentication.service';
import AuthenticationServiceStub from '../../services/stubs/AuthenticationServiceStub';

describe('ManagementComponent', () => {
  let component: ManagementComponent;
  let fixture: ComponentFixture<ManagementComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ManagementComponent ],
      imports: [HttpClientModule],
      providers: [FormBuilder, {provide: AuthenticationService, useClass: AuthenticationServiceStub}]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
