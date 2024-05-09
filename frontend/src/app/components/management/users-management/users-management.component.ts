import {Component, OnInit} from '@angular/core';
import {User} from '@/model/User';
import {ActivatedRoute, Router} from '@angular/router';

@Component({
    selector: 'app-users-management',
    templateUrl: './users-management.component.html',
    styleUrls: ['./users-management.component.scss'],
})
export class UsersManagementComponent implements OnInit {

    constructor(
        private router: Router,
        private route: ActivatedRoute,
    ) {
    }

    ngOnInit(): void {}

    toProfile(user: User): void {
        void this.router.navigate([user.userId],
            {relativeTo: this.route
        });
    }
}
