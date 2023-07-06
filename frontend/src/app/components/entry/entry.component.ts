import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { environment } from 'src/environments/environment';

@Component({
  selector: 'app-entry',
  templateUrl: './entry.component.html',
  styleUrls: ['./entry.component.scss'],
})
export class EntryComponent implements OnInit {
  constructor(private router: Router) {}

  ngOnInit(): void {
    if (!environment.useExternalDashboard) {
      this.router.navigateByUrl('/dashboard');
    } else {
      // Simulate an HTTP redirect:
      window.location.replace(environment.externalDashboardUrl);
    }
  }
}
