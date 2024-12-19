import { Component, EventEmitter, Input, Output } from '@angular/core';
import { LocationReservationsService } from '@/services/api/location-reservations/location-reservations.service';
import { LocationReservation, LocationReservationState } from '@/model/LocationReservation';
import { User } from '@/model/User';
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

    @Input()
    protected user: User;

    @Input() 
    protected locations: Location[] = [];

    @Input() 
    protected reservations: LocationReservation[] = [];

    @Input()
    protected isManagement = false;

    @Output() 
    protected updatedReservations = new EventEmitter<void>();

    constructor(
        private reservationService: LocationReservationsService,
        private translateService: TranslateService
    ) {
        super();
    }

    public ngOnInit(): void {
        this.reservations.forEach(reservation => {
            reservation.location = this.locations.find(location =>
                location.locationId === reservation.timeslot.locationId
            );
        });

        this.refresh$.subscribe(() =>
            this.updatedReservations.emit()
        );
    }

    public storeDelete(item: LocationReservation): void {
        this.sendBackendRequest(
            this.reservationService.deleteLocationReservation(item)
        );
    }

    public getTableMapper(): TableMapper<LocationReservation> {
        return (reservation: LocationReservation) => {
            return {
                'profile.reservations.locations.table.header.locationName': reservation.location.name,
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
