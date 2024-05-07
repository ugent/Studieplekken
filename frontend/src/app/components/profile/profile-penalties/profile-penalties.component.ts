import {Component, Input} from '@angular/core';
import {LangChangeEvent, TranslateService} from '@ngx-translate/core';
import {merge, Observable, of} from 'rxjs';
import {map} from 'rxjs/operators';
import {PenaltyList} from 'src/app/services/api/penalties/penalty.service';
import {AuthenticationService} from '@/services/authentication/authentication.service';
import {User} from '@/model/User';

@Component({
    selector: 'app-profile-penalties',
    templateUrl: './profile-penalties.component.html',
    styleUrls: ['./profile-penalties.component.scss'],
})
export class ProfilePenaltiesComponent {

    @Input() user: User;

    protected penalties: Observable<PenaltyList>;

    constructor(
        authenticationService: AuthenticationService,
        private translationService: TranslateService
    ) {
        this.penalties = authenticationService.penaltyObservable;
    }

    currentLanguage(): Observable<string> {
        return merge<LangChangeEvent, LangChangeEvent>(
            of<LangChangeEvent>({
                lang: this.translationService.currentLang,
            } as LangChangeEvent),
            this.translationService.onLangChange
        ).pipe(map((s) => s.lang));
    }
}
