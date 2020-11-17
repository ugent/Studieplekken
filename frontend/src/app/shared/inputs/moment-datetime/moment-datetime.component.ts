import { Component, EventEmitter, Input, OnChanges, OnInit, Output } from '@angular/core';
import * as moment from 'moment';
import { Moment } from 'moment';

@Component({
  selector: 'app-moment-datetime',
  templateUrl: './moment-datetime.component.html',
  styleUrls: ['./moment-datetime.component.css']
})
export class MomentDateTimeComponent implements OnInit, OnChanges {

  @Input()
  model: Moment;
  @Input()
  disabled: boolean;
  @Output()
  modelChange: EventEmitter<Moment> = new EventEmitter();


  modelDateAsString: string;
  modelTimeAsString: string;


  constructor() { }

  ngOnInit(): void {
  }

  ngOnChanges(): void {
    if (this.model) {
      this.modelDateAsString = this.model.format('YYYY-MM-DD');
      this.modelTimeAsString = this.model.format('HH:mm');
    } else {
      this.modelDateAsString = '';
      this.modelTimeAsString = '';
    }
  }

  onNewDate(): void {
    this.modelChange.next(moment(this.modelDateAsString + 'T' + this.modelTimeAsString, 'YYYY-MM-DDTHH:mm'));
  }

}
