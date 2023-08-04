import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {Observable, ReplaySubject, Subject} from 'rxjs';
import {map} from 'rxjs/operators';
import {booleanSorter} from 'src/app/extensions/util/Util';
import {ScanningService} from '../../../extensions/services/api/scan/scanning.service';
import {Location} from '../../../model/Location';
import {TableDataService} from '../../stad-gent-components/atoms/table/data-service/table-data-service.service';
import {TableComponent} from '../../../contracts/table.component.interface';
import {ListAction, TableAction, TableMapper} from '../../../model/Table';
import * as moment from 'moment/moment';

@Component({
    selector: 'app-scanning-locations',
    templateUrl: './scanning-locations.component.html',
    styleUrls: ['./scanning-locations.component.scss'],
})
export class ScanningLocationsComponent implements OnInit, TableComponent {

    protected locationObs$: Subject<Location[]>;

    constructor(
        private scanningService: ScanningService,
        private tableDataService: TableDataService,
        private router: Router
    ) {
        this.locationObs$ = new ReplaySubject();
    }

    ngOnInit(): void {
       this.scanningService.getLocationsToScan().pipe(
            map((locations) =>
                locations.filter(location =>
                    this.isScannable(location)
                )
            )
        ).subscribe((locations: Location[]) => {
            this.locationObs$.next(locations);
       });
    }

    isScannable(location: Location): boolean {
        return location.currentTimeslot && location.currentTimeslot.reservable && moment().isAfter(
            location.currentTimeslot.getStartMoment().subtract(30, 'minutes')
        );
    }

    getTableActions(): TableAction[] {
        return [
            new ListAction((location: Location) =>
                void this.router.navigate(['/scan/locations/' + location.locationId])
            )
        ];
    }

    getTableMapper(): TableMapper {
        return (location: Location) => ({
            'scan.locations.header.name': location.name,
            'scan.locations.header.building': location.building.name,
            'scan.locations.header.numberOfSeats': location.numberOfSeats
        });
    }
}
