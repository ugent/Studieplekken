import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {Building} from 'src/app/model/Building';
import {Cache} from '../../cache/Cache';
import {api} from '../endpoints';

@Injectable({
    providedIn: 'root',
})
export class BuildingService {
    private buildingCache: Cache<number, Building>;

    constructor(private http: HttpClient) {
        this.buildingCache = new Cache<number, Building>(this.http,
            (arg: Building) => arg.buildingId
        );
    }

    // *************************************
    // *   CRUD operations for AUTHORITY   *
    // *************************************/

    getAllBuildings(): Observable<Building[]> {
        return this.buildingCache.getAllValues(api.buildings, true);
    }

    getBuilding(buildingId: number): Observable<Building> {
        const url = api.building.replace('{buildingId}', String(buildingId));
        return this.buildingCache.getValue(buildingId, url);
    }

    addBuilding(building: Building): Observable<void> {
        return this.http.post<void>(api.addBuilding, building);
    }

    updateBuilding(buildingId: number, building: Building): Observable<void> {
        return this.http.put<void>(
            api.updateBuilding.replace('{buildingId}', String(buildingId)),
            building
        );
    }

    deleteBuilding(buildingId: number): Observable<void> {
        return this.http.delete<void>(
            api.deleteBuilding.replace('{buildingId}', String(buildingId))
        );
    }
}
