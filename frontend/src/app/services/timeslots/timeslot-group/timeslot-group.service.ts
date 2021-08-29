import { Injectable } from '@angular/core';
import * as moment from 'moment';
import { Moment } from 'moment';
import { DefaultMap } from 'src/app/shared/default-map/defaultMap';
import { Timeslot } from 'src/app/shared/model/Timeslot';


export type TimeslotGroups = Map<number, Timeslot[]>;
export type Suggestion = {model: Timeslot, copy: Timeslot};

@Injectable({
  providedIn: 'root'
})
export class TimeslotGroupService {

  constructor() { }

  copyByWeekOffset(timeslot: Timeslot, weekOffset: number): Timeslot {
    const date = moment(timeslot.timeslotDate).add(weekOffset+1, "weeks");
    return this.copy(timeslot, date)
  }

  copy(timeslot: Timeslot, date: Moment) {
    const reservationDiff = date.diff(timeslot.timeslotDate, "minutes");

    return new Timeslot(null, date, null, null, timeslot.reservable, timeslot.reservableFrom?.subtract(reservationDiff), timeslot.locationId, timeslot.openingHour, timeslot.closingHour, timeslot.timeslotGroup, timeslot.repeatable)

  }

  groupTimeslots(timeslot: Timeslot[]): TimeslotGroups {
    const perGroup = new DefaultMap<number, Timeslot>();
    timeslot.forEach(t => perGroup.addValueAsList(t.timeslotGroup, t))


    return perGroup;
  }

  getOldestTimeslotPerGroup(timeslots: Timeslot[]) {
    const groups = this.groupTimeslots(timeslots);
    return this.getOldestInTimeslotGroup(groups);
  }

  private getOldestInTimeslotGroup(groups: TimeslotGroups) {
    const mapper: (a: [number, Timeslot[]]) => [number, Timeslot] 
                  = ([group, value]) => [group, value.reduce((a, b) => a.timeslotDate.isAfter(b.timeslotDate) ? a:b)]

    const list = Array.from(groups).map(mapper)

    return new Map(list);
  }

  getSuggestions(timeslots: Timeslot[], amountOfWeeks=3) {
    const latestTimeslots = this.getOldestTimeslotPerGroup(timeslots);
    const repeatableTimeslots = [...latestTimeslots.values()].filter(t => t.repeatable);
    
    const suggestions: Suggestion[] = [];

    for(let i = 1; i <= amountOfWeeks; i++) {
      const targetDate = (t: Timeslot) => moment(t.timeslotDate).add(i, "weeks");
      repeatableTimeslots.filter(t => t.timeslotDate.isBefore(targetDate(t), "day")).forEach(t => suggestions.push({model: t, copy: this.copy(t, targetDate(t))}));
    }

    return suggestions;
  }

  filterTimeslotsByMoment(timeslots: Timeslot[], day: Moment, granularity: moment.unitOfTime.StartOf) {
    return timeslots.filter(t => t.timeslotDate.isSame(day, granularity))
  }

  filterSuggestionsByMoment(suggestions: Suggestion[], day: Moment, granularity: moment.unitOfTime.StartOf) {
    return suggestions.filter(t => t.copy.timeslotDate.isSame(day, granularity))
  }
}
