import { Component, OnInit } from '@angular/core';
import {CalendarEvent} from 'angular-calendar';

@Component({
  selector: 'app-profile-calendar',
  templateUrl: './profile-calendar.component.html',
  styleUrls: ['./profile-calendar.component.css']
})
export class ProfileCalendarComponent implements OnInit {
  events: CalendarEvent[] = [];

  constructor() { }

  ngOnInit(): void {
  }

}
