import {Component, Input, OnInit} from '@angular/core';
import {HasTableComponent} from '../../../../../contracts/has-table.component.interface';
import {User} from '../../../../../model/User';
import {Observable} from 'rxjs';
import {DeleteAction, TableAction, TableMapper} from '../../../../../model/Table';

@Component({
    selector: 'app-location-volunteers-management',
    templateUrl: './location-volunteers-management.component.html',
    styleUrls: ['./location-volunteers-management.component.scss']
})
export class LocationVolunteersManagementComponent implements OnInit, HasTableComponent {

    @Input() volunteers: Observable<User[]>;

    constructor() {
    }

    ngOnInit(): void {
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
                alert('deleting');
            })
        ];
    }
}
