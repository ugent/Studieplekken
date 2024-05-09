import {
    AfterViewInit,
    Component,
    ElementRef,
    Input,
    OnChanges,
    QueryList,
    SimpleChanges,
    ViewChildren
} from '@angular/core';
import * as Accordion from "gent_styleguide/build/styleguide/js/accordion.functions.js"
import {Observable} from 'rxjs';

@Component({
    selector: 'app-accordeon',
    templateUrl: './accordeon.component.html',
    styleUrls: ['./accordeon.component.scss']
})
export class AccordeonComponent implements AfterViewInit, OnChanges {
    @ViewChildren("accordioncontainer", {read: ElementRef}) container: QueryList<ElementRef>;
    @Input() controller: Observable<boolean>
    private accordionController: any;

    @Input() classes: string[];

    constructor() {
    }

    ngAfterViewInit(): void {
        this.accordionController = new Accordion(this.container.first.nativeElement, {buttonSelector: ".accordion--button"})
    }

    ngOnChanges(changes: SimpleChanges) {
        if (changes.controller && changes.controller.currentValue) {
            changes.controller.currentValue.subscribe(b => b ? this.open() : this.close())
        }
    }

    private close() {
        this.accordionController.closeAll();
    }

    private open() {
        this.accordionController.openAll();
    }

    getClasses() {
        return this.classes ? this.classes.join(" ") : ""
    }

}
