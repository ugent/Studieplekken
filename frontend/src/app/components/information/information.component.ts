import {Component, OnInit} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {UserService} from '../../extensions/services/api/users/user.service';
import {AuthenticationService} from '../../extensions/services/authentication/authentication.service';
import {BreadcrumbService} from '../../stad-gent-components/header/breadcrumbs/breadcrumb.service';
import {User} from '../../extensions/model/User';
import {filter} from 'rxjs/operators';

@Component({
    selector: 'app-information',
    templateUrl: './information.component.html',
    styleUrls: ['./information.component.scss'],
})
export class InformationComponent implements OnInit {
    public showManagement = false;
    public showAdmin = false;
    public showSupervisors = false;

    constructor(
        public translate: TranslateService,
        private authenticationService: AuthenticationService,
        private userService: UserService,
        private breadcrumbService: BreadcrumbService
    ) {
    }

    /**
     * Determine what manuals are shown to the user,
     * depending on their role.
     */
    ngOnInit(): void {
        // subscribe to the user observable to make sure that the correct information
        // is shown in the application.
        this.authenticationService.user.pipe(
            filter(user => !!user)
        ).subscribe((user: User) => {
            this.showManagement = user.isAuthority();
            this.showAdmin = user.isAdmin();
            this.showSupervisors = user.isScanner();
        });

        this.breadcrumbService.setCurrentBreadcrumbs([{
            pageName: 'Information', url: '/information'
        }]);
    }
}
