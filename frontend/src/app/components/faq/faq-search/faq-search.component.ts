import {Component, OnInit} from '@angular/core';
import {BehaviorSubject, combineLatest, Observable} from 'rxjs';
import {FaqItem} from '@/model/FaqItem';
import {FaqService} from '@/services/api/faq/faq.service';
import {LangChangeEvent, TranslateService} from '@ngx-translate/core';
import {map, tap} from 'rxjs/operators';
import {ActivatedRoute, Router} from '@angular/router';

@Component({
    selector: 'app-faq-search',
    templateUrl: './faq-search.component.html',
    styleUrls: ['./faq-search.component.scss']
})
export class FaqSearchComponent implements OnInit {

    /* Observables */
    protected $pinned: Observable<FaqItem[]>;
    protected $items: Observable<FaqItem[]>;
    protected $searchItems: Observable<FaqItem[]>;

    /* Search form control */
    protected $searchSubject: BehaviorSubject<string>;

    /* State */
    protected locale: string;

    constructor(
        private router: Router,
        private activatedRoute: ActivatedRoute,
        private translateService: TranslateService,
        private faqService: FaqService
    ) {
        this.locale = this.translateService.currentLang;

        this.translateService.onLangChange.subscribe((event: LangChangeEvent) => {
            this.locale = event.lang;
        });

        this.$searchSubject = new BehaviorSubject<string>(
            this.activatedRoute.snapshot.queryParams.search || ''
        );
    }

    /**
     * Initialize the component
     */
    ngOnInit(): void {
        this.$items = this.faqService.getItems();

        this.$pinned = this.$items.pipe(
            map((items: FaqItem[]) =>
                items.filter(item => item.isPinned)
            )
        );

        this.$searchItems = combineLatest([
            this.$searchSubject,
            this.$items
        ]).pipe(
            tap(([search, _]) =>
                void this.router.navigate([], {
                    relativeTo: this.activatedRoute,
                    queryParams: {search},
                    replaceUrl: true
                })
            ),
            map(([search, items]) =>
                items.filter((item: FaqItem) => {
                    return search && (item.title.translations[this.locale].toLowerCase().includes(search.toLowerCase()) ||
                    item.content.translations[this.locale].toLowerCase().includes(search.toLowerCase()))
                })
            )
        );
    }
}
