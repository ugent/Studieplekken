import { Component, Input, AfterContentInit, ElementRef, ViewChild, ContentChild, ContentChildren, QueryList, TemplateRef } from '@angular/core';

@Component({
    selector: 'app-card',
    templateUrl: './card.component.html',
    styleUrls: ['./card.component.scss']
})
export class CardComponent {
    @Input() 
    protected title: string = '';  

    @ContentChild('cardActions', { static: false }) 
    protected cardActionsContent?: TemplateRef<any>;
}
