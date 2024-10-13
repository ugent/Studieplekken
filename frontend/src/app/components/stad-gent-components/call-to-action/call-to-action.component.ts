import { Component, Input, OnInit, TemplateRef } from '@angular/core';

@Component({
    selector: 'app-call-to-action',
    templateUrl: './call-to-action.component.html',
    styleUrls: ['./call-to-action.component.scss']
})
export class CallToActionComponent implements OnInit {

    @Input() title!: string;

    @Input() description: string;
    @Input() descriptionTemplate: TemplateRef<any>;

    @Input() footer: string;
    @Input() footerTemplate: TemplateRef<any>;

    constructor() { }

    ngOnInit(): void {
    }

}
