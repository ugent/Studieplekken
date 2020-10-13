import { Component, OnInit } from '@angular/core';
import {vars} from '../../environments/environment';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

  casFlowTriggerUrl = vars.casFlowTriggerUrl;

  constructor() { }

  ngOnInit(): void {
  }

}
