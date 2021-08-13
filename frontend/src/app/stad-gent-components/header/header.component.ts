import { AfterViewChecked, AfterViewInit, Component, ElementRef, OnInit } from '@angular/core';
import { Subject } from 'rxjs';
import { delay, distinctUntilChanged, map, tap } from 'rxjs/operators';
import { AuthenticationService } from 'src/app/services/authentication/authentication.service';
import {BreadcrumbService} from "./breadcrumbs/breadcrumb.service"

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit, AfterViewInit {

  accordionSubject = new Subject<boolean>();

  constructor(private breadcrumbService: BreadcrumbService, private authenticationService: AuthenticationService, private elem: ElementRef) { }

  ngOnInit(): void {

  }

  ngAfterViewInit() {
  }

  getLinkedBreadcrumbs() {
    return this.breadcrumbService.getCurrentBreadcrumbs().pipe(map(v => v.slice(0,-1)))
  }

  getUnlinkedBreadcrumbs() {
    return this.breadcrumbService.getCurrentBreadcrumbs().pipe(map(v => v.slice(-1)))
  }

  getUser() {
    return this.authenticationService.user
  }

  logout(): void {
    this.authenticationService.logout();
  }
}
