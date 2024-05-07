import {Component, OnInit} from '@angular/core';
import {FaqService} from '@/services/api/faq/faq.service';
import {FaqItem} from '@/model/FaqItem';
import {Observable} from 'rxjs';
import {ActivatedRoute, ParamMap} from '@angular/router';
import {switchMap} from 'rxjs/operators';
import {TranslateService} from '@ngx-translate/core';

@Component({
    selector: 'app-faq-item',
    templateUrl: './faq-item.component.html',
    styleUrls: ['./faq-item.component.scss']
})
export class FaqItemComponent implements OnInit {

    /* Observables */
    protected $item: Observable<FaqItem>;

    /* State */
    protected locale: string;

    constructor(
        private translateService: TranslateService,
        private route: ActivatedRoute,
        private faqService: FaqService
    ) {
        this.locale = this.translateService.currentLang;

        this.translateService.onLangChange.subscribe(() => {
            this.locale = translateService.currentLang;
        });
    }

    ngOnInit(): void {
        this.$item = this.route.paramMap.pipe(
            switchMap((params: ParamMap) =>
                this.faqService.getItem(params.get('id'))
            )
        );
    }
}
