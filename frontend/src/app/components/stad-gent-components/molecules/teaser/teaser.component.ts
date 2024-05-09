import {Component, Input, OnInit} from '@angular/core';

@Component({
    selector: 'app-teaser',
    templateUrl: './teaser.component.html',
    styleUrls: ['./teaser.component.scss']
})
export class TeaserComponent implements OnInit {

    /* Props */
    @Input() protected title: string;
    @Input() protected buttonText: string;
    @Input() protected buttonLink: string|string[];
    @Input() protected imageUrl: string;
    @Input() protected tags: string[] = [];

    constructor() {
    }

    ngOnInit(): void {
    }

}
