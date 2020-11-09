import { Component, EventEmitter, Input, OnChanges, OnInit, Output } from '@angular/core';
import * as moment from 'moment';
import { Moment } from 'moment';

@Component({
  selector: 'app-moment-date',
  templateUrl: './moment-date.component.html',
  styleUrls: ['./moment-date.component.css']
})
export class MomentDateComponent implements OnInit, OnChanges {

  @Input()
  model: Moment;
  @Input()
  type: 'date'|'time';
  @Output()
  modelChange: EventEmitter<Moment> = new EventEmitter();


  modelAsString: string;

  constructor() { }

  ngOnInit(): void {
  }

  ngOnChanges(): void {
    if (this.model) {
      this.modelAsString = this.type === 'date' ? this.model.format('YYYY-MM-DD') : this.model.format('HH:mm');
    }
  }

  onNewDate(): void {
    this.modelChange.next(moment(this.modelAsString, this.type === 'date' ? 'YYYY-MM-DD' : 'HH:mm'));
  }

}
