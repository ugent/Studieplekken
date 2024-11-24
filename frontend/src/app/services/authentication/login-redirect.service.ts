import {Injectable} from '@angular/core';
import {Router} from '@angular/router';

@Injectable({
    providedIn: 'root',
})
export class LoginRedirectService {
    private static readonly REDIRECT_DEFAULT = '/profile/overview';
    private static readonly REDIRECT_KEY = 'afterLogin';

    constructor(private router: Router) {}

    /**
     * Stores the provided URL in the local storage under the key 'afterLogin'.
     * This URL can be used to redirect the user after a successful login.
     *
     * @param url - The URL to be stored for redirection after login.
     */
    public registerUrl(url: string): void {
        localStorage.setItem(LoginRedirectService.REDIRECT_KEY, url);
    }

    /**
     * Checks if a redirect URL is stored in localStorage.
     *
     * @returns {boolean} - True if a redirect URL is stored in localStorage, false otherwise.
     */
    public hasRedirectUrl(): boolean {
        return localStorage.getItem(LoginRedirectService.REDIRECT_KEY) != null;
    }

    /**
     * Navigates to the last URL stored in localStorage after login.
     * 
     * This method retrieves the URL stored under the key 'afterLogin' in localStorage,
     * removes the item from localStorage, and then navigates to that URL.
     * If no URL is found, it defaults to navigating to '/profile/overview'.
     * 
     * @returns {void}
     */
    public navigateToLastUrl(): void {
        // Pull the URL stored in localStorage.
        const url = localStorage.getItem(LoginRedirectService.REDIRECT_KEY);
        localStorage.removeItem(LoginRedirectService.REDIRECT_KEY);

        // Navigate to the stored URL or the default URL.
        void this.router.navigateByUrl(
            url || LoginRedirectService.REDIRECT_DEFAULT
        );
    }
}
