// A table is represented by an object type:
// The object keys are the table columns, with the values being the data itself.
export type TableData = object;

export type TableColumn = string;

// A mapper function to map model to table format.
export type TableMapper = (model: any) => TableData;

// A table action is defined by an icon and an on click handler.
export class TableAction {
    constructor(
        private icon: string,
        private handler: (data: TableData) => void
    ) {
    }

    getIcon(): string {
        return this.icon;
    }

    handle(data: TableData): void {
        this.handler(data);
    }
}

export class ListAction extends TableAction {
    constructor(
        handler: (data: TableData) => void
    ) {
        super('icon-hamburger', handler);
    }
}

export class EditAction extends TableAction {
    constructor(
        handler: (data: TableData) => void
    ) {
        super('icon-pencil', handler);
    }
}

export class DeleteAction extends TableAction {
    constructor(
        handler: (data: TableData) => void
    ) {
        super('icon-trashcan', handler);
    }
}
