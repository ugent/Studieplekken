import {Component, Input, OnInit} from '@angular/core';
import {isThisISOWeek} from 'date-fns';
import {Observable, Subject} from 'rxjs';
import {AuthenticationService} from 'src/app/extensions/services/authentication/authentication.service';
import {User} from 'src/app/extensions/model/User';
import {UserService} from 'src/app/extensions/services/api/users/user.service';
import {TranslateService} from '@ngx-translate/core';

@Component({
    selector: 'app-header-dropdown',
    templateUrl: './dropdown.component.html',
    styleUrls: ['./dropdown.component.scss']
})
export class DropdownComponent implements OnInit {
    @Input() user: User;
    @Input() isProfile: boolean;
    @Input() accordion: Subject<boolean>;

    protected showAdmin = false;
    protected showManagement = false;
    protected showLoggedIn = false;
    protected showVolunteer = false;

    constructor(
        private authenticationService: AuthenticationService
    ) {
    }

    ngOnInit(): void {
        this.showLoggedIn = this.user.isLoggedIn();
        this.showAdmin = this.user.isAdmin();
        this.showManagement = this.user.isAuthority();
        this.showVolunteer = this.user.isScanner();
    }

    logout(): void {
        return this.authenticationService.logout();
    }

    close(): void {
        this.accordion.next(false);
    }
}
