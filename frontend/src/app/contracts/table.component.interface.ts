import {TableAction, TableMapper} from '../model/Table';

export declare interface TableComponent {
    /**
     * A mapper to map data items to their column representation,
     * where the keys represent the column names (a translation key) and the values the
     * corresponding table entries.
     */
    getTableMapper(): TableMapper;

    /**
     * A list of table actions, displayed in the last column of the table.
     */
    getTableActions(): TableAction[];
}
