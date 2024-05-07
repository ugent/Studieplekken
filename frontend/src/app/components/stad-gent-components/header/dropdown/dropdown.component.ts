import {Component, Input, OnInit} from '@angular/core';
import {Subject} from 'rxjs';
import {User} from 'src/app/model/User';
import {AuthenticationService} from '../../../../services/authentication/authentication.service';

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
