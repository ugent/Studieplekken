import {Component, Input, OnInit} from '@angular/core';
import {User, UserSettings} from '@/model/User';
import {FormControl, FormGroup} from '@angular/forms';
import {UserService} from '@/services/api/users/user.service';

@Component({
    selector: 'app-profile-overview',
    templateUrl: './profile-overview.component.html',
    styleUrls: ['./profile-overview.component.scss'],
})
export class ProfileOverviewComponent implements OnInit {

    @Input()
    protected user: User;
    protected logoSource: string;

    constructor(
        private userService: UserService
    ) {
    }

    public ngOnInit(): void {
        this.logoSource = this.getLogoSource(this.user.institution);
    }

    /**
     * Handles the error that occurs when loading the logo.
     * Sets the logo source to a default or fallback value.
     * 
     * @protected
     */
    protected handleLogoError(): void {
        this.logoSource = this.getLogoSource('Other');
    }

    /**
     * Constructs the source path for a logo image.
     *
     * @param logo - The name of the logo file without extension.
     * @returns The full path to the logo image in the assets directory.
     */
    private getLogoSource(logo: string): string {
        return '/assets/images/logo/' + logo + '_resized.png';
    }
}
