import {Component, OnInit} from '@angular/core';
import {Observable} from 'rxjs';
import {PenaltyEvent, PenaltyEventConstructor} from '../../shared/model/PenaltyEvent';
import {PenaltyService} from '../../services/api/penalties/penalty.service';
import {TranslateService} from '@ngx-translate/core';
import {AbstractControl, FormControl, FormGroup, Validators} from '@angular/forms';
import {HttpErrorResponse} from '@angular/common/http';
import {languageAsEnum} from '../../app.constants';

@Component({
  selector: 'app-penalty-events-management',
  templateUrl: './penalty-events-management.component.html',
  styleUrls: ['./penalty-events-management.component.css']
})
export class PenaltyEventsManagementComponent implements OnInit {
  penaltyEventsObs: Observable<PenaltyEvent[]>;
  errorOnRetrievingPenaltyEvents = false;

  currentPenaltyEventToDelete: PenaltyEvent = PenaltyEventConstructor.new();
  deletionWasSuccess: boolean = undefined;

  penaltyEventFormGroup = new FormGroup({
    code: new FormControl('', Validators.required),
    penaltyPoints: new FormControl('',
      Validators.compose([Validators.required, Validators.min(0)]))
  });

  descriptionsForNewPenaltyEvent: Map<string, string>;
  // emptyDescription is to be able to validate the descriptions cleanly:
  //   a description is valid is it differs from this.emptyDescription
  emptyDescription = '';
  additionWasSuccess: boolean = undefined;
  notAllSupportedLanguagesAreFilledInError = false;

  updateWasSuccess: boolean = undefined;

  supportedLanguagesTranslated: string[] = [];

  constructor(private penaltyService: PenaltyService,
              private translate: TranslateService) {
  }

  get code(): AbstractControl {
    return this.penaltyEventFormGroup.get('code');
  }

  get penaltyPoints(): AbstractControl {
    return this.penaltyEventFormGroup.get('penaltyPoints');
  }

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
      .findIndex(n => n === languageAsEnum[this.translate.currentLang]);

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

  clearFeedbackBooleans(): void {
    this.additionWasSuccess = undefined;
    this.updateWasSuccess = undefined;
    this.notAllSupportedLanguagesAreFilledInError = false;
  }

  /**
   * Be aware that the descriptions are provided in a 'Map' attribute, not in the FormGroup
   */
  prepareToAddPenaltyEvent(): void {
    // clear all feedback booleans:
    this.clearFeedbackBooleans();
    this.penaltyEventFormGroup.enable();

    // set the penaltyEventFormGroup
    this.penaltyEventFormGroup.setValue({
      code: '',
      penaltyPoints: ''
    });
    this.code.enable();
    this.penaltyPoints.enable();

    // set the descriptions
    this.setupDescriptionsForNewPenaltyEvent();
  }

  validPenaltyEventForm(): boolean {
    // use formGroup.invalid instead of formGroup.valid: if a form control
    // within a form group happens to be disabled, the 'valid' returns
    // false, but 'invalid' just checks the values of each form control
    // Note: I could not find this in the documentation, but tested this
    // through logging the 'valid' and 'invalid' values of the form controls
    // before and after disabling them in prepareToUpdatePenaltyEvent
    const validForm = !this.penaltyEventFormGroup.invalid;

    let validDescriptions = true;
    this.supportedLanguagesTranslated.forEach(lang => {
      if (this.descriptionsForNewPenaltyEvent.get(lang).trim() === this.emptyDescription) {
        validDescriptions = false;
      }
    });

    return validForm && validDescriptions;
  }

  addNewPenaltyEvent(value: { code: number, penaltyPoints: number }): void {
    if (this.validPenaltyEventForm()) {
      const penaltyEvent = this.penaltyEventFromFormAndDescriptions(value);

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

  /**
   * Be aware that the descriptions are provided in a 'Map' attribute, not in the FormGroup
   */
  prepareToUpdatePenaltyEvent(penaltyEvent: PenaltyEvent): void {
    // clear all feedback booleans:
    this.clearFeedbackBooleans();
    this.penaltyEventFormGroup.enable();

    // set the penaltyEventFormGroup
    this.penaltyEventFormGroup.setValue({
      code: penaltyEvent.code,
      penaltyPoints: penaltyEvent.points
    });
    this.code.disable();

    // disable if necessary
    if (!this.isPenaltyEventUpdatableOrDeletable(penaltyEvent.code)) {
      this.penaltyPoints.disable();
    }

    // set the descriptions
    this.setupDescriptionsForUpdate(penaltyEvent);
  }

  setupDescriptionsForUpdate(penaltyEvent: PenaltyEvent): void {
    this.descriptionsForNewPenaltyEvent = new Map<string, string>();
    this.supportedLanguagesTranslated.forEach(key => {
      this.descriptionsForNewPenaltyEvent.set(key, penaltyEvent.descriptions[key.toUpperCase()]);
    });
  }

  updatePenaltyEvent(): void {
    const value = {
      code: this.code.value,
      penaltyPoints: this.penaltyPoints.value
    };

    if (this.validPenaltyEventForm()) {
      const penaltyEvent = this.penaltyEventFromFormAndDescriptions(value);

      this.updateWasSuccess = null;
      this.penaltyService.updatePenaltyEvent(value.code, penaltyEvent).subscribe(
        () => {
          this.updateWasSuccess = true;
          this.penaltyEventsObs = this.penaltyService.getPenaltyEvents();
        }, (error: HttpErrorResponse) => {
          if (error.status === 417) {
            this.updateWasSuccess = undefined; // otherwise, two divs will show
            this.notAllSupportedLanguagesAreFilledInError = true;
          } else {
            this.updateWasSuccess = false;
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

  isPenaltyEventUpdatableOrDeletable(code: number): boolean {
    return !String(code).startsWith('1666');
  }

  penaltyEventFromFormAndDescriptions(value: { code: number, penaltyPoints: number }): PenaltyEvent {
    const penaltyEvent = PenaltyEventConstructor.new();
    penaltyEvent.code = value.code;
    penaltyEvent.points = value.penaltyPoints;

    const descriptions = {};
    for (const [key, val] of this.descriptionsForNewPenaltyEvent) {
      descriptions[key.toUpperCase()] = val;
    }
    penaltyEvent.descriptions = descriptions;
    return penaltyEvent;
  }
}
