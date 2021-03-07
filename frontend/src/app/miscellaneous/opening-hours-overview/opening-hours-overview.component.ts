import { Component, OnInit } from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {Observable, Subject} from 'rxjs';
import {LocationService} from '../../services/api/locations/location.service';
import {catchError, tap} from 'rxjs/operators';
import {of} from 'rxjs/internal/observable/of';

@Component({
  selector: 'app-opening-hours-overview',
  templateUrl: './opening-hours-overview.component.html',
  styleUrls: ['./opening-hours-overview.component.css']
})
export class OpeningHoursOverviewComponent implements OnInit {

  year: number;
  weekNr: number;
  overviewObs: Observable<Map<string, string[]>>;

  errorSubject = new Subject<boolean>();

  constructor(private route: ActivatedRoute,
              private locationService: LocationService) { }

  ngOnInit(): void {
    this.year = Number(this.route.snapshot.paramMap.get('year'));
    this.weekNr = Number(this.route.snapshot.paramMap.get('weekNr'));
    this.overviewObs = this.locationService.getOpeningOverviewOfWeek(this.year, this.weekNr).pipe(
      tap(next => {
        console.log(next);
        for (const locationName of next.keys()) {
          console.log(locationName);
        }
      }),
      catchError(() => {
        this.errorSubject.next(true);
        return of(null);
      })
    );
  }

}
