import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Observable} from 'rxjs';
import {api} from '../endpoints';
import {LocationTag} from '../../../shared/model/LocationTag';
import {Cache} from '../../../shared/cache/Cache';

@Injectable({
  providedIn: 'root'
})
export class TagsService {

  constructor(private http: HttpClient) {
  }

  tagCache: Cache<number, LocationTag> = new Cache<number, LocationTag>(this.http, (arg: LocationTag) => arg.tagId);

  /*****************************************************
   *   API calls for CRUD operations with public.TAGS  *
   *****************************************************/

  getAllTags(): Observable<LocationTag[]> {
    console.log('getallTags');
    return this.tagCache.getAllValues(api.tags);
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
}
