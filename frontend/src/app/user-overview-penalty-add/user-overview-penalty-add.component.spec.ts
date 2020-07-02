import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { UserOverviewPenaltyAddComponent } from './user-overview-penalty-add.component';
import {PenaltyService} from '../../services/penalty.service';
import PenaltyServiceStub from '../../services/stubs/PenaltyServiceStub';
import {LocationService} from '../../services/location.service';
import LocationServiceStub from '../../services/stubs/LocationServiceStub';
import {TranslateModule, TranslateService, TranslateStore} from '@ngx-translate/core';
import {of} from 'rxjs';

describe('UserOverviewPenaltyAddComponent', () => {
  let component: UserOverviewPenaltyAddComponent;
  let fixture: ComponentFixture<UserOverviewPenaltyAddComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ UserOverviewPenaltyAddComponent],
      providers: [ {provide: PenaltyService, useClass: PenaltyServiceStub},
        {provide: PenaltyService, useClass: PenaltyServiceStub},
        {provide: LocationService, useClass: LocationServiceStub},
      TranslateService, TranslateStore],
      imports: [TranslateModule.forChild()]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UserOverviewPenaltyAddComponent);
    component = fixture.componentInstance;
    component.display = of("test");
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
