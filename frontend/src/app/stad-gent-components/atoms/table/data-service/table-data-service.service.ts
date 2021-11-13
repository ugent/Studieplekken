import { Injectable } from '@angular/core';
import { Location } from 'src/app/shared/model/Location';
import { TabularData } from '../tabular-data';

@Injectable({
  providedIn: 'root',
})
export class TableDataService {
  constructor() {}

  isLocationScannable(location: Location) {
    return (
      location.currentTimeslot &&
      location.currentTimeslot.reservable &&
      location.currentTimeslot.isCurrent()
    );
  }

  locationsToScannable(locations: Location[]): TabularData<Location> {
    return {
      columns: [
        {
          columnHeader: 'Name',
          type: 'contentColumn',
          columnContent: (l) => l.name,
          width: 50,
        },
        {
          columnHeader: 'Building',
          type: 'contentColumn',
          columnContent: (l) => l.building.name,
          width: 30,
        },
        {
          columnHeader: 'Seats',
          type: 'contentColumn',
          columnContent: (l) => `${l.numberOfSeats}`,
          width: 10,
        },
        {
          columnHeader: 'Scan',
          type: 'actionColumn',
          width: 10,
          columnContent: (l) =>
            this.isLocationScannable(l)
              ? {
                  actionType: 'icon',
                  actionContent: 'hamburger',
                  fallbackContent: 'Scan',
                }
              : {
                  actionType: 'string',
                  actionContent: 'Closed',
                  disabled: true,
                },
        },
      ],
      data: locations,
    };
  }
}
