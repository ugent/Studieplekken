import { contact } from '@/app.constants';
import { Component, Input, OnInit } from '@angular/core';

@Component({
    selector: 'app-contact',
    templateUrl: './contact.component.html',
    styleUrls: ['./contact.component.scss']
})
export class ContactComponent implements OnInit {
    /* Component input attributes */
    @Input() public title: string;
    @Input() public content: string;

    /* Component state */
    public contact: any;

    /**
     * Constructor.
     */
    constructor() {
        this.contact = contact;
    }

    ngOnInit(): void {
        
    }

}
