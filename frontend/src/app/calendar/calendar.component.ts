import { Component, OnInit } from '@angular/core';
import {CalendarView, CalendarEvent, CalendarEventTimesChangedEvent} from 'angular-calendar';
import {Subject} from 'rxjs';

@Component({
  selector: 'app-calendar',
  templateUrl: './calendar.component.html',
  styleUrls: ['./calendar.component.css']
})
export class CalendarComponent implements OnInit {
  view: CalendarView = CalendarView.Month;
  viewDate: Date = new Date();

  events: CalendarEvent[];

  refresh: Subject<any> = new Subject();

  constructor() { }

  ngOnInit(): void {
  }

  dayClicked({ date, events }: { date: Date; events: CalendarEvent[] }): void {
    console.log('Clicked on date ' + date);
  }

  handleEvent(action: string, event: CalendarEvent): void {
    console.log('Event occurred, action: ' + action);
  }

  eventTimesChanged({event, newStart, newEnd, }: CalendarEventTimesChangedEvent): void {
    console.log('event times changed to start = ' + newStart + ' end = ' + newEnd);
  }
}
