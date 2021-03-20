import {Component, EventEmitter, Input, OnChanges, OnInit, Output} from '@angular/core';
import 'moment-duration-format';

@Component({
  selector: 'app-moment-timeslot-size',
  templateUrl: './moment-timeslot-size.component.html',
  styleUrls: ['./moment-timeslot-size.component.css'],
})
export class MomentTimeslotSizeComponent implements OnInit, OnChanges {
  /**
   * This is the value that will eventually be emitted
   */
  @Input()
  model: number;

  @Input()
  disabled: boolean;

  @Input()
  periodDuration: number;

  /**
   * This is the emitter that will emit the eventual size
   */
  @Output()
  modelChange: EventEmitter<number> = new EventEmitter();

  /**
   * This is the string that will be shown in the frontend
   */
  modelAsString: string;

  constructor() {
  }

  ngOnInit(): void {
  }

  /**
   * This is called whenever the value is changed by the application (e.g. loaded from server)
   */
  ngOnChanges(changes): void {
    const newValue = this.periodDuration
      ? Math.abs(Math.round(this.periodDuration / this.model))
      : 0;
    this.modelAsString = `${newValue}`;
  }

  /**
   * This is called whenever the user changes the value, outputValue should be emitted using timeslotSizeChange
   */
  onNewDate(): void {
    const amountOfSlots = Number(this.modelAsString);
    if (this.periodDuration) {
      const outputValue = Math.round(
        Math.abs(this.periodDuration / amountOfSlots)
      );
      this.modelChange.next(outputValue);
    }
  }
}
