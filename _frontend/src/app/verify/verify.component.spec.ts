import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { VerifyComponent } from './verify.component';
import {RouterModule} from '@angular/router';
import {VerificationService} from '../../services/verification.service';
import VerificationServiceStub from '../../services/stubs/VerificationServiceStub';

describe('VerifyComponent', () => {
  let component: VerifyComponent;
  let fixture: ComponentFixture<VerifyComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ VerifyComponent ],
      providers:[{provide: VerificationService, useClass: VerificationServiceStub}],
      imports: [RouterModule.forRoot([])]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(VerifyComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
