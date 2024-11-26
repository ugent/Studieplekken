import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {api} from '../endpoints';
import {Moment} from 'moment';
import {LocationConstructor} from '@/model/Location';
import {Cache} from '../../cache/Cache';
import {Location} from '@/model/Location';
import {LocationTag} from '@/model/LocationTag';
import {User} from '@/model/User';

type locationOverview = { [locationName: string]: string[] };

@Injectable({
    providedIn: 'root',
})
export class LocationService {
    private locationCache: Cache<number, Location>;

    constructor(private http: HttpClient) {
       this.locationCache = new Cache(this.http,
            (location: Location) => location.locationId,
            (json: never) => LocationConstructor.newFromObj(json)
        );
    }

    /**
     * Retrieves a list of visible locations.
     *
     * @returns {Observable<Location[]>} An observable that emits an array of Location objects.
     */
    public getLocations(): Observable<Location[]> {
        return this.locationCache.getAllValues(api.visible_locations);
    }

    /**
     * Retrieves all locations.
     *
     * @param cached - A boolean indicating whether to use cached data. Defaults to true.
     * @returns An Observable that emits an array of Location objects.
     */
    public getAllLocations(cached: boolean = true): Observable<Location[]> {
        return this.locationCache.getAllValues(
            api.all_locations, !cached
        );
    }

    /**
     * Retrieves the location details for a given location ID.
     * 
     * @param locationId - The unique identifier of the location.
     * @param invalidateCache - Optional parameter to force cache invalidation and fetch fresh data. Defaults to false.
     * @returns An Observable that emits the location details.
     */
    public getLocation(locationId: number, invalidateCache: boolean = false): Observable<Location> {
        const url = api.location.replace('{locationId}', String(locationId));
        return this.locationCache.getValue(locationId, url, invalidateCache);
    }

    /**
     * Fetches the next reservable times for all locations.
     *
     * @returns An Observable that emits an array of objects, each containing:
     * - `locationName`: The name of the location.
     * - `nextReservableFrom`: The next available reservation time as a Moment object.
     */
    public getAllLocationNextReservableFroms(): Observable<{locationName: string, nextReservableFrom: Moment}[]> {
        return this.http.get<{locationName: string, nextReservableFrom: Moment}[]>(
            api.allReservableFroms
        );
    }

    /**
     * Retrieves a list of volunteers for a given location.
     *
     * @param {number} locationId - The ID of the location to get volunteers for.
     * @returns {Observable<User[]>} An observable that emits an array of User objects representing the volunteers.
     */
    public getVolunteers(locationId: number): Observable<User[]> {
        const url = api.locationVolunteers.replace('{locationId}',String(locationId));
        return this.http.get<User[]>(url);
    }

    /**
     * Adds a volunteer to a specified location.
     *
     * @param locationId - The ID of the location to which the volunteer will be added.
     * @param userId - The ID of the user who will be added as a volunteer.
     * @returns An Observable that completes when the volunteer has been successfully added.
     */
    public addVolunteer(locationId: number, userId: string): Observable<void> {
        const url = api.addLocationVolunteer
            .replace('{locationId}', String(locationId))
            .replace('{userId}', String(userId));
        return this.http.post<void>(url, {});
    }

    /**
     * Deletes a volunteer from a specific location.
     *
     * @param locationId - The ID of the location from which the volunteer will be removed.
     * @param userId - The ID of the user (volunteer) to be removed.
     * @returns An Observable that completes when the volunteer is successfully deleted.
     */
    public deleteVolunteer(locationId: number, userId: string): Observable<void> {
        const url = api.addLocationVolunteer
            .replace('{locationId}', String(locationId))
            .replace('{userId}', String(userId));
        return this.http.delete<void>(url, {});
    }

    /**
     * Adds a new location by sending a POST request to the API.
     *
     * @param location - The location object to be added.
     * @returns An Observable that completes when the location is successfully added.
     */
    public addLocation(location: Location): Observable<void> {
        return this.http.post<void>(api.addLocation, location);
    }

    public updateLocation(locationId: number, location: Location): Observable<void> {
        return this.http.put<void>(
            api.updateLocation.replace('{locationId}', String(locationId)),
            location
        );
    }

    public approveLocation(location: Location, approval: boolean): Observable<void> {
        return this.http.put<void>(
            api.approveLocation.replace('{locationId}', String(location.locationId)),
            {location, approval}
        );
    }

    public deleteLocation(locationId: number): Observable<void> {
        return this.http.delete<void>(
            api.deleteLocation.replace('{locationId}', String(locationId))
        );
    }

    public setupTagsForLocation(locationId: number, tags: LocationTag[]): Observable<void> {
        return this.http.put<void>(
            api.setupTagsForLocation.replace('{locationId}', String(locationId)),
            tags.map((v) => v.tagId)
        );
    }

    public getOpeningOverviewOfWeek(year: number, weekNr: number): Observable<locationOverview> {
        return this.http.get<locationOverview>(
            api.openingHoursOverview
                .replace('{year}', String(year))
                .replace('{weekNr}', String(weekNr))
        );
    }

    public subscribeToLocation(locationId: number): Observable<void> {
        return this.http.post<void>(
            api.userLocationSubscriptions.replace('{locationId}', String(locationId)),
            {}
        );
    }

    public unsubscribeFromLocation(locationId: number): Observable<void> {
        return this.http.delete<void>(
            api.userLocationSubscriptions.replace('{locationId}', String(locationId))
        );
    }
}
