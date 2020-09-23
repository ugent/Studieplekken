import {AfterViewInit, Component, Input, OnInit} from '@angular/core';
import {Location} from '../../shared/model/Location';
import {vars} from '../../../environments/environment';
import {LocationService} from '../../services/api/locations/location.service';
import {TranslateService} from '@ngx-translate/core';
import {LocationTag} from '../../shared/model/LocationTag';
import {TagsService} from '../../services/api/tags/tags.service';

@Component({
  selector: 'app-dashboard-item',
  templateUrl: './dashboard-item.component.html',
  styleUrls: ['./dashboard-item.component.css']
})
export class DashboardItemComponent implements OnInit, AfterViewInit {
  @Input() location: Location;

  occupation: number;

  altImageUrl = vars.defaultLocationImage;

  assignedTags: LocationTag[];

  currentLang: string;
  tagsInCurrentLang: string; // e.g. 'tag1, tag2, tag3', set by auxiliary setupTagsInCurrentLang()

  constructor(private locationService: LocationService,
              private translate: TranslateService) { }

  ngOnInit(): void {
    this.locationService.getNumberOfReservations(this.location).subscribe(next => {
      this.occupation = Math.round(100 * next / this.location.numberOfSeats);
    });

    this.currentLang = this.translate.currentLang;
    this.translate.onLangChange.subscribe(
      () => {
        this.currentLang = this.translate.currentLang;
        this.setupTagsInCurrentLang();
      }
    );

    this.assignedTags = this.location.assignedTags;
    this.setupTagsInCurrentLang();
  }

  ngAfterViewInit(): void {
  }

  handleImageError(): void {
    this.location.imageUrl = vars.defaultLocationImage;
  }

  setupTagsInCurrentLang(): void {
    if (this.assignedTags) {
      this.tagsInCurrentLang = '';

      this.assignedTags.forEach(tag => {
        if (this.currentLang === 'nl') {
          this.tagsInCurrentLang += tag.dutch + ', ';
        } else {
          this.tagsInCurrentLang += tag.english + ', ';
        }
      });

      this.tagsInCurrentLang = this.tagsInCurrentLang.substr(0, this.tagsInCurrentLang.length - 2);
    }
  }
}
