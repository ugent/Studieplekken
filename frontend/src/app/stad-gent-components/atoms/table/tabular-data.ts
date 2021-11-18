type IconAction = {
  actionType: "icon",
  actionContent: string,
  fallbackContent: string,
  disabled?: boolean
}

type StringAction = {
  actionType: "string",
  actionContent: string,
  disabled?: boolean
}

export type ActionContent = IconAction | StringAction;
export type ActionCell<T> = (a: T) => ActionContent;

export type ContentCell<T> = (a: T) => string;

type ActionColumn<T> = {
  type: "actionColumn",
  columnHeader: string,
  columnContent: ActionCell<T>,
  width?: number
}

type ContentColumn<T> = {
  type: "contentColumn",
  columnHeader: string,
  columnContent: ContentCell<T>,
  width?: number
}

export type Column<T> = ActionColumn<T> | ContentColumn<T>;


export interface TabularData<T> {
  columns: Column<T>[];
  data: T[];
}
