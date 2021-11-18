import { Injectable } from '@angular/core';
import { Location } from 'src/app/shared/model/Location';
import { User } from 'src/app/shared/model/User';
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
          columnHeader: 'scan.locations.header.name',
          type: 'contentColumn',
          columnContent: (l) => l.name,
          width: 50,
        },
        {
          columnHeader: 'scan.locations.header.name',
          type: 'contentColumn',
          columnContent: (l) => l.building.name,
          width: 30,
        },
        {
          columnHeader: 'scan.locations.header.name',
          type: 'contentColumn',
          columnContent: (l) => `${l.numberOfSeats}`,
          width: 10,
        },
        {
          columnHeader: 'scan.locations.header.name',
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

  usersToTable(users: User[], icon = "icon-hamburger"): TabularData<User> {
    return {
      columns: [
        {
          columnHeader: "management.users.searchResult.table.id",
          type: 'contentColumn',
          columnContent: user => user.userId,
        }, {
          columnHeader: "management.users.searchResult.table.firstName",
          type: 'contentColumn',
          columnContent: user => user.firstName

        }, {
          columnHeader: "management.users.searchResult.table.lastName",
          type: 'contentColumn',
          columnContent: user => user.lastName

        }, {
          columnHeader: "management.users.searchResult.table.institution",
          type: 'contentColumn',
          columnContent: user => user.institution
        },
        {
          columnHeader: "",
          type: 'actionColumn',
          width: 7,
          columnContent: user => ({
            actionType: 'icon',
            actionContent: icon.replace("icon-", ""),
            fallbackContent: 'Details',
          })
        },
      ],
      data: users
    }
  }
}
