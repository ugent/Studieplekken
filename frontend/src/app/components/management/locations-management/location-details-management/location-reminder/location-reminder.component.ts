import { Component, Input, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import * as ClassicEditor from '@ckeditor/ckeditor5-build-classic';
import { Location } from '@/model/Location';
import { switchMap, take } from 'rxjs/operators';
import { LocationService } from 'src/app/services/api/locations/location.service';
import { msToShowFeedback } from 'src/app/app.constants';

@Component({
  selector: 'app-location-reminder',
  templateUrl: './location-reminder.component.html',
  styleUrls: ['./location-reminder.component.scss'],
})
export class LocationReminderComponent implements OnInit {

    @Input() location: Observable<Location>;

    editor: unknown = ClassicEditor;

    modelOriginal = {
        dutch: '',
        english: ''
    };

    model = {
        dutch: '',
        english: ''
    };

    config = {
        toolbar: [
            'bold',
            'italic',
            '|',
            'link',
            '|',
            'undo',
            'redo',
          ]
    };

    showUpdateSuccess: boolean = undefined;

    constructor(
        private locationService: LocationService
    ) {

    }

    ngOnInit(): void {
        this.location.subscribe((next) => {
            this.model.dutch = next.reminderDutch? next.reminderDutch : '';
            this.model.english = next.reminderEnglish? next.reminderEnglish : '';
            this.modelOriginal.dutch = next.reminderDutch? next.reminderDutch: '';
            this.modelOriginal.english = next.reminderEnglish? next.reminderEnglish: '';
        });
    }

    updateButtonClick(): void {
        this.showUpdateSuccess = null;
        let loc: Location; // TODO: Why this.
        this.location.pipe(
            switchMap(
                location => {
                    location.reminderDutch = this.model.dutch;
                    location.reminderEnglish = this.model.english;
                    loc = location;
                    return this.locationService
                        .updateLocation(location.locationId, location);
                }
            ),
            take(1) // unsubscribe from this.location
        ).subscribe(
            () => {
                this.showUpdateSuccess = true;
                this.modelOriginal.dutch = this.model.dutch;
                this.modelOriginal.english = this.model.english;
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
        this.model.dutch = this.modelOriginal.dutch;
        this.model.english = this.modelOriginal.english;
    }

    isModelUpdatable(): boolean {
        return (
            this.modelOriginal.dutch !== this.model.dutch ||
            this.modelOriginal.english !== this.model.english
        );
    }

    protected readonly undefined = undefined;
}
