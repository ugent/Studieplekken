import {MatSelectChange} from '@angular/material/select';
import {LocationTag} from './model/LocationTag';
import {Timeslot} from './model/Timeslot';
import {CalendarPeriod} from './model/CalendarPeriod';
import * as moment from 'moment';

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

export function isTimeslotInPast(timeslot: Timeslot, calendarPeriod: CalendarPeriod): boolean {
  const timeslotDate = timeslot.timeslotDate.format('YYYY-MM-DD');
  const timeslotClose = calendarPeriod.openingTime.clone()
    .add(calendarPeriod.reservableTimeslotSize * (timeslot.timeslotSeqnr + 1), 'minutes')
    .format('HH:mm:ss');

  const timeslotEndingTimestamp = moment(timeslotDate + ' ' + timeslotClose, 'YYYY-MM-DD HH:mm:ss');
  return timeslotEndingTimestamp.isBefore(moment());
}
