import {Component, OnInit} from '@angular/core';
import {IPenaltyEvent} from '../../interfaces/IPenaltyEvent';
import {TranslateService} from '@ngx-translate/core';
import {appLanguages, languageTranslations} from '../../environments/environment';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {AuthenticationService} from '../../services/authentication.service';
import {transition, trigger, useAnimation} from '@angular/animations';
import {rowsAnimation} from '../animations';
import {PenaltyService} from '../../services/penalty.service';

@Component({
  selector: 'app-penalties',
  templateUrl: './penalties.component.html',
  styleUrls: ['./penalties.component.css'],
  animations: [trigger('rowsAnimation', [
    transition('void => *', [
      useAnimation(rowsAnimation)
    ])
  ])]
})
export class PenaltiesComponent implements OnInit {

  results: IPenaltyEvent[];
  selected: IPenaltyEvent;
  backupSelected: IPenaltyEvent;

  displayDelete = 'none';
  // this selected has nothing to do with this.selected (defined above), this is is for the delete popup
  selectedCode: number;

  display1666 = 'none';
  errorMessage = '';
  displayErrorMessage = false;

  penaltyForm = new FormGroup({
    code: new FormControl('', [Validators.required, Validators.min(0)]),
    points: new FormControl('', [Validators.required, Validators.min(0)]),
    publicAccessible: new FormControl('true')
    // descriptions will become a FormGroup of its own, see constructor for implementation
  });

  changePenaltyForm = new FormGroup({
    code: new FormControl('', [Validators.required, Validators.min(0)]),
    points: new FormControl('', [Validators.required, Validators.min(0)]),
    publicAccessible: new FormControl('true')
    // descriptions will become a FormGroup of its own, see constructor for implementation
  });


  Object: any;
  appLanguages: {};
  languageTranslations: {};

  constructor(public translate: TranslateService, public authenticationService: AuthenticationService,
              private penaltyService: PenaltyService) {
    this.results = [];
    this.Object = Object;
    this.appLanguages = appLanguages;
    this.languageTranslations = languageTranslations;

    // add the supported languages to the this.new instance, otherwise the form of a new PenaltyEvent won't
    // be able to add a description for a supported language
    let descriptions = new FormGroup({});
    for (let lang of Object.keys(appLanguages)) {
      // appLanguages[lang] gives the value that Spring can convert to the used enum 'be.ugent.blok2.helpers.Language'
      descriptions.addControl(appLanguages[lang], new FormControl('', Validators.required));
    }
    this.penaltyForm.addControl('descriptions', descriptions);
    this.changePenaltyForm.addControl('descriptions', descriptions);
  }

  ngOnInit(): void {
    this.penaltyService.getAllPenaltyEvents().subscribe(r => {
      this.results = r;
    });
  }

  setSelectedPenaltyEvent(penalty: IPenaltyEvent): void {
    this.selected = penalty;
    this.backupSelected = JSON.parse(JSON.stringify(penalty));

    this.changePenaltyForm.controls.code.setValue(penalty.code);
    this.changePenaltyForm.controls.points.setValue(penalty.points);
    this.changePenaltyForm.controls.publicAccessible.setValue(penalty.publicAccessible);
    for (let l of Object.keys(appLanguages)) {
      this.changePenaltyForm.controls.descriptions['controls'][appLanguages[l]].setValue(penalty.descriptions[appLanguages[l]]);
    }

  }

  cancel(): void {
    let idx = this.results.findIndex(v => {
      return this.selected.code === v.code;
    });
    this.results[idx] = this.backupSelected;
    this.changePenaltyForm.reset();
  }

  change(): void {
    if (this.changePenaltyForm.valid) {
      this.selected.points = this.changePenaltyForm.controls.points.value;
      this.selected.publicAccessible = this.changePenaltyForm.controls.publicAccessible.value;
      for (let l of Object.keys(appLanguages)) {
        this.selected.descriptions[appLanguages[l]] = this.changePenaltyForm.controls.descriptions['controls'][appLanguages[l]].value;
      }
      this.penaltyService.changePenaltyEvent(this.selected).subscribe(value => {
      });
      this.changePenaltyForm.reset();
      document.getElementById('hideChangePenaltyModal').click();
    } else {
      Object.keys(this.changePenaltyForm.controls).forEach(field => {
        const control = this.changePenaltyForm.get(field);
        control.markAsTouched({onlySelf: true});
      });
      Object.keys(this.changePenaltyForm.controls["descriptions"]['controls']).forEach(field => {
        const control = this.changePenaltyForm.controls['descriptions']['controls'][field];
        control.markAsTouched({ onlySelf: true });
      });
    }
  }

  addPenaltyEvent(value: any) {
    document.getElementById('publicAccessible').click();
    document.getElementById('publicAccessible').click();
    if (this.penaltyForm.valid) {
      let penalty: IPenaltyEvent = {
        code: value.code,
        points: value.points,
        publicAccessible: value.publicAccessible,
        descriptions: {}
      };

      // add the descriptions to the object 'penalty'
      for (let lang of Object.keys(appLanguages)) {
        penalty.descriptions[appLanguages[lang]] = this.penaltyForm.controls.descriptions['controls'][appLanguages[lang]].value;
      }

      if (this.isReserved(penalty) && this.display1666 !== 'block') {
        this.display1666 = 'block';
        // return here because if user clicks 'yes' in popup, then
        // this method will be called again, and display1666 will be at 'block'
        // and then the else clause will be called.
        return;
      } else {
        this.penaltyService.addPenaltyEvent(penalty).subscribe(value => {
          this.displayErrorMessage = false;
          this.results.push(penalty);
          this.results.sort((a: IPenaltyEvent, b: IPenaltyEvent) => {
            return a.code - b.code;
          });
          this.penaltyForm.reset();
        }, err => {
          this.displayErrorMessage = true;
        });

      }
    } else {
      // when a field was left empty and untouched this loop will mark it as touched and this will trigger validation
      Object.keys(this.penaltyForm.controls).forEach(field => {
        const control = this.penaltyForm.get(field);
        control.markAsTouched({onlySelf: true});
      });
      Object.keys(this.penaltyForm.controls["descriptions"]['controls']).forEach(field => {
        const control = this.penaltyForm.controls['descriptions']['controls'][field];
        control.markAsTouched({ onlySelf: true });
      });
    }
  }

  cancelAdd(event) {
    event.preventDefault();
    this.penaltyForm.reset();
    this.displayErrorMessage = false;
  }

  deletePenaltyEvent(code: number) {
    this.penaltyService.deletePenaltyEvent(code).subscribe(r => {
      let idx = this.results.findIndex(v => v.code === code);
      if (idx > -1) {
        this.results.splice(idx, 1);
      }
    });
  }

  /**
   * The events with a code that starts with 1666 is a reserved code and may not be changed
   * by anyone.
   * Fun side note: the 1666 comes from 1 resembling a ! (NOT) and 666 the number of the devil (-> death)
   */
  isReserved(event: IPenaltyEvent): boolean {
    let str = '' + event.code;
    if (str.length >= 4 && str.slice(0, 4) === '1666') {
      return true;
    }
    return false;
  }
}
