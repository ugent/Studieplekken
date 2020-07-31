import { Component, OnInit } from '@angular/core';
import {LocationReservationService} from "../../services/location-reservation.service";
import {IPenaltyEvent} from "../../interfaces/IPenaltyEvent";
import {TranslateService} from "@ngx-translate/core";
import {PenaltyService} from "../../services/penalty.service";
import {appLanguages, languageTranslations} from '../../environments/environment';
import {rowsAnimation} from '../animations';
import {transition, trigger, useAnimation} from "@angular/animations";



@Component({
  selector: 'app-profile-help',
  templateUrl: './profile-help.component.html',
  styleUrls: ['./profile-help.component.css'],
  animations: [trigger('rowsAnimation', [
    transition('void => *', [
      useAnimation(rowsAnimation)
    ])
  ])]
})
export class ProfileHelpComponent implements OnInit {
  public max_penalty_points: number;
  penalties: IPenaltyEvent[];

  appLanguages: {};
  languageTranslations: {};

  constructor(private locationReservationService: LocationReservationService, public translate: TranslateService, private penaltyService: PenaltyService) {
    this.penalties=[];
    this.appLanguages = appLanguages;
    this.languageTranslations = languageTranslations;
  }

  ngOnInit(): void {
    this.locationReservationService.MAX_PENALTY_POINTS.subscribe( value => {
      this.max_penalty_points = value;
    });
    this.penaltyService.getAllPenaltyEvents().subscribe(r => {
      this.penalties = r.filter(i => i.publicAccessible == true);
    });
  }

}
