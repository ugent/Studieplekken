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
    protected onCheckedChange: (checked: boolean) => void;
    protected onCheckedTouched: () => void;

    public writeValue(checked: boolean): void {
        this.isChecked = checked;
    }

    public registerOnChange(onChange: (checked:boolean) => void): void {
        this.onCheckedChange = onChange;
    }

    public registerOnTouched(onTouched: () => void): void {
        this.onCheckedTouched = onTouched;
    }

    public setDisabledState?(isDisabled: boolean): void {
        this.isDisabled = isDisabled;
    }
}
