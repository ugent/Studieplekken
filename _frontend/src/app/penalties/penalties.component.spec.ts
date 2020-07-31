import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PenaltiesComponent } from './penalties.component';
import {HttpClientModule} from '@angular/common/http';
import {TranslateModule, TranslateService, TranslateStore} from '@ngx-translate/core';
import {AuthenticationService} from '../../services/authentication.service';
import AuthenticationServiceStub from '../../services/stubs/AuthenticationServiceStub';

import PenaltyServiceStub from '../../services/stubs/PenaltyServiceStub';
import {PenaltyService} from '../../services/penalty.service';
import {appLanguages, languageTranslations} from '../../environments/environment';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {of} from "rxjs";

describe('PenaltiesComponent', () => {
  let component: PenaltiesComponent;
  let fixture: ComponentFixture<PenaltiesComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PenaltiesComponent ],
      providers: [ TranslateService, TranslateStore,
        {provide: AuthenticationService, useClass: AuthenticationServiceStub},
        {provide: PenaltyService, useClass: PenaltyServiceStub}],
      imports: [TranslateModule.forChild(), BrowserAnimationsModule]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PenaltiesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should add a control in the form for all languages', () => {
    for(let lang of Object.keys(appLanguages)){
      expect(component.penaltyForm.controls["descriptions"]["controls"][appLanguages[lang]]).toBeTruthy();
    }
  });

  it('should only try to add a new penaltyEvent if the penaltyForm is valid', () => {
    let service = TestBed.inject(PenaltyService);
    spyOn(service, "addPenaltyEvent").and.returnValue(of(null));

    // initially form is empty and thus invalid
    component.addPenaltyEvent(component.penaltyForm.value);
    // new penaltyEvent should not have been added
    expect(service.addPenaltyEvent).toHaveBeenCalledTimes(0);

    for(let o of Object.keys(appLanguages)){
      component.penaltyForm.controls.descriptions['controls'][appLanguages[o]].setValue("Description");
    }
    component.penaltyForm.controls["code"].setValue(10);
    component.penaltyForm.controls["points"].setValue(10);
    component.penaltyForm.controls["publicAccessible"].setValue(true);

    // form should be valid now as all fields are filled in
    expect(component.penaltyForm.valid).toBeTrue();
    component.addPenaltyEvent(component.penaltyForm.value);
    fixture.detectChanges();
    // form is valid so it should try to add it
    expect(service.addPenaltyEvent).toHaveBeenCalled();
  });
});
