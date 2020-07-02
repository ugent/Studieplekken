import { Component, OnInit } from '@angular/core';
import {AuthenticationService} from "../../services/authentication.service";
import {PenaltyService} from "../../services/penalty.service";
import {IPenalty} from "../../interfaces/IPenalty";
import {urls} from "../../environments/environment";
import {IUser} from "../../interfaces/IUser";
import {transition, trigger, useAnimation} from "@angular/animations";
import {rowsAnimation} from "../animations";

@Component({
  selector: 'app-profile-penalties-overview',
  templateUrl: './profile-penalties-overview.component.html',
  styleUrls: ['./profile-penalties-overview.component.css'],
  animations: [trigger('rowsAnimation', [
    transition('void => *', [
      useAnimation(rowsAnimation)
    ])
  ])]
})
export class ProfilePenaltiesOverviewComponent implements OnInit {
  penalties: IPenalty[];
  me: IUser;

  constructor(private authenticationService: AuthenticationService, private penaltyService: PenaltyService) { }

  ngOnInit(): void {
    this.me = this.authenticationService.getCurrentUser();
    this.penaltyService.getPenalties(this.me.augentID).subscribe(n => {
      this.penalties = n;
    });
  }

}
