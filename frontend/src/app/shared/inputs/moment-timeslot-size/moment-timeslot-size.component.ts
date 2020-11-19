import { Component, EventEmitter, Input, OnChanges, OnInit, Output } from '@angular/core';
import * as moment from 'moment';
import 'moment-duration-format';

@Component({
  selector: 'app-moment-timeslot-size',
  templateUrl: './moment-timeslot-size.component.html',
  styleUrls: ['./moment-timeslot-size.component.css']
})
export class MomentTimeslotSizeComponent implements OnInit, OnChanges {

  /**
   * This is the value that will eventually be emitted
   */
  @Input()
  model: number;

  @Input()
  type: 'date'|'time';

  /**
   * This is the emitter that will emit the eventual size
   */
  @Output()
  modelChange: EventEmitter<number> = new EventEmitter();

  /**
   * This is the string that will be shown in the frontend
   */
  modelAsString: string;

  constructor() { }

  ngOnInit(): void {
  }

  /**
   * This is called whenever the value is changed by the application (e.g. loaded from server)
   */
  ngOnChanges(): void {
    if (this.model !== 0) {
      this.modelAsString = moment.duration(this.model, 'minutes').format('HH:mm');
    } else {
      this.modelAsString = '';
    }
  }

  /**
   * This is called whenever the user changes the value, outputValue should be emitted using timeslotSizeChange
   */
  onNewDate(): void {
    const asTime = moment(this.modelAsString, 'HH:mm');
    const outputValue = asTime.hours() * 60 + asTime.minutes();
    this.modelChange.next(outputValue);
  }

}
