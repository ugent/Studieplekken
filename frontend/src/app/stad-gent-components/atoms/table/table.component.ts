import { Component, EventEmitter, Input, OnChanges, OnInit, Output } from '@angular/core';
import { TabularData } from './tabular-data';

@Component({
  selector: 'app-table',
  templateUrl: './table.component.html',
  styleUrls: ['./table.component.scss'],
})
export class TableComponent<T> implements OnInit, OnChanges {
  @Input() tabularData: TabularData<T>;
  @Output() action = new EventEmitter<{data: T, columnIndex: number}>()

  constructor() {}

  ngOnInit(): void {}

  ngOnChanges() {
  }

  onAction({columnIndex, data}: {columnIndex: number, data: T}) {
    this.action.next({columnIndex, data});
  }
}
