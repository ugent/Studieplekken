import { Component, OnInit } from '@angular/core';
import {Router} from "@angular/router";
import {urls} from "../../environments/environment";

@Component({
  selector: 'app-process',
  templateUrl: './process.component.html',
  styleUrls: ['./process.component.css']
})
export class ProcessComponent implements OnInit {

  constructor(private router: Router) {
    // CAS will return to this URL when a user is succesfully logged in.
    // When scucessfully logged in the user gets redirected to the dashboard
    // This redirection is needed, otherwise CAS will be triggered to show
    // his login page on every refresh.
    this.router.navigateByUrl(urls.dashboard).then();
  }

  ngOnInit(): void {
  }

}
