import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, Subject } from 'rxjs';
import { of } from 'rxjs/internal/observable/of';
import { catchError, map } from 'rxjs/operators';
import { booleanSorter } from 'src/app/extensions/util/Util';
import { ScanningService } from '../../../extensions/services/api/scan/scanning.service';
import { Location } from '../../../extensions/model/Location';
import { TableDataService } from '../../stad-gent-components/atoms/table/data-service/table-data-service.service';
import * as moment from "moment";

@Component({
  selector: 'app-scanning-locations',
  templateUrl: './scanning-locations.component.html',
  styleUrls: ['./scanning-locations.component.scss'],
})
export class ScanningLocationsComponent implements OnInit {
  locationObs: Observable<Location[]>;
  loadingError = new Subject<boolean>();

  constructor(
    private scanningService: ScanningService,
    private tableDataService: TableDataService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.locationObs = this.scanningService.getLocationsToScan().pipe(
      catchError((err) => {
        console.error('Error while loading the locations you could scan.', err);
        this.loadingError.next(true);
        return of<Location[]>(null);
      }),
      map((l) =>
        l.sort(
          booleanSorter((l) => this.tableDataService.isLocationScannable(l))
        )
      )
    );
  }
  getTableData(locations: Location[]) {
    return this.tableDataService.locationsToScannable(locations);
  }

  onAction({ data, columnIndex }: { data: Location; columnIndex: number }) {
    this.router.navigate([`/scan/locations/${data.locationId}`]);
  }

  isScannable(location: Location) {
    return (location.currentTimeslot
      && location.currentTimeslot.reservable
      && location.currentTimeslot.getStartMoment().isBefore(moment().add(30, "minutes")))
  }
}
