import { Component, EventEmitter, forwardRef, Input, Output } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

@Component({
    selector: 'app-checkbox',
    templateUrl: './checkbox.component.html',
    styleUrls: ['./checkbox.component.scss'],
    providers: [{
        provide: NG_VALUE_ACCESSOR,
        useExisting: forwardRef(() => CheckboxComponent),
        multi: true
    }]
})
export class CheckboxComponent implements ControlValueAccessor {
    @Input() variant: 'default' | 'error' | 'success' = 'default';
    @Input() id: string = 'checkbox-id';
    @Input() label: string;
    @Input() description: string | null = null;
    @Input() message: string | null = null;

    protected isDisabled: boolean = false;
    protected isChecked: boolean = false;
    protected onCheckedChange: (target: boolean) => void;
    protected onCheckedTouched: () => void;

    /**
     * Writes a value to the checkbox component.
     *
     * @param checked - The boolean value to set the checkbox state.
     */
    public writeValue(checked: boolean): void {
        this.isChecked = checked;
    }

    /**
     * Registers a callback function to be called when the checkbox state changes.
     *
     * @param onChange - A callback function that takes a boolean parameter indicating the new checked state of the checkbox.
     */
    public registerOnChange(onChange: (checked:boolean) => void): void {
        this.onCheckedChange = onChange;
    }

    /**
     * Registers a callback function that will be called when the checkbox is touched.
     * 
     * @param onTouched - The callback function to register.
     */
    public registerOnTouched(onTouched: () => void): void {
        this.onCheckedTouched = onTouched;
    }

    /**
     * Sets the disabled state of the checkbox.
     * 
     * @param isDisabled - A boolean indicating whether the checkbox should be disabled.
     */
    public setDisabledState?(isDisabled: boolean): void {
        this.isDisabled = isDisabled;
    }

    /**
     * Handles the change event for the input element.
     * 
     * @param event - The event object for the input change.
     * 
     * This method casts the event target to an HTMLInputElement and checks if the `onCheckedChange` callback is defined.
     * If it is, it calls the `onCheckedChange` callback with the boolean value of the input's value.
     */
    public onInputChange(event: Event): void {
        const input = event.target as HTMLInputElement;

        if (this.onCheckedChange) {
            this.onCheckedChange(!!input.value);
        }
    }
}
