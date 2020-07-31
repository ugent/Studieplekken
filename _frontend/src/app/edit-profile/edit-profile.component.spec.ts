import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {EditProfileComponent} from './edit-profile.component';
import {HttpClientModule} from '@angular/common/http';
import {AuthenticationService} from '../../services/authentication.service';
import {BarcodeService} from '../../services/barcode.service';
import {IUser} from '../../interfaces/IUser';
import AuthenticationServiceStub from '../../services/stubs/AuthenticationServiceStub';
import BarcodeServiceStub from '../../services/stubs/BarcodeServiceStub';

describe('EditProfileComponent', () => {
  let component: EditProfileComponent;
  let fixture: ComponentFixture<EditProfileComponent>;

  const barcodeStub= { getBarcodeImage: (img)=> {
    return null;
  }};

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [EditProfileComponent],
      imports: [],
      providers: [{provide: AuthenticationService, useClass: AuthenticationServiceStub},{ provide: BarcodeService, useClass: BarcodeServiceStub}]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EditProfileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should handle changing passwords correctly', () => {
    let service = TestBed.inject(AuthenticationService);
    spyOn(service, "updateOwnProfile");
    component.profileForm.controls["pwd"].setValue("short");
    component.profileForm.controls["confPwd"].setValue("short");
    fixture.detectChanges();
    component.editPassword = true;
    component.saveChanges(component.profileForm);

    // form wasn't valid, so this shouldnt have been called
    expect(service.updateOwnProfile).toHaveBeenCalledTimes(0);

    component.profileForm.controls["pwd"].setValue("goodpassword");
    component.profileForm.controls["confPwd"].setValue("goodpassword");
    fixture.detectChanges();
    // with a valid form it should try to change the users password
    component.saveChanges(component.profileForm);
    expect(service.updateOwnProfile).toHaveBeenCalledTimes(1);

  });
});
