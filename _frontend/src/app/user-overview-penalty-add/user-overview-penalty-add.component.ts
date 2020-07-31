import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Observable} from "rxjs";
import {IPenalty} from "../../interfaces/IPenalty";
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {IPenaltyEvent} from "../../interfaces/IPenaltyEvent";
import {PenaltyService} from "../../services/penalty.service";
import {ILocation} from "../../interfaces/ILocation";
import {LocationService} from "../../services/location.service";
import {appLanguages} from "../../environments/environment";
import {TranslateService} from "@ngx-translate/core";
import {IDate} from "../../interfaces/IDate";

@Component({
  selector: 'app-user-overview-penalty-add',
  templateUrl: './user-overview-penalty-add.component.html',
  styleUrls: ['./user-overview-penalty-add.component.css']
})
export class UserOverviewPenaltyAddComponent implements OnInit {
  @Input() display: Observable<string>;
  @Input() augentID: string;
  @Output() newPenaltyEvent = new EventEmitter<IPenalty>();

  displayThis: string;
  penaltyEvents: IPenaltyEvent[];
  locations: ILocation[];

  newPenaltyForm: FormGroup;

  appLanguages = appLanguages;

  constructor(private penaltyService: PenaltyService, private locationService: LocationService
              , public translateService: TranslateService) {
    this.newPenaltyForm = new FormGroup({
      penaltyEvent: new FormControl('', Validators.required),
      timestamp: new FormControl('', Validators.required),
      reservationDate: new FormControl('', Validators.required),
      reservationLocation: new FormControl('', Validators.required)
    });
  }

  ngOnInit(): void {
    this.display.subscribe(d => {
      this.displayThis = d;
      if (d !== 'none' && this.augentID !== undefined && this.augentID.length !== 0) {
        this.penaltyService.getAllPenaltyEvents().subscribe(n => {
          this.penaltyEvents = n;

          console.log(n[0].code + ': ' + n[0].descriptions[appLanguages[this.translateService.currentLang]])
        });
        this.locationService.getAllLocationsWithoutLockersAndCalendar().subscribe(n => {
          this.locations = n;
        })
      }
    });
  }

  add(value: any) {
    if (this.newPenaltyForm.valid) {
      let ret: IPenalty = {
        augentID: this.augentID,
        eventCode: +value.penaltyEvent,
        timestamp: this.createIDate(value.timestamp),
        reservationDate: this.createIDate(value.reservationDate),
        reservationLocation: value.reservationLocation,
        receivedPoints: -1 // this value will indicate to the backend that it's a new penalty and the according points should be filled in
      };
      this.newPenaltyForm.reset();
      this.newPenaltyEvent.emit(ret);
    }
  }

  cancel(): void {
    this.newPenaltyForm.reset();
    this.newPenaltyEvent.emit(null);
  }

  createIDate(date: string): IDate {
    return {
      year: +(date.substr(0, 4)),
      month: +(date.substr(5, 2)),
      day: +(date.substr(8, 2)),
      hrs: +(date.substr(11, 2)),
      min: +(date.substr(14, 2)),
      sec: 0
    };
  }
}
