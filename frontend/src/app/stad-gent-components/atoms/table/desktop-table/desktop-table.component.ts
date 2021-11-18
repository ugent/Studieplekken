import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Column, TabularData } from '../tabular-data';

@Component({
  selector: 'app-desktop-table',
  templateUrl: './desktop-table.component.html',
  styleUrls: ['./desktop-table.component.scss']
})
export class DesktopTableComponent<T> implements OnInit {
  @Input() tabularData: TabularData<T>;
  @Output() action = new EventEmitter<{data: T, columnIndex: number}>()

  constructor() { }

  ngOnInit(): void {
  }


  onAction(columnIndex: number, data: T) {
    this.action.next({columnIndex, data});
  }

  getWidth(column: Column<T>) {
    return column.width || ""
  }
}
