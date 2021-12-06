import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Observable, Subject } from 'rxjs';
import { LocationService } from '../../services/api/locations/location.service';
import { catchError, tap } from 'rxjs/operators';
import { of } from 'rxjs/internal/observable/of';
import * as moment from 'moment';
import { Location } from '@angular/common';

@Component({
  selector: 'app-opening-hours-overview',
  templateUrl: './opening-hours-overview.component.html',
  styleUrls: ['./opening-hours-overview.component.scss']
})
export class OpeningHoursOverviewComponent implements OnInit {
  Object = Object;

  year: number;
  weekNr: number;
  overviewObs: Observable<Record<string, string[]>>;

  errorSubject = new Subject<boolean>();

  constructor(
    private route: ActivatedRoute,
    private locationService: LocationService,
    private location: Location
  ) {}

  ngOnInit(): void {
    this.year = Number(this.route.snapshot.paramMap.get('year'));
    this.weekNr = Number(this.route.snapshot.paramMap.get('weekNr'));
    this.overviewObs = this.locationService
      .getOpeningOverviewOfWeek(this.year, this.weekNr)
      .pipe(
        catchError((err) => {
          console.error(err);
          this.errorSubject.next(true);
          return of<Record<string, string[]>>(null);
        })
      );
  }

  stringOrPlaceholder(str: string): string {
    return str === null || str === undefined || str.length === 0 ? '-' : str;
  }

  mondayDate(year: number, week: number): string {
    const mondayDate = moment()
      .set('year', year)
      .isoWeek(week)
      .startOf('isoWeek');
    return mondayDate.format('DD/MM/YYYY');
  }

  sundayDate(year: number, week: number): string {
    const sundayDate = moment()
      .set('year', year)
      .isoWeek(week)
      .endOf('isoWeek');
    return sundayDate.format('DD/MM/YYYY');
  }

  previousWeek(): void {
    this.overviewObs = null;
    const calc = moment()
      .isoWeekYear(this.year)
      .isoWeek(this.weekNr)
      .subtract(1, 'week');
    const year = calc.isoWeekYear();
    const week = calc.isoWeek();
    this.retrieveOverview(year, week);
  }

  nextWeek(): void {
    this.overviewObs = null;
    const calc = moment().isoWeekYear(this.year).isoWeek(this.weekNr).add(1, 'week');
    const year = calc.isoWeekYear();
    const week = calc.isoWeek();
    this.retrieveOverview(year, week);
  }

  retrieveOverview(year: number, week: number): void {
    this.overviewObs = this.locationService
      .getOpeningOverviewOfWeek(year, week)
      .pipe(
        tap(() => {
          this.year = year;
          this.weekNr = week;
          this.location.replaceState('/opening/overview/' + year + '/' + week);
        })
      );
  }

  isInPast(year: number, week: number): boolean {
    return moment().set('year', year).isoWeek(week).isBefore(moment());
  }
}
