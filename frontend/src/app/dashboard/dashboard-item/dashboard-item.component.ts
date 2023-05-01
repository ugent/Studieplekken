import {Component, Input, OnInit} from '@angular/core';
import {DatePipe} from '@angular/common';
import {Location} from '../../shared/model/Location';
import {LocationService} from '../../services/api/locations/location.service';
import {TranslateService} from '@ngx-translate/core';
import {LocationTag} from '../../shared/model/LocationTag';
import {defaultLocationImage} from '../../app.constants';
import {Moment} from 'moment';

@Component({
    selector: 'app-dashboard-item',
    templateUrl: './dashboard-item.component.html',
    styleUrls: ['./dashboard-item.component.scss', '../location.scss'],
    providers: [DatePipe],
})
export class DashboardItemComponent implements OnInit {
    @Input() location: Location;
    @Input() nextReservableFrom: Moment;

    imageUrlErrorOccurred = false;
    altImageUrl = defaultLocationImage;

    assignedTags: LocationTag[];

    currentLang: string;
    tagsInCurrentLang: string[]; // e.g. 'tag1, tag2, tag3', set by auxiliary setupTagsInCurrentLang()

    /* Subscriptions */
    constructor(
        private locationService: LocationService,
        private translate: TranslateService
    ) {
    }

    ngOnInit(): void {
        this.currentLang = this.translate.currentLang;
        this.translate.onLangChange.subscribe(() => {
            this.currentLang = this.translate.currentLang;
            this.setupTagsInCurrentLang();
        });

        this.assignedTags = this.location.assignedTags;
        this.setupTagsInCurrentLang();
    }

    locationStatusColorClass(): string {
        return this.location.currentTimeslot?.isCurrent()
            ? 'open'
            : 'closed';
    }

    handleImageError(): void {
        this.imageUrlErrorOccurred = true;
    }

    setupTagsInCurrentLang(): void {
        this.tagsInCurrentLang = [];
        if (this.assignedTags && this.assignedTags.length > 0) {
            this.assignedTags.forEach((tag) => {
                if (this.currentLang === 'nl') {
                    this.tagsInCurrentLang.push(tag.dutch);
                } else {
                    this.tagsInCurrentLang.push(tag.english);
                }
            });
        }
    }
}
