import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {TableComponent} from '../../../../../contracts/table.component.interface';
import {User} from '../../../../../model/User';
import {Location} from '../../../../../model/Location';
import {Observable} from 'rxjs';
import {DeleteAction, TableAction, TableMapper} from '../../../../../model/Table';
import {LocationService} from '../../../../../services/api/locations/location.service';
import {first, map, mergeMap, switchMap} from 'rxjs/operators';
import {of} from 'rxjs/internal/observable/of';

@Component({
    selector: 'app-location-volunteers-management',
    templateUrl: './location-volunteers-management.component.html',
    styleUrls: ['./location-volunteers-management.component.scss']
})
export class LocationVolunteersManagementComponent implements OnInit, TableComponent<User> {

    @Input() location: Location;
    @Input() volunteers: User[];

    @Output() updatedVolunteers: EventEmitter<Location>;

    constructor(
        private locationService: LocationService
    ) {
        this.updatedVolunteers = new EventEmitter();
    }

    ngOnInit(): void {
    }

    storeAdd(user: User): void {
        this.locationService.addVolunteer(this.location.locationId, user.userId).subscribe(() =>
            this.updatedVolunteers.emit(this.location)
        );
    }

    storeDelete(user: User): void {
        this.locationService.deleteVolunteer(this.location.locationId, user.userId).subscribe(() =>
            this.updatedVolunteers.emit(this.location)
        );
    }

    getTableMapper(): TableMapper<User> {
        return (user: User) => ({
            'management.users.searchResult.table.firstName': user.firstName,
            'management.users.searchResult.table.lastName': user.lastName,
            'management.users.searchResult.table.institution': user.institution
        });
    }

    getTableActions(): TableAction<User>[] {
        return [
            new DeleteAction((user: User) => {
                this.storeDelete(user);
            })
        ];
    }
}
