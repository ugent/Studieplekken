import { Component, Input, OnInit, ContentChild, TemplateRef } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
    selector: 'app-new-table',
    templateUrl: './new-table.component.html',
    styleUrls: ['./new-table.component.scss']
})
export class NewTableComponent<T> implements OnInit {
    @Input()
    public items: T[] = [];

    @Input()
    public filterFields: string[] = [];

    @Input()
    public itemsPerPageOptions: number[] = DEFAULT_TABLE_PER_PAGE_OPTIONS;

    @Input()
    public stateKey: string = DEFAULT_TABLE_STATE_KEY;

    @ContentChild('header', { static: true })
    public headerTemplate!: TemplateRef<any>;

    @ContentChild('body', { static: true })
    public bodyTemplate!: TemplateRef<any>;

    protected filteredItems: T[] = [];
    protected currentPage: number = DEFAULT_TABLE_PAGE;
    protected itemsPerPage: number = DEFAULT_TABLE_PER_PAGE;
    protected searchQuery: string;
    protected sortField: string;
    protected sortOrder: number;

    constructor(
        private readonly route: ActivatedRoute,
        private readonly router: Router
    ) {}

    public async ngOnInit(): Promise<void> {
        // Load initial state from query parameters.
        const queryParams = await this.route.queryParams.toPromise();

        // Set initial state based on query parameters.
        this.searchQuery = queryParams[this.getFilterKey('search')];
        this.currentPage = +queryParams[this.getFilterKey('page')];
        this.itemsPerPage = +queryParams[this.getFilterKey('perPage')];
        this.sortField = queryParams[this.getFilterKey('sort')];
        this.sortOrder = +queryParams[this.getFilterKey('order')];
        
        // Apply the filters.
        this.applyFilters();
    }

    /**
     * Applies filters, sorting, and pagination to the items array.
     * 
     * @protected
     * @returns {void}
     */
    protected applyFilters(): void {
        // Clone the items array to avoid mutating the original array.
        let filtered = [...this.items];

        // Global Search based on filterFields.
        if (this.searchQuery && this.filterFields.length > 0) {
            const query = this.searchQuery.toLowerCase();

            filtered = filtered.filter(item =>
                this.filterFields.some(field => {
                    const value = this.getNestedValue(item, field);
                    return value?.toString().toLowerCase().includes(query);
                })
            );
        }

        // Sorting.
        if (this.sortField) {
            filtered.sort((a, b) => {
                const aValue = this.getNestedValue(a, this.sortField);
                const bValue = this.getNestedValue(b, this.sortField);
                if (aValue < bValue) return -1 * this.sortOrder;
                if (aValue > bValue) return 1 * this.sortOrder;
                return 0;
            });
        }

        // Pagination.
        const start = (this.currentPage - 1) * this.itemsPerPage;
        this.filteredItems = filtered.slice(start, start + this.itemsPerPage);
    }

    /**
     * Updates the query parameters in the URL based on the current state of the component.
     * The query parameters are merged with the existing ones in the URL.
     * 
     * This implicitly triggers the applyFilters method by the subscription.
     * 
     * @protected
     * @returns {void}
     */
    protected updateFilters(): void {
        const queryParams = {
            [this.getFilterKey('search')]: this.searchQuery,
            [this.getFilterKey('page')]: this.currentPage,
            [this.getFilterKey('perPage')]: this.itemsPerPage,
            [this.getFilterKey('sort')]: this.sortField,
            [this.getFilterKey('order')]: this.sortOrder
        };

        this.router.navigate([], {
            queryParams: queryParams,
            queryParamsHandling: 'merge', 
        });
    }

    /**
     * Changes the current page to the specified new page number.
     *
     * @param newPage - The new page number to switch to.
     * @protected
     * @returns {void}
     */
    protected changePage(newPage: number): void {
        if (newPage < 1) return;
        this.currentPage = newPage;
        this.applyFilters();
    }

    /**
     * Updates the number of items displayed per page and resets to the first page.
     * Also applies any active filters to the data.
     *
     * @param newItemsPerPage - The new number of items to display per page.
     */
    protected changeItemsPerPage(newItemsPerPage: number): void {
        this.itemsPerPage = newItemsPerPage;
        this.currentPage = 1;
        this.applyFilters();
    }

    /**
     * Handles the sorting logic for a given field. If the field is already the current sort field,
     * it toggles the sort order between ascending and descending. If the field is different, it sets
     * the sort field to the new field and defaults the sort order to ascending.
     *
     * @param field - The field to sort by.
     */
    protected onSort(field: string): void {
        if (this.sortField === field) {
            this.sortOrder = -this.sortOrder;
        } else {
            this.sortField = field;
            this.sortOrder = 1;
        }

        this.applyFilters();
    }

    
    /**
     * Constructs a filter key string based on the provided field and the component's state key.
     *
     * @param field - The field name to be included in the filter key.
     * @returns The constructed filter key string in the format `${this.stateKey}_filter_${field}`.
     */
    private getFilterKey(field: string): string {
        return `${this.stateKey}_filter_${field}`;
    }   

    /**
     * Utility method to get nested values using dot notation.
     */
    private getNestedValue(obj: any, path: string): any {
        return path.split('.').reduce((acc, part) => acc && acc[part], obj);
    }
}

export const DEFAULT_TABLE_PER_PAGE_OPTIONS = [10, 25, 50, 100];
export const DEFAULT_TABLE_STATE_KEY = 'table';
export const DEFAULT_TABLE_PER_PAGE = 10;
export const DEFAULT_TABLE_PAGE = 1;
