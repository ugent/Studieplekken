import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {api} from '../endpoints';
import {Location, LocationConstructor} from '../../../../model/Location';
import {LocationTag} from '../../../../model/LocationTag';
import {map} from 'rxjs/internal/operators/map';
import {Cache} from '../../../cache/Cache';
import {Moment} from 'moment';
import {Pair} from '../../../../model/helpers/Pair';
import {User} from '../../../../model/User';

type locationOverview = { [locationName: string]: string[] };

@Injectable({
    providedIn: 'root',
})
export class LocationService {
    constructor(private http: HttpClient) {
    }

    locationCache: Cache<number, Location> = new Cache<number, Location>(
        this.http,
        (arg: Location) => arg.locationId,
        (json: never) => LocationConstructor.newFromObj(json)
    );

    /***********************************************************
     *   API calls for CRUD operations with public.LOCATIONS   *
     ***********************************************************/

    getLocations(): Observable<Location[]> {
        return this.locationCache.getAllValues(api.visible_locations);
    }

    getAllLocations(cached: boolean = true): Observable<Location[]> {
        return this.locationCache.getAllValues(
            api.all_locations, !cached
        );
    }

    /**
     * Special treatment, because otherwise this messes with the cache
     */
    getUnapprovedLocations(): Observable<Location[]> {
        return this.http.get<Location[]>(api.locationsUnapproved);
    }

    getLocation(
        locationId: number,
        invalidateCache: boolean = false
    ): Observable<Location> {
        const url = api.location.replace('{locationId}', String(locationId));
        return this.locationCache.getValue(locationId, url, invalidateCache);
    }

    getAllLocationNextReservableFroms(): Observable<{
        locationName: string,
        nextReservableFrom: Moment
    }[]> {
        return this.http.get<{
            locationName: string,
            nextReservableFrom: Moment
        }[]>(api.allReservableFroms);
    }

    getVolunteers(locationId: number): Observable<User[]> {
        const url = api.locationVolunteers.replace(
            '{locationId}',
            String(locationId)
        );
        return this.http.get<User[]>(url);
    }

    addVolunteer(locationId: number, userId: string): Observable<void> {
        const url = api.addLocationVolunteer
            .replace('{locationId}', String(locationId))
            .replace('{userId}', String(userId));
        return this.http.post<void>(url, {});
    }

    deleteVolunteer(locationId: number, userId: string): Observable<void> {
        const url = api.addLocationVolunteer
            .replace('{locationId}', String(locationId))
            .replace('{userId}', String(userId));
        return this.http.delete<void>(url, {});
    }

    addLocation(location: Location): Observable<void> {
        return this.http.post<void>(api.addLocation, location);
    }

    updateLocation(locationId: number, location: Location): Observable<void> {
        return this.http.put<void>(
            api.updateLocation.replace('{locationId}', String(locationId)),
            location
        );
    }

    approveLocation(location: Location, approval: boolean): Observable<void> {
        return this.http.put<void>(
            api.approveLocation.replace('{locationId}', String(location.locationId)),
            {location, approval}
        );
    }

    deleteLocation(locationId: number): Observable<void> {
        return this.http.delete<void>(
            api.deleteLocation.replace('{locationId}', String(locationId))
        );
    }

    /***************************************************************
     *   API calls for CRUD operations with public.LOCATION_TAGS   *
     ***************************************************************/

    setupTagsForLocation(
        locationId: number,
        tags: LocationTag[]
    ): Observable<void> {
        return this.http.put<void>(
            api.setupTagsForLocation.replace('{locationId}', String(locationId)),
            tags.map((v) => v.tagId)
        );
    }

    /**************************************************
     *   Miscellaneous queries concerning locations   *
     ***************************************************/

    getOpeningOverviewOfWeek(
        year: number,
        weekNr: number
    ): Observable<locationOverview> {
        return this.http.get<locationOverview>(
            api.openingHoursOverview
                .replace('{year}', String(year))
                .replace('{weekNr}', String(weekNr))
        );
    }

    subscribeToLocation(locationId: number): Observable<void> {
        return this.http.post<void>(
            api.userLocationSubscriptions.replace('{locationId}', String(locationId)),
            {}
        );
    }

    unsubscribeFromLocation(locationId: number): Observable<void> {
        return this.http.delete<void>(
            api.userLocationSubscriptions.replace('{locationId}', String(locationId))
        );
    }
}
