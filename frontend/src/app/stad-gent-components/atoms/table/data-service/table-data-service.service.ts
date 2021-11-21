import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Observable } from 'rxjs';
import { Location } from 'src/app/shared/model/Location';
import { LocationReservation, LocationReservationState } from 'src/app/shared/model/LocationReservation';
import { User } from 'src/app/shared/model/User';
import { TabularData } from '../tabular-data';

@Injectable({
  providedIn: 'root',
})
export class TableDataService {
  constructor(private translationService: TranslateService) {}

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

  reservationsToScanningTable(reservations: LocationReservation[]): TabularData<LocationReservation> {
    return {
      columns: [
        {
          columnHeader: "management.locationDetails.calendar.reservations.table.user",
          type: 'contentColumn',
          columnContent: lr => `${lr.user.firstName} ${lr.user.lastName}`,
        }, {
          columnHeader: "management.locationDetails.calendar.reservations.table.attended",
          type: 'contentColumn',
          columnContent: _ => "",
          translateColumnContent: lr => this.getCorrectI18NObjectOfReservation(lr),
        },
        {
          columnHeader: "management.locationDetails.calendar.reservations.table.scan",
          type: 'actionColumn',
          width: 7,
          columnContent: lr => ({
            actionType: 'button',
            actionContent: "Yes",
            buttonClass: "primary",
            disabled: this.disableYesButton(lr)
          })
        },
        {
          columnHeader: "",
          type: 'actionColumn',
          width: 7,
          columnContent: lr => ({
            actionType: 'button',
            actionContent: "No",
            buttonClass: "secondary",
            disabled: this.disableNoButton(lr)
          })
        },
      ],
      data: reservations
    }
  }

  private getCorrectI18NObjectOfReservation(reservation: LocationReservation): string {
    switch (reservation.state) {
      case LocationReservationState.PRESENT: {
        return 'general.yes';
      }
      case LocationReservationState.ABSENT: {
        return 'general.no';
      }
      default: {
        if (!reservation.timeslot.isInPast()) {
          return "general.notAvailableAbbreviation";
        } else {
          return "management.locationDetails.calendar.reservations.table.notScanned";
        }
      }
    }
  }

  private disableYesButton(reservation: LocationReservation): boolean {
    return reservation.state === LocationReservationState.PRESENT;
  }

  private disableNoButton(reservation: LocationReservation): boolean {
    return reservation.state === LocationReservationState.ABSENT;
  }
}
