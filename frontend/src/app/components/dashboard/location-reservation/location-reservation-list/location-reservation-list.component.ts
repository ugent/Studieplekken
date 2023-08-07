import {Component, Input, OnInit} from '@angular/core';
import {LocationReservation} from '../../../../model/LocationReservation';
import {User} from '../../../../model/User';
import {TableComponent} from '../../../../contracts/table.component.interface';
import {TableAction, TableMapper} from '../../../../model/Table';
import {TranslateService} from '@ngx-translate/core';

@Component({
    selector: 'app-location-reservation-list',
    templateUrl: './location-reservation-list.component.html',
    styleUrls: ['./location-reservation-list.component.scss']
})
export class LocationReservationListComponent implements OnInit, TableComponent<LocationReservation> {

    @Input() user: User;
    @Input() reservations: LocationReservation[];

    constructor(
        private translateService: TranslateService
    ) {
    }


    ngOnInit(): void {
    }

    getTableActions(): TableAction<LocationReservation>[] {
        return [];
    }

    getTableMapper(): TableMapper<LocationReservation> {
        return (reservation: LocationReservation) => ({
            'profile.reservations.locations.table.header.reservationDate': reservation.timeslot.timeslotDate.format('DD/MM/YYYY'),
            'profile.reservations.locations.table.header.beginHour':  reservation.timeslot.openingHour.format('HH:mm'),
            'profile.reservations.locations.table.header.state':  this.translateService.stream(
                'profile.reservations.locations.table.attended.' + reservation.state
            )
        });
    }
}
