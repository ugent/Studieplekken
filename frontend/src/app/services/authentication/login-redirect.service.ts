import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root',
})
export class LoginRedirectService {

  constructor(private router: Router) {}

  registerUrl(url: string) {
    localStorage.setItem("afterLogin", url);
  }

  navigateToLastUrl() {
    const url = localStorage.getItem("afterLogin") || (environment.useExternalDashboard ? '/management':'/dashboard');
    localStorage.removeItem("afterLogin");
    this.router.navigateByUrl(url);
  }
}
