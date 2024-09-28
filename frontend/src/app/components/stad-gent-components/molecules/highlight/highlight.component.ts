import { Component, Input, OnInit } from '@angular/core';

@Component({
    selector: 'app-highlight',
    templateUrl: './highlight.component.html',
    styleUrls: ['./highlight.component.scss']
})
export class HighlightComponent implements OnInit {

    /* Component input attributes */
    @Input() public title: string;
    @Input() public content: string;

    ngOnInit(): void {
        
    }

}
