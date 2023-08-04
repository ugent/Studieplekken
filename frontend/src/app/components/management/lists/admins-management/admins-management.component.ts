import {Component, OnInit} from '@angular/core';
import {UserService} from '../../../../extensions/services/api/users/user.service';
import {TableComponent} from '../../../../contracts/table.component.interface';
import {ListAction, TableAction, TableMapper} from '../../../../model/Table';
import {Observable} from 'rxjs';
import {User} from '../../../../model/User';
import {Router} from '@angular/router';

@Component({
    selector: 'app-admins-management',
    templateUrl: './admins-management.component.html',
    styleUrls: ['./admins-management.component.scss'],
})
export class AdminsManagementComponent implements OnInit, TableComponent {

    protected adminsObs$: Observable<User[]>;

    constructor(
        private userService: UserService,
        private router: Router
    ) {
    }

    ngOnInit(): void {
        this.adminsObs$ = this.userService.getAdmins();
    }

    getTableActions(): TableAction[] {
        return [
            new ListAction((admin: User) => {
                void this.router.navigate(['/management/users/' + admin.userId]);
            })
        ];
    }

    getTableMapper(): TableMapper {
        return (admin: User) => ({
            'management.users.searchResult.table.firstName': admin.firstName,
            'management.users.searchResult.table.lastName': admin.lastName,
            'management.users.searchResult.table.institution': admin.institution
        });
    }
}
