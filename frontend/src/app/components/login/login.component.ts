import {Component} from '@angular/core';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {environment} from '@env/environment';
import {userWantsTLogInLocalStorageKey} from '@/app.constants';
import {AuthenticationService} from '@/services/authentication/authentication.service';

@Component({
    selector: 'app-login',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.scss'],
})
export class LoginComponent {
    public casFlowTriggerUrl: string = environment.casFlowTriggerUrl;
    public hoGentFlowTriggerUrl: string = environment.hoGentFlowTriggerUrl;
    public arteveldeHSFlowTriggerUrl: string = environment.arteveldeHSFlowTriggerUrl;
    public odiseeFlowTriggerUrl: string = environment.odiseeFlowTriggerUrl;
    public lucaFlowTriggerUrl: string = environment.lucaFlowTriggerUrl;
    public stadGentFlowTriggerUrl: string = environment.stadGentFlowTriggerUrl;
    public kulFlowTriggerUrl: string = environment.kulFlowTriggerUrl;
    public otherFlowTriggerUrl: string = environment.otherFlowTriggerUrl;

    constructor(route: ActivatedRoute, authService: AuthenticationService, router: Router) {
        route.queryParamMap.subscribe((map: ParamMap) => {            
            if (map.has('token')) {
                localStorage.setItem('access_token', map.get('token'));
                authService.login(true);
            }
        });
    }

    /**
     * When the user clicked on the login button, we set the localStorage's
     * key 'userWantsToLogin' to 'true', so that after redirect from the backend
     * to /dashboard, and the method AuthenticationService#login() is called,
     * the user can be logged in.
     */
    loginButtonClicked(): void {
        localStorage.setItem(userWantsTLogInLocalStorageKey, 'true');
    }

    getCallbackUrl(): string {
        return `${window.location.origin}/login`;
    }
}
