import {Component, Input, OnInit} from '@angular/core';
import {Location, LocationConstructor} from '@/model/Location';
import {FormBuilder, FormControl, FormGroup} from '@angular/forms';
import {LocationService} from '@/services/api/locations/location.service';
import {
    LocationDetailsService
} from '@/services/single-point-of-truth/location-details/location-details.service';
import {Authority} from '@/model/Authority';
import {Building} from 'src/app/model/Building';
import {msToShowFeedback} from '@/app.constants';

@Component({
    selector: 'app-details-form',
    templateUrl: './details-form.component.html',
    styleUrls: ['./details-form.component.scss'],
})
export class DetailsFormComponent implements OnInit {
    @Input() location: Location;
    @Input() authorities: Authority[];
    @Input() buildings: Building[];

    protected formGroup: FormGroup;

    protected disableEditLocationButton = false;
    protected disableCancelLocationButton = true;
    protected disablePersistLocationButton = true;

    protected successUpdatingLocation: boolean = undefined;

    constructor(
        private locationService: LocationService,
        private locationDetailsService: LocationDetailsService,
        private formBuilder: FormBuilder
    ) {
        this.formGroup = this.formBuilder.group({
            name: null,
            authority: null,
            building: null,
            numberOfSeats: null,
            forGroup: null,
            imageUrl: null,
            usesPenaltyPoints: null,
            hidden: null
        });
    }

    public ngOnInit(): void {
        this.setupForm();
    }

    public setupForm(): void {
        this.formGroup.patchValue({
            name: this.location.name,
            authority: this.location.authority.authorityId,
            building: this.location.building.buildingId,
            numberOfSeats: this.location.numberOfSeats,
            forGroup: this.location.forGroup,
            imageUrl: this.location.imageUrl,
            usesPenaltyPoints: this.location.usesPenaltyPoints,
            hidden: this.location.hidden
        });

        this.disableFormGroup();
    }

    public disableFormGroup(): void {
        this.formGroup.disable();
    }

    public enableFormGroup(): void {
        this.formGroup.enable();
    }

    public toggleFormButtons(): void {
        this.disableEditLocationButton = !this.disableEditLocationButton;
        this.disableCancelLocationButton = !this.disableCancelLocationButton;
        this.disablePersistLocationButton = !this.disablePersistLocationButton;
    }

    public editLocationDetailsButtonClick(): void {
        this.enableFormGroup();
        this.toggleFormButtons();
    }

    public cancelLocationDetailsButtonClick(): void {
        this.setupForm();
        this.toggleFormButtons();

        this.successUpdatingLocation = undefined;
    }

    public persistLocationDetailsButtonClick(): void {
        this.successUpdatingLocation = null; // show 'loading' message

        const from = this.location;
        const to = this.locationInForm;

        this.locationService.updateLocation(from.locationId, to).subscribe(
            () => {
                this.successHandler();

                // update the location attribute: 'loadLocation' will perform a next()
                // on the subject behavior, which will trigger a next() on the underlying
                // observable, to which the HTML is implicitly subscribed through the
                // *ngIf="location | async as location" in the outer div of the template.
                this.locationDetailsService.loadLocation(to.locationId);
            },
            () => {
                this.errorHandler();
                // reload the location to be sure
                this.locationDetailsService.loadLocation(from.locationId);
            }
        );

        this.disableFormGroup();
        this.toggleFormButtons();
    }

    get locationInForm(): Location {
        const formValue = this.formGroup.value;

        const location: Location = LocationConstructor.newFromObj({
            ...this.location,
            name: String(formValue.name),
            authority: this.authorities.find(authority =>
                authority.authorityId === formValue.authority
            ),
            building: this.buildings.find(building =>
                building.buildingId === Number(formValue.building)
            ),
            numberOfSeats: Number(formValue.numberOfSeats),
            numberOfLockers: this.location.numberOfLockers,
            forGroup: Boolean(formValue.forGroup),
            imageUrl: String(formValue.imageUrl),
            usesPenaltyPoints: Boolean(formValue.usesPenaltyPoints),
            hidden: Boolean(formValue.hidden)
        });

        return location;
    }

    protected successHandler(): void {
        this.successUpdatingLocation = true;

        setTimeout(
            () => (this.successUpdatingLocation = undefined),
            msToShowFeedback
        );
    }

    protected errorHandler(): void {
        this.successUpdatingLocation = false;

        setTimeout(
            () => (this.successUpdatingLocation = undefined),
            msToShowFeedback
        );
    }
}
