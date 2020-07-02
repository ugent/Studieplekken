import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {LocationItemComponent} from './location-item.component';
import {AuthenticationService} from '../../services/authentication.service';
import {LocationReservationService} from '../../services/location-reservation.service';
import {HttpClientModule} from '@angular/common/http';
import LocationReservationServiceStub from '../../services/stubs/LocationReservationServiceStub';

describe('LocationItemComponent', () => {
  let component: LocationItemComponent;
  let fixture: ComponentFixture<LocationItemComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [LocationItemComponent],
      imports: [HttpClientModule],
      providers: [AuthenticationService, { provide: LocationReservationService, useClass: LocationReservationServiceStub}, HttpClientModule]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LocationItemComponent);
    component = fixture.componentInstance;
    component.location = {
      startPeriodLockers: null,
      endPeriodLockers: null,
      'name': 'Faculteit Bio-Ingenieurs-wetenschappen',
      'address': 'Resto Agora, Coupure 653 (gelijkvloers gebouw E)',
      'numberOfSeats': 400,
      'numberOfLockers': 67,
      'mapsFrame': 'https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d2508.461346449728!2d3.725643616041962!3d51.04456867956177!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x47c371505918a89f%3A0x215e897a80bba60d!2sStudentenhuis%20De%20Therminal!5e0!3m2!1snl!2sbe!4v1582414683281!5m2!1snl!2sbe',
      'descriptions': {},
      'imageUrl': 'https://www.schamper.ugent.be/files/styles/article/public/images/icoon_UGent_BW_NL_RGB_2400_kleur.png?itok=4mHGEwLM',
      'calendar': [{
        'date': {'year': 2020, 'month': 4, 'day': 10, 'hrs': 10, 'min': 0, 'sec': 0},
        'openingHour': {'hours': 10, 'minutes': 0},
        'closingHour': {'hours': 18, 'minutes': 0},
        'openForReservationDate': {'year': 2020, 'month': 4, 'day': 10, 'hrs': 6, 'min': 0, 'sec': 0}
      }],
      'lockers': []
    };

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
