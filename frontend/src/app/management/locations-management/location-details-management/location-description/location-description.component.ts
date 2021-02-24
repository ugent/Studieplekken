import {Component, Input, OnInit} from '@angular/core';
import {Observable} from 'rxjs';
import {Location} from '../../../../shared/model/Location';
import * as ClassicEditor from '@ckeditor/ckeditor5-build-classic';
import {LocationDetailsService} from '../../../../services/single-point-of-truth/location-details/location-details.service';
import {LocationService} from '../../../../services/api/locations/location.service';
import {msToShowFeedback} from '../../../../app.constants';

@Component({
  selector: 'app-location-description',
  templateUrl: './location-description.component.html',
  styleUrls: ['./location-description.component.css']
})
export class LocationDescriptionComponent implements OnInit {
  @Input() location: Observable<Location>;

  editor = ClassicEditor;

  modelInDataLayer = {
    dutch: '',
    english: ''
  };

  model = {
    dutch: '',
    english: ''
  };

  config = {
    toolbar: [
      'heading', '|', 'bold', 'italic', '|', 'numberedList', 'bulletedList', '|', 'link', 'blockQuote',
      'insertTable', '|', 'undo', 'redo'
    ]
  };

  showUpdateSuccess: boolean = undefined;

  constructor(private locationDetailsService: LocationDetailsService,
              private locationService: LocationService) { }

  ngOnInit(): void {
    this.location.subscribe(next => {
      this.modelInDataLayer.dutch = next.descriptionDutch;
      this.modelInDataLayer.english = next.descriptionEnglish;

      this.model.dutch = next.descriptionDutch;
      this.model.english = next.descriptionEnglish;
    });
  }

  updateButtonClick(): void {
    // show "loading" alert
    this.showUpdateSuccess = null;

    // prepare location to update
    const location = this.locationDetailsService.location;
    location.descriptionDutch = this.model.dutch;
    location.descriptionEnglish = this.model.english;

    // update
    this.locationService.updateLocation(location.locationId, location).subscribe(
      () => {
        this.showUpdateSuccess = true;
        setTimeout(() => this.showUpdateSuccess = undefined, msToShowFeedback);
        // make sure to retrieve the updated the location
        this.locationDetailsService.loadLocation(location.locationId);
      }, () => {
        this.showUpdateSuccess = false;
        setTimeout(() => this.showUpdateSuccess = undefined, msToShowFeedback);
      }
    );
  }

  cancelButtonClick(): void {
    this.showUpdateSuccess = undefined;
    this.model.dutch = this.modelInDataLayer.dutch;
    this.model.english = this.modelInDataLayer.english;
  }

  isModelUpdatable(): boolean {
    return this.modelInDataLayer.dutch !== this.model.dutch ||
      this.modelInDataLayer.english !== this.model.english;
  }
}
