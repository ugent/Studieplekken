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
    selector: 'app-moment-datetime',
    templateUrl: './moment-datetime.component.html',
    styleUrls: ['./moment-datetime.component.scss'],
})
export class MomentDateTimeComponent implements OnChanges {
    @Input()
    model: Moment;
    @Input()
    disabled: boolean;
    @Output()
    modelChange: EventEmitter<Moment> = new EventEmitter<Moment>();
    @Input()
    min: Moment;
    @Input()
    max: Moment;

    modelDateAsString: string;
    modelTimeAsString: string;
    modelMinAsString: string;
    modelMaxAsString: string;

    ngOnChanges(): void {
        if (this.model) {
            this.modelDateAsString = this.model.format('YYYY-MM-DD');
            this.modelTimeAsString = this.model.format('HH:mm');
        } else {
            this.modelDateAsString = '';
            this.modelTimeAsString = '';
        }

        if (this.min) {
            this.modelMinAsString = this.min.format('YYYY-MM-DD');
        } else {
            this.modelMinAsString = '';
        }

        if (this.max) {
            this.modelMaxAsString = this.max.format('YYYY-MM-DD');
        } else {
            this.modelMaxAsString = '';
        }
    }

    onNewDate(): void {
        this.modelChange.next(
            moment(
                this.modelDateAsString + 'T' + this.modelTimeAsString,
                'YYYY-MM-DDTHH:mm'
            )
        );
    }
}
