import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Building } from 'src/app/shared/model/Building';
import { api } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class BuildingService {

  constructor(private http: HttpClient) { }

  // *************************************
  // *   CRUD operations for AUTHORITY   *
  // *************************************/

  getAllBuildings(): Observable<Building[]> {
    return this.http.get<Building[]>(api.buildings);
  }

  getBuilding(buildingId: number): Observable<Building> {
    return this.http.get<Building>(api.building.replace('{buildingId}', String(buildingId)));
  }

  addBuilding(building: Building): Observable<any> {
    return this.http.post(api.addBuilding, building);
  }

  updateBuilding(buildingId: number, building: Building): Observable<any> {
    return this.http.put(api.updateBuilding.replace('{authorityId}', String(buildingId)), building);
  }

  deleteBuilding(buildingId: number): Observable<any> {
    return this.http.delete(api.deleteAuthority.replace('{buildingId}', String(buildingId)));
  }
}
