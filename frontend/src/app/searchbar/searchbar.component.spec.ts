import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SearchbarComponent } from './searchbar.component';
import LockerReservationServiceStub from '../../services/stubs/LockerReservationServiceStub';
import {ILockerReservation} from '../../interfaces/ILockerReservation';
import {DebugElement} from '@angular/core';

describe('SearchbarComponent', () => {
  let component: SearchbarComponent;
  let fixture: ComponentFixture<SearchbarComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SearchbarComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SearchbarComponent);
    component = fixture.componentInstance;
    component.searchPossibilities = [{
      name: "location",
      translation: "management.locationName",
      searchFunction: "getAllLockerReservationsOfLocation"
    }];

    component.service = new LockerReservationServiceStub();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should emit an event with new results when search is clicked', () => {
    component.searchFor = "location";
    component.searchValue = "Therminal";
    component.resultsChanged.subscribe((results : ILockerReservation[]) => {
      expect(results.length).toBeGreaterThan(0);
    });
    component.search();
  });

  it("should make radio buttons with search options", () => {
    const bannerDe: DebugElement = fixture.debugElement;
    const bannerEl: HTMLElement = bannerDe.nativeElement;
    const h = bannerEl.querySelector('label');
    expect(h.textContent).toEqual(component.searchPossibilities[0].translation);
  });
});
