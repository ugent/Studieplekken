import { Component, OnInit } from '@angular/core';
import {Observable} from 'rxjs';
import {PenaltyEvent, PenaltyEventConstructor} from '../../shared/model/PenaltyEvent';
import {PenaltyService} from '../../services/api/penalties/penalty.service';
import {transition, trigger, useAnimation} from '@angular/animations';
import {rowsAnimation} from '../../shared/animations/RowAnimation';
import {TranslateService} from '@ngx-translate/core';
import {languageAsEnum} from '../../../environments/environment';
import {AbstractControl, FormControl, FormGroup, Validators} from '@angular/forms';
import {HttpErrorResponse} from '@angular/common/http';

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

  penaltyEventFormGroup: FormGroup;
  descriptionsForNewPenaltyEvent: Map<string, string>;
  // emptyDescription is to be able to validate the descriptions cleanly:
  //   a description is valid is it differs from this.emptyDescription
  emptyDescription = '';
  additionWasSuccess: boolean = undefined;
  notAllSupportedLanguagesAreFilledInError = false;

  supportedLanguagesTranslated: string[] = [];

  constructor(private penaltyService: PenaltyService,
              private translate: TranslateService) { }

  ngOnInit(): void {
    // just to make sure that the getters below are creatable
    this.prepareToAddPenaltyEvent();

    this.penaltyEventsObs = this.penaltyService.getPenaltyEvents();
    this.penaltyEventsObs.subscribe(
      () => {
        this.errorOnRetrievingPenaltyEvents = false;
      }, () => {
        this.errorOnRetrievingPenaltyEvents = true;
      }
    );

    Object.keys(languageAsEnum).forEach(key => {
      this.translate.get('language.' + key).subscribe(lang => {
        // push the new language to the supported languages
        this.supportedLanguagesTranslated.push(lang);
        // and make sure that there is a description entry in the map
        // note: it indeed recreates the full map
        this.setupDescriptionsForNewPenaltyEvent();
      });
    });
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

  setupDescriptionsForNewPenaltyEvent(): void {
    // prepare descriptions
    this.descriptionsForNewPenaltyEvent = new Map<string, string>();
    this.supportedLanguagesTranslated.forEach(key => {
      this.descriptionsForNewPenaltyEvent.set(key, this.emptyDescription);
    });
  }

  /**
   * Be aware that the descriptions are provided in a 'Map' attribute, not in the FormGroup
   */
  prepareToAddPenaltyEvent(): void {
    // clear all feedback booleans:
    this.additionWasSuccess = undefined;
    this.notAllSupportedLanguagesAreFilledInError = false;

    this.penaltyEventFormGroup = new FormGroup({
      code: new FormControl('', Validators.required),
      penaltyPoints: new FormControl('',
        Validators.compose([Validators.required, Validators.min(0)]))
    });

    this.setupDescriptionsForNewPenaltyEvent();
  }

  validFormToAddPenaltyEvent(): boolean {
    const validForm = this.penaltyEventFormGroup.valid;

    let validDescriptions = true;
    this.supportedLanguagesTranslated.forEach(lang => {
      if (this.descriptionsForNewPenaltyEvent.get(lang).trim() === this.emptyDescription) {
        validDescriptions = false;
      }
    });

    return validForm && validDescriptions;
  }

  addNewPenaltyEvent(value: {code: number, penaltyPoints: number}): void {
    if (this.validFormToAddPenaltyEvent()) {
      const penaltyEvent = PenaltyEventConstructor.new();
      penaltyEvent.code = value.code;
      penaltyEvent.points = value.penaltyPoints;

      const descriptions = {};
      for (const [key, val] of this.descriptionsForNewPenaltyEvent) {
        descriptions[key.toUpperCase()] = val;
      }
      penaltyEvent.descriptions = descriptions;

      this.additionWasSuccess = null;
      this.penaltyService.addPenaltyEvent(penaltyEvent).subscribe(
        () => {
          this.additionWasSuccess = true;
          this.penaltyEventsObs = this.penaltyService.getPenaltyEvents();
        }, (error: HttpErrorResponse) => {
          if (error.status === 417) {
            this.additionWasSuccess = undefined; // otherwise, two divs will show
            this.notAllSupportedLanguagesAreFilledInError = true;
          } else {
            this.additionWasSuccess = false;
          }
        }
      );
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

  isPenaltyEventDeletable(code: number): boolean {
    return !String(code).startsWith('1666');
  }

  get code(): AbstractControl { return this.penaltyEventFormGroup.get('code'); }
  get penaltyPoints(): AbstractControl { return this.penaltyEventFormGroup.get('penaltyPoints'); }
}
