import {Component, EventEmitter, Input, Output} from '@angular/core';
import {Column, ContentColumn, TabularData} from '../tabular-data';

@Component({
    selector: 'app-desktop-table',
    templateUrl: './desktop-table.component.html',
    styleUrls: ['./desktop-table.component.scss']
})
export class DesktopTableComponent<T> {
    @Input() tabularData: TabularData<T>;
    @Output() action = new EventEmitter<{ data: T, columnIndex: number }>();

    constructor() {}

    onAction(columnIndex: number, data: T): void {
        this.action.next({columnIndex, data});
    }

    getWidth(column: Column<T>): string | number {
        return column.width || '';
    }

    translateColumnContent(column: ContentColumn<T>, data: T): string {
        return column.translateColumnContent ? column.translateColumnContent(data) : '';
    }

    getCssClasses(datapoint: T): string[] {
        if (this.tabularData.css) {
            return this.tabularData.css(datapoint);
        }

        return [];
    }

    trackByMethod(index: number, _: T): number {
        return index;
    }
}
