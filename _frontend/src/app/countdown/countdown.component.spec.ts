import {async, ComponentFixture, fakeAsync, TestBed, tick} from '@angular/core/testing';

import {CountdownComponent} from './countdown.component';
import {CustomDate, dateToIDate} from '../../interfaces/CustomDate';
import {DebugElement} from '@angular/core';
import {By} from '@angular/platform-browser';
import {TranslateModule, TranslateService, TranslateStore} from '@ngx-translate/core';

describe('CountdownComponent', () => {
  let component: CountdownComponent;
  let fixture: ComponentFixture<CountdownComponent>;
  let day = {
    date: new CustomDate(), openForReservationDate: dateToIDate(new Date()),
    openingHour: {hours: 10, minutes: 10}, closingHour: {hours: 10, minutes: 10}
  };

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [CountdownComponent],
      providers: [TranslateService, TranslateStore],
      imports: [TranslateModule.forChild()]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CountdownComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should show time if time until reservation opening is less than an hour', () => {
    let d = new Date();
    d.setMinutes(d.getMinutes() + 30);
    d.setSeconds(0);
    day.openForReservationDate = dateToIDate(d);
    component.day = day;
    component.ngOnInit();
    fixture.detectChanges();
    expect(component.visible.getValue()).toBe(true, "component should not be visible");
    const bannerDe: DebugElement = fixture.debugElement;
    const bannerEl: HTMLElement = bannerDe.nativeElement;
    const el = bannerEl.querySelector("#timeUntilOpenForReservation");
   // let el = fixture.nativeElement.querySelector(By.css("#timeUntilOpenForReservation"));
    component.decrementTimer();
    fixture.detectChanges(); // less than a minute should have  passed
    expect(component.minsTill <  30).toBeTrue();
  });
})
;
