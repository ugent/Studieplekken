import {Component, Input, OnInit} from '@angular/core';
import {Observable} from 'rxjs';
import {Location} from '../../../../../extensions/model/Location';
import * as ClassicEditor from '@ckeditor/ckeditor5-build-classic';
import {
    LocationDetailsService
} from '../../../../../extensions/services/single-point-of-truth/location-details/location-details.service';
import {LocationService} from '../../../../../extensions/services/api/locations/location.service';
import {msToShowFeedback} from '../../../../../app.constants';
import {switchMap, take} from 'rxjs/operators';

@Component({
    selector: 'app-location-description',
    templateUrl: './location-description.component.html',
    styleUrls: ['./location-description.component.scss'],
})
export class LocationDescriptionComponent implements OnInit {
    @Input() location: Observable<Location>;

    editor: unknown = ClassicEditor;

    modelInDataLayer = {
        dutch: '',
        english: '',
    };

    model = {
        dutch: '',
        english: '',
    };

    config = {
        toolbar: [
            'heading',
            '|',
            'bold',
            'italic',
            '|',
            'numberedList',
            'bulletedList',
            '|',
            'link',
            'blockQuote',
            'insertTable',
            '|',
            'undo',
            'redo',
        ],
    };

    showUpdateSuccess: boolean = undefined;

    constructor(
        private locationDetailsService: LocationDetailsService,
        private locationService: LocationService
    ) {
    }

    ngOnInit(): void {
        this.location.subscribe((next) => {
            this.modelInDataLayer.dutch = next.descriptionDutch;
            this.modelInDataLayer.english = next.descriptionEnglish;

            this.model.dutch = next.descriptionDutch;
            this.model.english = next.descriptionEnglish;
        });
    }

    updateButtonClick(): void {
        // show "loading" alert
        this.showUpdateSuccess = null;

        let loc: Location;

        this.location.pipe(
            switchMap(
                location => {
                    // prepare location to update
                    location.descriptionDutch = this.model.dutch;
                    location.descriptionEnglish = this.model.english;
                    loc = location;
                    return this.locationService
                        .updateLocation(location.locationId, location);
                }
            ),
            take(1) // unsubscribe from this.location
        ).subscribe(
            () => {
                this.showUpdateSuccess = true;
                this.modelInDataLayer.dutch = this.model.dutch;
                this.modelInDataLayer.english = this.model.english;
                setTimeout(
                    () => (this.showUpdateSuccess = undefined),
                    msToShowFeedback
                );
            },
            () => {
                this.showUpdateSuccess = false;
                setTimeout(
                    () => (this.showUpdateSuccess = undefined),
                    msToShowFeedback
                );
            }
        );
    }

    cancelButtonClick(): void {
        this.showUpdateSuccess = undefined;
        this.model.dutch = this.modelInDataLayer.dutch;
        this.model.english = this.modelInDataLayer.english;
    }

    isModelUpdatable(): boolean {
        return (
            this.modelInDataLayer.dutch !== this.model.dutch ||
            this.modelInDataLayer.english !== this.model.english
        );
    }
}
