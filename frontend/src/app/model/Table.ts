// A table is represented by an object type:
// The object keys are the table columns, with the values being the data itself.
import {OrderDirection} from '@/helpers/Util';

export type TableData<T> = {
    mapped: object,
    raw: T
};

export type TableColumn = string;

// A mapper function to map model to table format.
export type TableMapper<T> = (model: T) => object;

// A table action is defined by an icon and an on click handler.
export class TableAction<T> {
    constructor(
        private icon: string,
        private handler = (_: T) => {},
        private shouldShow = (_: T) => true
    ) {
    }

    getIcon(): string {
        return this.icon;
    }

    show(data: T): boolean {
        return this.shouldShow(data);
    }

    handle(data: T): void {
        this.handler(data);
    }
}

export class ListAction<T> extends TableAction<T> {
    constructor(
        handler: (data: T) => void
    ) {
        super('icon-hamburger', handler);
    }
}

export class EditAction<T> extends TableAction<T> {
    constructor(
        handler: (data: T) => void,
        shouldShow = (_: T) => true
    ) {
        super('icon-pencil', handler, shouldShow);
    }
}

export class DeleteAction<T> extends TableAction<T> {
    constructor(
        handler: (data: T) => void,
        shouldShow = (_: T) => true
    ) {
        super('icon-trashcan', handler, shouldShow);
    }
}

export type Pagination = {
    perPage: number;
    currentPage: number;
};

export type Ordering = {
    orderBy: string;
    orderDirection: OrderDirection;
};

export type Search = string;
