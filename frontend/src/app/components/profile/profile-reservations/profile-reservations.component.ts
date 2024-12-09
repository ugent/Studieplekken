import { Component, EventEmitter, Input, Output } from '@angular/core';
import { LocationReservationsService } from '@/services/api/location-reservations/location-reservations.service';
import { LocationReservation, LocationReservationState } from '@/model/LocationReservation';
import { User } from '@/model/User';
import { LocationService } from 'src/app/services/api/locations/location.service';
import { map } from 'rxjs/operators';
import { BaseManagementComponent } from '../../management//base-management.component';
import { DeleteAction, TableAction, TableMapper } from 'src/app/model/Table';
import { TranslateService } from '@ngx-translate/core';
import { Location } from '@/model/Location';

@Component({
    selector: 'app-profile-reservations',
    templateUrl: './profile-reservations.component.html',
    styleUrls: ['./profile-reservations.component.scss'],
})
export class ProfileReservationsComponent extends BaseManagementComponent<LocationReservation> {

    @Input() user: User;
    @Input() locations: Location[] = [];
    @Input() reservations: LocationReservation[] = [];
    @Input() isManagement = false;

    @Output() updatedReservations = new EventEmitter<void>();

    constructor(
        private reservationService: LocationReservationsService,
        private translateService: TranslateService
    ) {
        super();
    }

    public ngOnInit(): void {
        this.refresh$.subscribe(() =>
            this.updatedReservations.emit()
        );
    }

    private getLocation(locationId: number): Location {
        return this.locations.find(location => location.locationId === locationId);
    }

    public storeDelete(item: LocationReservation): void {
        this.sendBackendRequest(
            this.reservationService.deleteLocationReservation(item)
        );
    }

    public getTableMapper(): TableMapper<LocationReservation> {
        return (reservation: LocationReservation) => {
            let locationName = this.getLocation(reservation.timeslot.locationId)?.name;

            if (locationName === undefined || locationName === null) {
                locationName = '/';
            }

            return {
                'profile.reservations.locations.table.header.locationName': this.getLocation(reservation.timeslot.locationId).name,
                'profile.reservations.locations.table.header.reservationDate': reservation.timeslot.timeslotDate.format('YYYY/MM/DD'),
                'profile.reservations.locations.table.header.beginHour': reservation.timeslot.openingHour.format('HH:mm'),
                'profile.reservations.locations.table.header.state': this.translateService.stream(
                    'profile.reservations.locations.table.attended.' + reservation.state
                )
            }
        };
    }

    public getTableActions(): TableAction<LocationReservation>[] {
        return [
            new DeleteAction((reservation: LocationReservation) => {
                this.prepareDelete(reservation);
            }, (reservation: LocationReservation) =>
                reservation.state === LocationReservationState.APPROVED && !reservation.timeslot.isInPast()
            )
        ];
    }
}
