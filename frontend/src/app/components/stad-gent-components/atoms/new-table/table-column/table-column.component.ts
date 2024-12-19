import { Component, ContentChild, Input, TemplateRef } from '@angular/core';

@Component({
    selector: 'app-table-column',
    templateUrl: './table-column.component.html',
    styleUrls: ['./table-column.component.scss']
})
export class TableColumnComponent {
    @Input() 
    public title: string = '';

    @ContentChild(TemplateRef) 
    public template!: TemplateRef<any>;
}
