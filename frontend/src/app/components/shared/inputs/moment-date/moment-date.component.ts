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

    protected dateOrTimeValue: Moment = moment();
    protected isDisabled: boolean = false;
    protected onDateOrTimeChange: (date: Moment) => void;
    protected onDateOrTimeTouched: () => void;

    /**
     * @inheritdoc
     */
    public writeValue(dateOrTime: Moment): void {
        this.dateOrTimeValue = dateOrTime;
    }

    /**
     * @inheritdoc
     */
    public registerOnChange(onChange: (dateOrTime: Moment) => void): void {
        this.onDateOrTimeChange = onChange;
    }

    /**
     * @inheritdoc
     */
    public registerOnTouched(onTouched: () => void): void {
        this.onDateOrTimeTouched = onTouched;
    }

    /**
     * @inheritdoc
     */
    public setDisabledState(isDisabled: boolean): void {
        this.isDisabled = isDisabled;
    }

    /**
     * Handles the input change event for date or time input fields.
     *
     * @param event - The input change event.
     * @param part - Specifies whether the input is for 'date' or 'time'.
     * 
     * This method parses the input value based on the specified part ('date' or 'time')
     * and updates the `dateOrTimeValue` property accordingly. If a callback function
     * `onDateOrTimeChange` is provided, it will be called with the updated value.
     */
    public handleInputChange(event: Event, part: 'date' | 'time'): void {
        const input = event.target as HTMLInputElement;
        const value = input.value;

        if (!this.dateOrTimeValue) {
            this.dateOrTimeValue = moment();
        }

        if (part === 'date') {
            const newDate = moment(value, 'YYYY-MM-DD');

            if (newDate.isValid()) {
                this.dateOrTimeValue.date(newDate.date());
            }
        } else if (part === 'time') {
            const newTime = moment(value, 'HH:mm');

            if (newTime.isValid()) {
                this.dateOrTimeValue
                    .hour(newTime.hour())
                    .minute(newTime.minute());
            }
        }

        if (this.onDateOrTimeChange) {
            this.onDateOrTimeChange(this.dateOrTimeValue);
        }
    }

    /**
     * Formats a given Moment object into a string based on the provided format.
     *
     * @param moment - The Moment object to format. If null or undefined, the function will not return a formatted string.
     * @param format - The string format to apply to the Moment object.
     * @returns The formatted date string if the Moment object is valid; otherwise, undefined.
     */
    public getFormattedValue(moment: Moment, format: string): string {
        if (moment !== null && moment !== undefined) {
            return moment.format(format);
        }
    }
}
