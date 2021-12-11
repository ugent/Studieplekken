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

  constructor(private breakpointObserver: BreakpointObserver) {}

  ngOnInit(): void {}

  ngOnChanges() {
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
