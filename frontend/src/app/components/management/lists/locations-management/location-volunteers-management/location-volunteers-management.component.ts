import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {TableComponent} from '../../../../../contracts/table.component.interface';
import {User} from '../../../../../model/User';
import {Location} from '../../../../../model/Location';
import {Observable} from 'rxjs';
import {DeleteAction, TableAction, TableMapper} from '../../../../../model/Table';
import {LocationService} from '../../../../../extensions/services/api/locations/location.service';
import {first, map, mergeMap, switchMap} from 'rxjs/operators';
import {of} from 'rxjs/internal/observable/of';

@Component({
    selector: 'app-location-volunteers-management',
    templateUrl: './location-volunteers-management.component.html',
    styleUrls: ['./location-volunteers-management.component.scss']
})
export class LocationVolunteersManagementComponent implements OnInit, TableComponent {

    @Input() location: Observable<Location>;
    @Input() volunteers: Observable<User[]>;

    @Output() updatedVolunteers: EventEmitter<Location>;

    constructor(
        private locationService: LocationService
    ) {
        this.updatedVolunteers = new EventEmitter();
    }

    ngOnInit(): void {
    }

    storeAdd(user: User): void {
        this.location.pipe(
            switchMap(location =>
                this.locationService.addVolunteer(location.locationId, user.userId).pipe(
                    map(() => location)
                )
            ), first()
        ).subscribe((location: Location) =>
            this.updatedVolunteers.emit(location)
        );
    }

    storeDelete(user: User): void {
        this.location.pipe(
            switchMap((location: Location) =>
                this.locationService.deleteVolunteer(location.locationId, user.userId).pipe(
                    map(() => location)
                )
            ), first()
        ).subscribe((location) =>
            this.updatedVolunteers.emit(location)
        );
    }

    getTableMapper(): TableMapper {
        return (user: User) => ({
            'management.users.searchResult.table.firstName': user.firstName,
            'management.users.searchResult.table.lastName': user.lastName,
            'management.users.searchResult.table.institution': user.institution
        });
    }

    getTableActions(): TableAction[] {
        return [
            new DeleteAction((user: User) => {
                this.storeDelete(user);
            })
        ];
    }
}
