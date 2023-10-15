import { Pipe, PipeTransform } from '@angular/core';
import { LangChangeEvent, TranslateService } from '@ngx-translate/core';
import { merge, Observable, of } from 'rxjs';
import { ActionLogEntry } from '../../model/ActionLogEntry';
import { flatMap, map, mergeMap } from 'rxjs/operators';

@Pipe({name: 'formatAction'})
export class FormatActionPipe implements PipeTransform {


    constructor(private translate: TranslateService) {}

    transform(action: ActionLogEntry): Observable<string> {
        return merge(
            of({lang: this.translate.currentLang} as LangChangeEvent),
            this.translate.onLangChange
        ).pipe(mergeMap(() => this.actionToDescription(action)));
    }


    actionToDescription(action: ActionLogEntry): Observable<string> {
        const translateSource = action.domainId     ?
        'management.actionlog.descriptionFormatId'  :
        'management.actionlog.descriptionFormatNoId';

        let actionDescription : Observable<string> = this.translate.get(translateSource);
        actionDescription = actionDescription.pipe(
            flatMap((description: string) => {
                const translationObs : Observable<string> = this.translate.get('management.actionlog.action.' + action.type);
                return translationObs.pipe(
                    map((translation : string) => {
                        return description.replace('%action', translation);
                    })
                );
            }),
            flatMap((description: string) => {
                const translationObs : Observable<string> = this.translate.get('management.actionlog.domain.' + action.domain);
                return translationObs.pipe(
                    map((translation : string) => {
                        return description.replace('%subject', translation);
                    })
                );
            }),
            map((description : string) => {
                if (!action.domainId) {
                    return description;
                }
                return description.replace('%id', "" + action.domainId);
            })
        );
        return actionDescription;
    }



}
