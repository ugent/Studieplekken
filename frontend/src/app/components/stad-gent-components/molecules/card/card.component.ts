import { Component, Input, AfterContentInit, ElementRef, ViewChild } from '@angular/core';

@Component({
    selector: 'app-card',
    templateUrl: './card.component.html',
    styleUrls: ['./card.component.scss']
})
export class CardComponent implements AfterContentInit {
    @Input() 
    protected title: string = 'Default Title';

    @ViewChild('actionsContainer', { static: false })
    protected actionsContainer!: ElementRef<HTMLDivElement>;
    protected hasActions: boolean = false;

    public ngAfterContentInit() {
        this.hasActions = !!this.actionsContainer.nativeElement.children.length;
    }
}
