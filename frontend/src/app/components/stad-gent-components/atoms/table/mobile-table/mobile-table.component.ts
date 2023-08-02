import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { ContentColumn, TabularData } from '../tabular-data';

@Component({
  selector: 'app-mobile-table',
  templateUrl: './mobile-table.component.html',
  styleUrls: ['./mobile-table.component.scss']
})
export class MobileTableComponent<T> implements OnInit {
  @Input() tabularData: TabularData<T>;
  @Output() action = new EventEmitter<{data: T, columnIndex: number}>()

  constructor() { }

  ngOnInit(): void {
  }

  onAction(columnIndex: number, data: T) {
    this.action.next({columnIndex, data});
  }

  nonActionColumns() {
    return this.tabularData.columns.filter(c => c.type !== "actionColumn")
  }

  actionColumns() {
    return this.tabularData.columns.filter(c => c.type === "actionColumn")
  }

  translateColumnContent(column: ContentColumn<T>, data: T): string {
    return column.translateColumnContent ? column.translateColumnContent(data) : "";
  }

  getCssClasses(datapoint: T): string[] {
    if(this.tabularData.css)
    return this.tabularData.css(datapoint)

    return [];
  }
}
