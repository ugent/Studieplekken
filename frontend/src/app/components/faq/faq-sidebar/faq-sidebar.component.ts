import {Component, Input, OnInit} from '@angular/core';
import {FaqCategory} from '../../../model/FaqCategory';

@Component({
    selector: 'app-faq-sidebar',
    templateUrl: './faq-sidebar.component.html',
    styleUrls: ['./faq-sidebar.component.scss']
})
export class FaqSidebarComponent implements OnInit {

    @Input() locale: string;
    @Input() categories: FaqCategory[];

    constructor(
    ) {
    }

    /**
     * Initialize the component
     */
    public ngOnInit(): void {
    }
}
