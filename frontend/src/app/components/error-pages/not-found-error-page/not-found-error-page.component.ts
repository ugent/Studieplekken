import { User } from '@/model/User';
import { AuthenticationService } from '@/services/authentication/authentication.service';
import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';

@Component({
    selector: 'app-not-found-error-page',
    templateUrl: './not-found-error-page.component.html',
    styleUrls: ['./not-found-error-page.component.scss']
})
export class NotFoundErrorPageComponent implements OnInit {

    public $user: Observable<User>;

    constructor(
        private authenticationService: AuthenticationService
    ) {
    }

    public ngOnInit(): void {
        this.$user = this.authenticationService.getUserObs();
    }

}
