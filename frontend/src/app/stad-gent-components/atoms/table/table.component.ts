import { BreakpointObserver } from '@angular/cdk/layout';
import { Component, EventEmitter, Input, OnChanges, OnInit, Output } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/internal/operators/map';
import { TabularData } from './tabular-data';

@Component({
  selector: 'app-table',
  templateUrl: './table.component.html',
  styleUrls: ['./table.component.scss'],
})
export class TableComponent<T> implements OnInit, OnChanges {
  @Input() tabularData: TabularData<T>;
  @Output() action = new EventEmitter<{data: T, columnIndex: number}>()

  // See ngOnChanges
  public copyOfTabData;

  constructor(private breakpointObserver: BreakpointObserver) {}

  ngOnInit(): void {}

  ngOnChanges(changes) {

    // If we don't eat these duplicate events, the binding of the table sometimes fails
    // I don't know why, pretty sure it's a bug in angular
    // but this way it works.
    if(changes.tabularData.previousValue?.data != changes.tabularData.currentValue.data)
      this.copyOfTabData = {...changes.tabularData.currentValue};
  }

  onAction({columnIndex, data}: {columnIndex: number, data: T}) {
    const column = this.tabularData.columns[columnIndex];
    if(!(column.type === "actionColumn") || !column.columnContent(data).disabled)
      this.action.next({columnIndex, data});
  }

  showMobile(): Observable<boolean> {
    return this.breakpointObserver.observe(['(max-width: 768px)'])
            .pipe(map(s => s.matches))
  }
}
