import {Component, Input, OnInit} from '@angular/core';
import {CalendarView, CalendarEvent, CalendarEventTimesChangedEvent} from 'angular-calendar';
import {Subject} from 'rxjs';
import {TranslateService} from '@ngx-translate/core';

@Component({
  selector: 'app-calendar',
  templateUrl: './calendar.component.html',
  styleUrls: ['./calendar.component.css']
})
export class CalendarComponent implements OnInit {
  view: CalendarView = CalendarView.Week;
  viewDate: Date = new Date();

  CalendarView = CalendarView;

  @Input() events: CalendarEvent[];
  @Input() refresh: Subject<any>;

  currentLang: string;

  constructor(private translate: TranslateService) {
    this.translate.onLangChange.subscribe(
      () => {
        this.currentLang = this.translate.currentLang;
      }
    );
  }

  ngOnInit(): void {
    this.currentLang = this.translate.currentLang;
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

  setView(view: CalendarView): void {
    this.view = view;
  }
}
