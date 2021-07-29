import { Component, OnInit } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { Location } from '../../shared/model/Location';
import { ScanningService } from '../../services/api/scan/scanning.service';
import { catchError } from 'rxjs/operators';
import { of } from 'rxjs/internal/observable/of';

@Component({
  selector: 'app-scanning-locations',
  templateUrl: './scanning-locations.component.html',
  styleUrls: ['./scanning-locations.component.scss'],
})
export class ScanningLocationsComponent implements OnInit {
  locationObs: Observable<Location[]>;
  loadingError = new Subject<boolean>();

  constructor(private scanningService: ScanningService) {}

  ngOnInit(): void {
    this.locationObs = this.scanningService.getLocationsToScan().pipe(
      catchError((err) => {
        console.error('Error while loading the locations you could scan.', err);
        this.loadingError.next(true);
        return of<Location[]>(null);
      })
    );
  }
}
