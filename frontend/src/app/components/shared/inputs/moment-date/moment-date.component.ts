import { Component, forwardRef, Input } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { Moment } from 'moment';
import * as moment from 'moment';

@Component({
    selector: 'app-moment-date',
    templateUrl: './moment-date.component.html',
    styleUrls: ['./moment-date.component.scss'],
    providers: [{
        provide: NG_VALUE_ACCESSOR,
        useExisting: forwardRef(() => MomentDateComponent),
        multi: true
    }]
})
export class MomentDateComponent implements ControlValueAccessor {
    @Input() type: 'date' | 'time' | 'datetime';
    @Input() id: string;
    @Input() min: Moment;
    @Input() max: Moment;

    protected dateOrTimeValue: Moment;
    protected isDisabled: boolean = false;
    protected onDateOrTimeChange: (date: Moment) => void;
    protected onDateOrTimeTouched: () => void;

    public writeValue(dateOrTime: Moment): void {
        this.dateOrTimeValue = dateOrTime;
    }

    public registerOnChange(onChange: (dateOrTime: Moment) => void): void {
        this.onDateOrTimeChange = onChange;
    }

    public registerOnTouched(onTouched: () => void): void {
        this.onDateOrTimeTouched = onTouched;
    }

    public setDisabledState(isDisabled: boolean): void {
        this.isDisabled = isDisabled;
    }

    public handleInputChange(event: InputEvent, part: 'date' | 'time'): void {
        const input = event.target as HTMLInputElement;
        const value = input.value;

        if (part === 'date') {
            this.dateOrTimeValue = moment(value, 'YYYY-MM-DD');
        } else if (part === 'time') {
            this.dateOrTimeValue = moment(value, 'HH:mm');
        }

        if (this.onDateOrTimeChange) {
            this.onDateOrTimeChange(this.dateOrTimeValue);
        }
    }

    public getFormattedValue(moment: Moment, format: string): string {
        if (moment !== null && moment !== undefined) {
            return moment.format(format);
        }
    }
}
