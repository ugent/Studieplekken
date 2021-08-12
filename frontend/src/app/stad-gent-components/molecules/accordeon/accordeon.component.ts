import { AfterViewInit, Component, ElementRef, OnChanges, OnInit, QueryList, SimpleChanges, ViewChild, ViewChildren } from '@angular/core';
import * as Accordion from "gent_styleguide/build/styleguide/js/accordion.functions.js"

@Component({
  selector: 'app-accordeon',
  templateUrl: './accordeon.component.html',
  styleUrls: ['./accordeon.component.scss']
})
export class AccordeonComponent implements AfterViewInit {
  @ViewChildren("accordioncontainer", {read: ElementRef}) container: QueryList<ElementRef>;
  private accordionController: any;

  constructor() { }

  ngAfterViewInit(): void {
    this.accordionController = new Accordion(this.container.first.nativeElement)
  }

}
