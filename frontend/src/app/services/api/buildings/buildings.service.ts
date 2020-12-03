import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {Building} from 'src/app/shared/model/Building';
import {Cache} from '../../../shared/cache/Cache';
import {api} from '../endpoints';

@Injectable({
  providedIn: 'root'
})
export class BuildingService {

  constructor(private http: HttpClient) {
  }

  buildingCache: Cache<number, Building> = new Cache<number, Building>(this.http, (arg: Building) => arg.buildingId);

  // *************************************
  // *   CRUD operations for AUTHORITY   *
  // *************************************/

  getAllBuildings(): Observable<Building[]> {
    console.log('getAllBuildings');
    return this.buildingCache.getAllValues(api.buildings);
  }

  getBuilding(buildingId: number): Observable<Building> {
    console.log('getBuilding');
    const url = api.building.replace('{buildingId}', String(buildingId));
    return this.buildingCache.getValue(buildingId, url);
  }

  addBuilding(building: Building): Observable<any> {
    return this.http.post(api.addBuilding, building);
  }

  updateBuilding(buildingId: number, building: Building): Observable<any> {
    return this.http.put(api.updateBuilding.replace('{buildingId}', String(buildingId)), building);
  }

  deleteBuilding(buildingId: number): Observable<any> {
    return this.http.delete(api.deleteBuilding.replace('{buildingId}', String(buildingId)));
  }
}
