import {AfterViewInit, Component, Input, OnInit} from '@angular/core';
import {Location} from '../../shared/model/Location';
import {LocationService} from '../../services/api/locations/location.service';
import {TranslateService} from '@ngx-translate/core';
import {LocationTag} from '../../shared/model/LocationTag';
import {ApplicationTypeFunctionalityService} from '../../services/functionality/application-type/application-type-functionality.service';
import {defaultLocationImage} from '../../app.constants';

@Component({
  selector: 'app-dashboard-item',
  templateUrl: './dashboard-item.component.html',
  styleUrls: ['./dashboard-item.component.css']
})
export class DashboardItemComponent implements OnInit, AfterViewInit {
  @Input() location: Location;

  occupation: number = 0;

  altImageUrl = defaultLocationImage;

  assignedTags: LocationTag[];

  currentLang: string;
  tagsInCurrentLang: string; // e.g. 'tag1, tag2, tag3', set by auxiliary setupTagsInCurrentLang()

  showProgressBar: boolean;

  constructor(private locationService: LocationService,
              private translate: TranslateService,
              private functionalityService: ApplicationTypeFunctionalityService) { }

  ngOnInit(): void {
    this.currentLang = this.translate.currentLang;
    this.translate.onLangChange.subscribe(
      () => {
        this.currentLang = this.translate.currentLang;
        this.setupTagsInCurrentLang();
      }
    );

    this.assignedTags = this.location.assignedTags;
    this.setupTagsInCurrentLang();

    this.showProgressBar = this.functionalityService.showReservationsFunctionality();
  }

  ngAfterViewInit(): void {
  }

  handleImageError(): void {
    this.location.imageUrl = defaultLocationImage;
  }

  setupTagsInCurrentLang(): void {
    if (this.assignedTags && this.assignedTags.length > 0) {
      this.tagsInCurrentLang = '';

      this.assignedTags.forEach(tag => {
        if (this.currentLang === 'nl') {
          this.tagsInCurrentLang += tag.dutch + ', ';
        } else {
          this.tagsInCurrentLang += tag.english + ', ';
        }
      });

      this.tagsInCurrentLang = this.tagsInCurrentLang.substr(0, this.tagsInCurrentLang.length - 2);
    } else {
      this.translate.get('general.notAvailableAbbreviation').subscribe(
        next => {
          this.tagsInCurrentLang = next;
        }, () => {
          this.tagsInCurrentLang = 'n/a';
        }
      );
    }
  }
}
