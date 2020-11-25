import {MatSelectChange} from '@angular/material/select';
import {LocationTag} from './model/LocationTag';
export function objectExists(obj: any): boolean {
  return obj !== null && obj !== undefined;
}

export function matSelectionChanged(event: MatSelectChange, currentSelection: LocationTag[]): boolean {
  if (event.value.length !== currentSelection.length) {
    return true;
  } else {
    const selection: LocationTag[] = event.value;
    for (const tag of currentSelection) {
      if (selection.findIndex(v => v.tagId === tag.tagId) < 0) {
        return true;
      }
    }
    return false;
  }
}
