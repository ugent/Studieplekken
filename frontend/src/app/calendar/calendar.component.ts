import {Component, EventEmitter, Input, OnInit, Output, OnChanges, SimpleChanges} from '@angular/core';
import {CalendarView, CalendarEvent, CalendarEventTimesChangedEvent} from 'angular-calendar';
import {BehaviorSubject, Subject} from 'rxjs';
import {TranslateService} from '@ngx-translate/core';
import {defaultOpeningHour, defaultClosingHour} from 'src/app/app.constants';
import { getHours } from 'date-fns';

@Component({
  selector: 'app-calendar',
  templateUrl: './calendar.component.html',
  styleUrls: ['./calendar.component.css']
})
export class CalendarComponent implements OnInit, OnChanges {
  view: CalendarView = CalendarView.Week;
  viewDate: Date = new Date();

  CalendarView = CalendarView;

  openingHour: number;
  closingHour: number;

  @Input() events: CalendarEvent[];
  @Input() refresh: Subject<any>;
  @Output() onTimeslotPicked: EventEmitter<any> = new EventEmitter<any>();

  eventsSubj: BehaviorSubject<CalendarEvent[]> = new BehaviorSubject<CalendarEvent[]>([]);

  currentLang: string;

  constructor(private translate: TranslateService) {
    this.translate.onLangChange.subscribe(() => {this.currentLang = this.translate.currentLang;});
  }

  ngOnInit(): void {
    this.currentLang = this.translate.currentLang;
    this.eventsSubj.subscribe(next => {
      this.changeCalendarSize(next);
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.hasOwnProperty('events')) {
      this.eventsSubj.next(this.events);
    }
  }

  changeCalendarSize(events: CalendarEvent[]): void {
    if (events.length > 0) {
      const endHour = this.events.map(event => event.end?.getHours()).reduce((a, b) => Math.max(a, b));
      const beginHour = this.events.map(event => event.start.getHours()).reduce((a, b) => Math.min(a, b));
      this.openingHour = beginHour < defaultOpeningHour ? beginHour : defaultOpeningHour;
      this.closingHour = endHour > defaultClosingHour ? endHour : defaultClosingHour;
    } else {
      this.openingHour = defaultOpeningHour;
      this.closingHour = defaultClosingHour;
    }
  }

  dayClicked({ date, events }: { date: Date ; events: CalendarEvent[] }): void {}

  handleEvent(action: string, event: CalendarEvent): void {
    this.onTimeslotPicked.emit(event.meta);
  }

  eventTimesChanged({event, newStart, newEnd, }: CalendarEventTimesChangedEvent): void {}

  setView(view: CalendarView): void {
    this.view = view;
  }
}
