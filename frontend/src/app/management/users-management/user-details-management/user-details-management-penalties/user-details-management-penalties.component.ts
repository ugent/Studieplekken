import { Component, Input, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { User } from '../../../../shared/model/User';
import { Penalty, PenaltyConstructor } from '../../../../shared/model/Penalty';
import { PenaltyService } from '../../../../services/api/penalties/penalty.service';
import { UserDetailsService } from '../../../../services/single-point-of-truth/user-details/user-details.service';
import {
  AbstractControl,
  FormControl,
  FormGroup,
  Validators,
} from '@angular/forms';
import { LocationService } from '../../../../services/api/locations/location.service';
import { Location } from '../../../../shared/model/Location';
import * as moment from 'moment';
import { Moment } from 'moment';
import { penaltyEventCodeForManualEntry } from '../../../../app.constants';

@Component({
  selector: 'app-user-details-management-penalties',
  templateUrl: './user-details-management-penalties.component.html',
  styleUrls: ['./user-details-management-penalties.component.scss'],
})
export class UserDetailsManagementPenaltiesComponent implements OnInit {
  @Input() userObs: Observable<User>;
  userId: string;

  penaltiesObs: Observable<Penalty[]>;
  queryingPenaltiesError = false;

  currentPenaltyToDelete: Penalty = PenaltyConstructor.new();
  errorOnDeletingPenalty: boolean = undefined;

  newPenaltyFormGroup: FormGroup;
  errorOnAddingPenalty: boolean = undefined;

  locationObs: Observable<Location[]>;

  constructor(
    private penaltyService: PenaltyService,
    private userDetailsService: UserDetailsService,
    private locationService: LocationService
  ) {}

  ngOnInit(): void {
    this.userObs.subscribe(
      (next) => {
        if (next.userId === '') {
          return;
        }

        this.penaltiesObs = this.penaltyService.getPenaltiesOfUserById(
          next.userId
        );
        this.userId = next.userId;
      },
      () => {
        this.queryingPenaltiesError = true;
      }
    );

    this.locationObs = this.locationService.getLocations();

    // this is called to be able to create this component, otherwise the getters will
    // try to access something 'undefined'
    this.setupFormToAddPenalty();
  }

  prepareToAddPenalty(): void {
    this.setupFormToAddPenalty();
    this.errorOnAddingPenalty = undefined;
  }

  setupFormToAddPenalty(): void {
    this.newPenaltyFormGroup = new FormGroup({
      timestamp: new FormControl('', Validators.required.bind(this)),
      location: new FormControl('', Validators.required.bind(this)),
      points: new FormControl('', Validators.required.bind(this)),
      remarks: new FormControl(''),
    });
  }

  addPenalty(value: {
    timestamp: string;
    location: number;
    points: number;
    remarks: string;
  }): void {
    const penalty = PenaltyConstructor.new();
    penalty.userId = this.userId;
    penalty.eventCode = penaltyEventCodeForManualEntry;
    penalty.timestamp = moment(value.timestamp);
    penalty.reservationDate = null;
    penalty.reservationLocationId = Number(value.location);
    penalty.receivedPoints = value.points;
    penalty.remarks = value.remarks;

    this.errorOnAddingPenalty = null;
    this.penaltyService.addPenalty(penalty).subscribe(
      () => {
        this.successAdditionHandler();
      },
      () => {
        this.errorAdditionHandler();
      }
    );
  }

  successAdditionHandler(): void {
    this.errorOnAddingPenalty = false;
    this.userDetailsService.loadUser(this.userId);
  }

  errorAdditionHandler(): void {
    this.errorOnAddingPenalty = true;
  }

  prepareToDeletePenalty(penalty: Penalty): void {
    this.errorOnDeletingPenalty = undefined;
    this.currentPenaltyToDelete = penalty;
  }

  deletePenaltyLinkedToCurrentPenaltyToDelete(): void {
    this.errorOnDeletingPenalty = null;
    this.penaltyService.deletePenalty(this.currentPenaltyToDelete).subscribe(
      () => {
        this.successDeletionHandler();
      },
      () => {
        this.errorOnDeletingPenalty = true;
      }
    );
  }

  successDeletionHandler(): void {
    this.errorOnDeletingPenalty = false;
    this.userDetailsService.loadUser(this.userId);
  }

  toDateTimeViewString(date: Moment): string {
    return date.format('DD-MM-YYYY HH:mm');
  }

  validForm(): boolean {
    return this.newPenaltyFormGroup.valid;
  }

  getLocation(locationId: number): Observable<Location> {
    return this.locationService.getLocation(locationId);
  }

  get timestamp(): AbstractControl {
    return this.newPenaltyFormGroup.get('timestamp');
  }

  get location(): AbstractControl {
    return this.newPenaltyFormGroup.get('location');
  }

  get points(): AbstractControl {
    return this.newPenaltyFormGroup.get('points');
  }

  get remarks(): AbstractControl {
    return this.newPenaltyFormGroup.get('remarks');
  }
}
