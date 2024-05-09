import {Injectable} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {BehaviorSubject, Observable, Subject} from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class LanguageService {

    private readonly languageSub$: Subject<string>;

    constructor(
        translationService: TranslateService
    ) {
        this.languageSub$ = new BehaviorSubject(
            translationService.currentLang
        );

        translationService.onLangChange.subscribe(() =>
            this.languageSub$.next(translationService.currentLang)
        );
    }

    getLanguageObs(): Observable<string> {
        return this.languageSub$;
    }
}
