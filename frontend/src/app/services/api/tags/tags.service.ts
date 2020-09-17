import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Observable} from 'rxjs';
import {api} from '../../../../environments/environment';
import {LocationTag} from '../../../shared/model/LocationTag';

@Injectable({
  providedIn: 'root'
})
export class TagsService {

  constructor(private http: HttpClient) { }

  /*****************************************************
   *   API calls for CRUD operations with public.TAGS  *
   *****************************************************/

  getAllTags(): Observable<LocationTag[]> {
    return this.http.get<LocationTag[]>(api.tags);
  }

  /**
   * Adding a tag. Note that locationTag.tagId will be ignored. The return
   * value will have set the correct tagId for the added Tag.
   */
  addTag(locationTag: LocationTag): Observable<LocationTag> {
    return this.http.post<LocationTag>(api.addTag, locationTag);
  }

  updateTag(locationTag: LocationTag): Observable<any> {
    return this.http.put(api.updateTag, locationTag);
  }

  deleteTag(locationTag: LocationTag): Observable<any> {
    return this.http.delete(api.deleteTag.replace('{tagId}', String(locationTag.tagId)));
  }

  /**************************************************************
   *   API calls for CRUD operations with public.LOCATION_TAGS  *
   **************************************************************/

  assignTagsToLocation(locationName: string, tags: LocationTag[]): Observable<any> {
    return this.http.put(api.assignTagsToLocation.replace('{locationName}', locationName), tags);
  }

  reconfigureAllowedTagsOfLocation(locationName: string, tags: LocationTag[]): Observable<any> {
    return this.http.put(api.reconfigureAllowedTags.replace('{locationName}', locationName), tags);
  }
}
