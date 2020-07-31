import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { VerificationComponent } from './verification.component';
import {VerificationService} from '../../services/verification.service';
import VerificationServiceStub from '../../services/stubs/VerificationServiceStub';

describe('VerificationComponent', () => {
  let component: VerificationComponent;
  let fixture: ComponentFixture<VerificationComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ VerificationComponent ],
      providers: [{provide: VerificationService, useClass: VerificationServiceStub}]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(VerificationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
