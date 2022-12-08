import {Injectable} from '@angular/core';
import {Router} from '@angular/router';

@Injectable({
    providedIn: 'root',
})
export class LoginRedirectService {

    constructor(private router: Router) {}

    registerUrl(url: string): void {
        localStorage.setItem('afterLogin', url);
    }

    navigateToLastUrl(): void {
        const url = localStorage.getItem('afterLogin');
        localStorage.removeItem('afterLogin');
        void this.router.navigateByUrl(url);
    }
}
