import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import {BehaviorSubject} from "rxjs";
import {IDay} from "../../interfaces/IDay";
import {TranslateService} from "@ngx-translate/core";

@Component({
  selector: 'app-countdown',
  templateUrl: './countdown.component.html',
  styleUrls: ['./countdown.component.css']
})
export class CountdownComponent implements OnInit, OnDestroy {
  @Input() day: IDay;
  diffMillisecs: number;
  hrsTill: number=0;
  minsTill: number=0;
  secsTill: number=0;
  timer: any;
  visible = new BehaviorSubject(true);
  timeVisible = new BehaviorSubject(false);

  // translateservice is needed for the date format in the html code
  constructor(public translateService: TranslateService) {
  }

  ngOnInit(): void {
    if (this.day != null && this.day.openForReservationDate != null) {
      // if there is a next reservation that is not yet open
      const d = new Date(this.day.openForReservationDate.year, this.day.openForReservationDate.month - 1, this.day.openForReservationDate.day, this.day.openForReservationDate.hrs, this.day.openForReservationDate.min);
      this.diffMillisecs = (d.valueOf() - Date.now().valueOf());
      // if it opens within 24 hours, show a countdown timer
      if (this.diffMillisecs < 60 * 60 * 1000 * 24) {
        this.timeVisible.next(true);
        this.timer = setInterval(() => {
          this.decrementTimer();
        }, 1000);
      }
    }
  }

  ngOnDestroy(): void {
    clearInterval(this.timer);
  }

  // changes timer in html
  decrementTimer() {
    if (this.diffMillisecs >= 1000) {
      this.diffMillisecs -= 1000;
    } else {
      this.visible.next(false);
      clearInterval(this.timer);
    }
    this.secsTill = Math.floor((this.diffMillisecs % (60 * 1000)) / 1000);
    this.minsTill = Math.floor((this.diffMillisecs / (60 * 1000)) % 60);
    this.hrsTill = Math.floor((this.diffMillisecs / (60 * 60 * 1000)));
  }

  formatTime(nr: number): string {
    if (nr < 10) {
      return '0' + nr;
    }
    return '' + nr;
  }
}
