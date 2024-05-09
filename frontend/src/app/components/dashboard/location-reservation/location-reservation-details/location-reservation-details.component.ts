import {Component, Input, OnInit} from '@angular/core';
import {defaultTeaserImages} from '@/app.constants';
import {User} from '@/model/User';
import {Location} from '@/model/Location';
import {Observable} from 'rxjs';
import {LocationService} from '@/services/api/locations/location.service';

@Component({
    selector: 'app-location-reservation-details',
    templateUrl: './location-reservation-details.component.html',
    styleUrls: ['./location-reservation-details.component.scss']
})
export class LocationReservationDetailsComponent implements OnInit {

    @Input() location: Location;
    @Input() user: User;
    @Input() language: string;

    protected readonly defaultTeaserImage = defaultTeaserImages[
        Math.floor(Math.random() * defaultTeaserImages.length)
    ];

    constructor(
        private locationService: LocationService
    ) {
    }

    ngOnInit(): void {
    }

    toggleSubscription(): void {
        let request: Observable<void>;

        this.location.subscribed = !this.location.subscribed;

        if (!this.location.subscribed) {
            request = this.locationService.unsubscribeFromLocation(this.location.locationId);
        } else {
            request = this.locationService.subscribeToLocation(this.location.locationId);
        }

        request.subscribe();
    }
}
