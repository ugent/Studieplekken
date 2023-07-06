import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  Output,
} from '@angular/core';
import * as moment from 'moment';
import { Moment } from 'moment';

@Component({
  selector: 'app-moment-date',
  templateUrl: './moment-date.component.html',
  styleUrls: ['./moment-date.component.scss'],
})
export class MomentDateComponent implements OnChanges {
  @Input()
  model: Moment;
  @Input()
  type: 'date' | 'time';
  @Output()
  modelChange: EventEmitter<Moment> = new EventEmitter<Moment>();
  @Input()
  min: Moment;
  @Input()
  max: Moment;

  modelAsString: string;
  minAsString: string;
  maxAsString: string;

  ngOnChanges(): void {
    if (this.model) {
      this.modelAsString =
        this.type === 'date'
          ? this.model.format('YYYY-MM-DD')
          : this.model.format('HH:mm');
    } else {
      this.modelAsString = '';
    }

    if (this.min) {
      this.minAsString =
        this.type === 'date'
          ? this.min.format('YYYY-MM-DD')
          : this.min.format('HH:mm:ss');
    } else {
      this.minAsString = '';
    }

    if (this.max) {
      this.maxAsString =
        this.type === 'date'
          ? this.max.format('YYYY-MM-DD')
          : this.max.format('HH:mm:ss');
    } else {
      this.maxAsString = '';
    }
  }

  onNewDate(): void {
    this.modelChange.next(
      moment(this.modelAsString, this.type === 'date' ? 'YYYY-MM-DD' : 'HH:mm')
    );
  }
}
