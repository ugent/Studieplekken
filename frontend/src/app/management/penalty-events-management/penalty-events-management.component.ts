import { Component, OnInit } from '@angular/core';
import {Observable} from 'rxjs';
import {PenaltyEvent, PenaltyEventConstructor} from '../../shared/model/PenaltyEvent';
import {PenaltyService} from '../../services/api/penalties/penalty.service';
import {transition, trigger, useAnimation} from '@angular/animations';
import {rowsAnimation} from '../../shared/animations/RowAnimation';
import {TranslateService} from '@ngx-translate/core';
import {languageAsEnum} from '../../../environments/environment';

@Component({
  selector: 'app-penalty-events-management',
  templateUrl: './penalty-events-management.component.html',
  styleUrls: ['./penalty-events-management.component.css'],
  animations: [trigger('rowsAnimation', [
    transition('void => *', [
      useAnimation(rowsAnimation)
    ])
  ])]
})
export class PenaltyEventsManagementComponent implements OnInit {
  penaltyEventsObs: Observable<PenaltyEvent[]>;
  errorOnRetrievingPenaltyEvents = false;

  currentPenaltyEventToDelete: PenaltyEvent = PenaltyEventConstructor.new();
  deletionWasSuccess: boolean = undefined;

  constructor(private penaltyService: PenaltyService,
              private translate: TranslateService) { }

  ngOnInit(): void {
    this.penaltyEventsObs = this.penaltyService.getPenaltyEvents();
    this.penaltyEventsObs.subscribe(
      () => {
        this.errorOnRetrievingPenaltyEvents = false;
      }, () => {
        this.errorOnRetrievingPenaltyEvents = true;
      }
    );
  }

  descriptionToShow(penaltyEvent: PenaltyEvent): string {
    let idx = Object.keys(penaltyEvent.descriptions)
      .findIndex(n =>  n === languageAsEnum[this.translate.currentLang]);

    // if browser language is not supported, return ENGLISH
    if (idx < 0) {
      idx = Object.keys(penaltyEvent.descriptions)
        .findIndex(n => n === languageAsEnum.en);
    } else {
      return penaltyEvent.descriptions[languageAsEnum[this.translate.currentLang]];
    }

    // if ENGLISH is not found, try to return the first supported language
    if (idx < 0) {
      if (Object.keys(penaltyEvent.descriptions).length > 0) {
        return penaltyEvent.descriptions[Object.keys(penaltyEvent.descriptions)[0]];
      } else {
        return '';
      }
    } else {
      return penaltyEvent.descriptions[languageAsEnum[this.translate.currentLang]];
    }
  }

  prepareToDeletePenaltyEvent(penaltyEvent: PenaltyEvent): void {
    this.deletionWasSuccess = undefined;
    this.currentPenaltyEventToDelete = penaltyEvent;
  }

  deletePenaltyEventLinkedToCurrentToDelete(): void {
    this.deletionWasSuccess = null;
    this.penaltyService.deletePenaltyEvent(this.currentPenaltyEventToDelete).subscribe(
      () => {
        this.deletionWasSuccess = true;
        this.penaltyEventsObs = this.penaltyService.getPenaltyEvents();
      }, () => {
        this.deletionWasSuccess = false;
      }
    );
  }

  isPenaltyEventDeletable(penaltyEvent: PenaltyEvent): boolean {
    return !String(penaltyEvent.code).startsWith('1666');
  }
}
