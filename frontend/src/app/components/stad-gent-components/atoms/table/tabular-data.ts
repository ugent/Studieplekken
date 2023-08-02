type IconAction = {
    actionType: 'icon',
    actionContent: string,
    fallbackContent: string,
    disabled?: boolean
};

type StringAction = {
    actionType: 'string',
    actionContent: string,
    disabled?: boolean
};

type ButtonAction = {
    actionType: 'button',
    actionContent: string,
    buttonClass: string,
    disabled?: boolean
};

export type ActionContent = IconAction | StringAction | ButtonAction;
export type ActionCell<T> = (a: T) => ActionContent;

export type ContentCell<T> = (a: T) => string;

export type ActionColumn<T> = {
    type: 'actionColumn',
    columnHeader: string,
    columnContent: ActionCell<T>,
    width?: number
};

export type ContentColumn<T> = {
    type: 'contentColumn',
    columnHeader: string,
    columnContent: ContentCell<T>,
    translateColumnContent?: ContentCell<T>,
    width?: number
};

export type Column<T> = ActionColumn<T> | ContentColumn<T>;


export interface TabularData<T> {
    columns: Column<T>[];
    data: T[];
    css?: (a: T) => string[];
}
