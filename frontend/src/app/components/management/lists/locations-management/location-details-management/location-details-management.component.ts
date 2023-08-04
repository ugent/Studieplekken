import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {Observable, ReplaySubject, Subject, Subscription} from 'rxjs';
import {Location} from '../../../../../model/Location';
import {LocationService} from '../../../../../extensions/services/api/locations/location.service';

@Component({
    selector: 'app-location-details-management',
    templateUrl: './location-details-management.component.html',
    styleUrls: ['./location-details-management.component.scss'],
})
export class LocationDetailsManagementComponent implements OnInit, OnDestroy {

    protected locationSub: Subject<Location>;
    protected subscription: Subscription;

    constructor(
        private locationService: LocationService,
        private route: ActivatedRoute,
        private router: Router
    ) {
        this.locationSub = new ReplaySubject();
        this.subscription = new Subscription();
    }

    ngOnInit(): void {
        const locationId = Number(
            this.route.snapshot.paramMap.get('locationId')
        );

        this.subscription.add(
            this.locationService.getLocation(locationId).subscribe(location => {
                if (!location) {
                    return this.router.navigate(['/management/locations']);
                }

                this.locationSub.next(location);
            })
        );
    }

    ngOnDestroy(): void {
        this.subscription.unsubscribe();
    }
}
