import {Component, Input, OnInit} from '@angular/core';
import {Observable} from 'rxjs';
import {Location} from '../../../../shared/model/Location';

@Component({
  selector: 'app-lockers-calendar',
  templateUrl: './lockers-calendar.component.html',
  styleUrls: ['./lockers-calendar.component.css']
})
export class LockersCalendarComponent implements OnInit {
  @Input() location: Observable<Location>;

  constructor() { }

  ngOnInit(): void {
  }

}
