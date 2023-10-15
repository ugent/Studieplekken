import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {TableDataService} from 'src/app/components/stad-gent-components/atoms/table/data-service/table-data-service.service';
import {User} from '../../../model/User';
import {TabularData} from '../../stad-gent-components/atoms/table/tabular-data';

@Component({
    selector: 'app-search-user',
    templateUrl: './search-user-component.component.html',
    styleUrls: ['./search-user-component.component.scss']
})
export class SearchUserComponentComponent implements OnInit {

    @Input() icon = 'icon-hamburger';
    @Output() selectedUser = new EventEmitter<User>();
    users: User[];

    constructor(private table: TableDataService) {}

    ngOnInit(): void {}

    newUsers(users: User[]): void {
        this.users = users;
    }

    outputUser(user: User): void {
        this.selectedUser.next(user);
    }

    getTableData(users: User[]): TabularData<User>  {
        return this.table.usersToTable(users, this.icon);
    }
}
