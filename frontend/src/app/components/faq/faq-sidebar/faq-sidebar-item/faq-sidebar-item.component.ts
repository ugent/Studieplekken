import {Component, Input, OnInit} from '@angular/core';
import {FaqCategory} from '@/model/FaqCategory';

@Component({
    selector: 'app-faq-sidebar-item',
    templateUrl: './faq-sidebar-item.component.html',
    styleUrls: ['./faq-sidebar-item.component.scss']
})
export class FaqSidebarItemComponent implements OnInit {

    @Input() index: number;
    @Input() locale: string;
    @Input() category: FaqCategory;

    protected toggled: boolean = false;

    constructor(
    ) {
    }

    /**
     * Initialize the component
     */
    ngOnInit(): void {
    }
}
