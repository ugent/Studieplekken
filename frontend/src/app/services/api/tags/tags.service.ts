import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {api} from '../endpoints';
import {LocationTag} from '@/model/LocationTag';
import {Cache} from '../../cache/Cache';
import {TranslateService} from '@ngx-translate/core';

@Injectable({
    providedIn: 'root',
})
export class TagsService {
    private tagCache: Cache<number, LocationTag>;

    constructor(
        private http: HttpClient,
        private translateService: TranslateService
    ) {
        this.tagCache = new Cache<number, LocationTag>(this.http,
            (arg: LocationTag) => arg.tagId
        );
    }

    /*****************************************************
     *   API calls for CRUD operations with public.TAGS  *
     *****************************************************/

    getAllTags(): Observable<LocationTag[]> {
        return this.tagCache.getAllValues(api.tags)
            .pipe(map(tags => {
                return tags.sort((a, b) => {
                    const lang = this.translateService.currentLang;
                    if (lang === 'en') {
                        return a.english.localeCompare(b.english);
                    } else if (lang === 'nl') {
                        return a.dutch.localeCompare(b.dutch);
                    } else {
                        // Fallback in case of unexpected lang / lang not set.
                        return a.tagId - b.tagId;
                    }
                });
            }));
    }

    /**
     * Adding a tag. Note that locationTag.tagId will be ignored. The return
     * value will have set the correct tagId for the added Tag.
     */
    addTag(locationTag: LocationTag): Observable<LocationTag> {
        return this.http.post<LocationTag>(api.addTag, locationTag);
    }

    updateTag(locationTag: LocationTag): Observable<void> {
        return this.http.put<void>(api.updateTag, locationTag);
    }

    deleteTag(locationTag: LocationTag): Observable<void> {
        return this.http.delete<void>(
            api.deleteTag.replace('{tagId}', String(locationTag.tagId))
        );
    }
}
