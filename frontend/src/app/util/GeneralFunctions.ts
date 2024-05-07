import {MatSelectChange} from '@angular/material/select';
import {LocationTag} from '../model/LocationTag';

export function objectExists(obj: unknown): boolean {
    return obj !== null && obj !== undefined;
}

export function matSelectionChanged(
    event: MatSelectChange,
    currentSelection: LocationTag[]
): boolean {
    const selection = event.value as LocationTag[];

    if (selection.length !== currentSelection.length) {
        return true;
    } else {
        for (const tag of currentSelection) {
            if (selection.findIndex((v) => v.tagId === tag.tagId) < 0) {
                return true;
            }
        }
        return false;
    }
}
