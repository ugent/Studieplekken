import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { Location } from '../../../model/Location';
import { LocationService } from '../../api/locations/location.service';

/**
 * This class is a service used by the LocationDetailsManagementComponent,
 * and all the subcomponents it uses to keep track of the location to be
 * managed.
 *
 * This central point of truth is useful when the location changes.
 * E.g. the amount of lockers are changed in the DetailsFormComponent,
 *   instead of having to refresh the LocationDetailsManagementComponent
 *   to see the updated result in LockersTableComponent, this service
 *   will make sure that (if the LockersTableComponent is subscribed to
 *   'locationObs') the LockersTableComponent will notice that the amount
 *   of lockers have been updated by the user
 *
 * Note: you'll probably won't find much explicit subscriptions to the
 *   'locationObs' observable. The reason is that the HTML pages are
 *   implicitly subscribed to the observable through a construct like:
 *     <div *ngIf="locationObs | async as location">...</div>
 */
@Injectable({
  providedIn: 'root',
})
export class LocationDetailsService {
  /*
   * 'locationSubject' is the BehaviorSubject that keeps track of the location
   * that is now viewed in details by the user
   */
  private locationSubject: BehaviorSubject<Location> = new BehaviorSubject<Location>(
    undefined
  );
  /*
   * 'locationObs' is the observable that all subcomponents will listen to to get
   * the information to view
   */
  public locationObs = this.locationSubject.asObservable();

  constructor(private locationService: LocationService) {}

  loadLocation(locationId: number): void {
    // Make sure that the 'loading' content is shown
    this.locationSubject.next(undefined);
    // Now actually load the location
    this.locationService.getLocation(locationId, true).subscribe(
      (next) => {
        this.locationSubject.next(next);
      },
      (error) => {
        this.locationSubject.error(error);
      }
    );
  }

  get location(): Location {
    return this.locationSubject.value;
  }
}
