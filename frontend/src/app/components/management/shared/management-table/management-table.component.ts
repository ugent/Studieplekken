import {ChangeDetectionStrategy, Component, Input, OnChanges, OnDestroy, OnInit, SimpleChanges} from '@angular/core';
import {TableAction, TableColumn, TableData, TableMapper} from '../../../../model/Table';
import {ActivatedRoute, Params, Router} from '@angular/router';
import {debounceTime, first, takeUntil} from 'rxjs/operators';
import {isObservable, ReplaySubject, Subject} from 'rxjs';
import {escapeRegex, genericSorter, OrderDirection} from '../../../../extensions/util/Util';

@Component({
    selector: 'app-management-table',
    templateUrl: './management-table.component.html',
    styleUrls: ['./management-table.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ManagementTableComponent<T> implements OnInit, OnChanges, OnDestroy {

    @Input() data: T[];
    @Input() stateless = true;
    @Input() actions: TableAction<T>[] = [];

    protected columns: TableColumn[] = [];
    protected rawData: TableData<T>[] = [];
    protected searchedData: TableData<T>[] = [];
    protected filteredData: TableData<T>[] = [];

    protected ordering: Ordering = {
        orderBy: '',
        orderDirection: OrderDirection.DESC
    };

    protected pagination: Pagination = {
        currentPage: 1,
        perPage: 15
    };

    protected search: Search = '';
    protected searchSub$ =
        new ReplaySubject<string>();

    protected unsubscribe$ =
        new Subject<void>();

    constructor(
        private router: Router,
        private activatedRoute: ActivatedRoute
    ) {
    }

    ngOnInit(): void {
        // Extract the filter data from the query params.
        // We only have to do this once on component load, hence the first() pipe.
        if (this.stateless) {
            this.activatedRoute.queryParams.pipe(first()).subscribe(routeParams => {
                let params = {
                    ...this.ordering, ...this.pagination, search: this.search
                };

                if (this.stateless) {
                    params = {...params, ...routeParams};
                }

                this.search = params.search;

                this.pagination = {
                    currentPage: Number(params.currentPage),
                    perPage: Number(params.perPage)
                };

                this.ordering = {
                    orderBy: params.orderBy,
                    orderDirection: Number(params.orderDirection)
                };
            });
        }

        this.searchSub$.pipe(
            takeUntil(this.unsubscribe$),
            debounceTime(500)
        ).subscribe(value =>
            this.setSearch(value)
        );
    }

    ngOnChanges(changes: SimpleChanges): void {
        // Set up the initial data.
        if (changes.mapper || changes.data) {
            this.updateColumns();
            this.updateData();
        }
    }

    ngOnDestroy(): void {
        this.unsubscribe$.next();
        this.unsubscribe$.complete();
    }

    @Input() mapper: TableMapper<T> = (item) => ({});

    isLoading(): boolean {
        return this.data === undefined || this.data === null;
    }

    noResults(): boolean {
        return !this.isLoading() && this.filteredData?.length === 0;
    }

    isObservable(entry: any): boolean {
        return isObservable(entry);
    }

    /**
     * Get the start index of the displayed items.
     */
    getStartIndex(): number {
        return (this.pagination.currentPage - 1) * Number(this.pagination.perPage) + 1;
    }

    /**
     * Get the end index of the displayed items.
     */
    getEndIndex(): number {
        return Math.min(
            this.getStartIndex() + this.pagination.perPage - 1, this.searchedData.length
        );
    }

    /**
     * Get the total pagination count.
     */
    getPageCount(): number {
        return Math.ceil(this.searchedData.length / this.pagination.perPage);
    }

    /**
     * Get a list of page numbers for pagination.
     */
    getPages(): number[] {
        const length = this.getPageCount();
        const maxLength = 6;

        if (length <= maxLength) {
            return Array.from({length}, (_, i) =>
                i + 1
            );
        }

        const firstPage = Math.max(1, this.pagination.currentPage - Math.floor(maxLength / 2));
        const lastPage = Math.min(length, firstPage + maxLength - 1);
        const pages: number[] = [];

        for (let i = firstPage; i <= lastPage; i++) {
            pages.push(i);
        }

        return pages;
    }

    /**
     * Get the possible display per page options.
     */
    getPerPageOptions(): number[] {
        const perPage = [15, 25, 50];

        if (!perPage.includes(this.pagination.perPage)) {
            perPage.push(this.pagination.perPage);
        }

        return perPage.sort();
    }

    /**
     * Checks whether pagination is possible.
     */
    hasPages(): boolean {
        return this.getPageCount() > 0;
    }

    /**
     * Checks whether a next page exists.
     */
    hasNextPage(): boolean {
        return this.pagination.currentPage < this.getPageCount();
    }

    /**
     * Checks whether a previous page exists.
     */
    hasPreviousPage(): boolean {
        return this.pagination.currentPage > 1;
    }

    /**
     * Go to the next page, if possible.
     */
    nextPage(): void {
        if (this.hasNextPage()) {
            this.setCurrentPage(
                this.pagination.currentPage + 1
            );
        }
    }

    /**
     * Go to the previous page, if possible.
     */
    previousPage(): void {
        if (this.hasPreviousPage()) {
            this.setCurrentPage(
                this.pagination.currentPage - 1
            );
        }
    }

    /**
     * Check the current page number against the given.
     *
     * @param page the page number to check.
     */
    isCurrentPage(page: number): boolean {
        return this.pagination.currentPage === page;
    }

    setSearch(search: string): void {
        this.search = search;

        this.updateData();
        this.updateQuery();
        this.resetPagination();
    }

    /**
     * Set the current page to the given page number.
     *
     * @param page the new page number.
     */
    setCurrentPage(page: number): void {
        this.pagination = {
            ...this.pagination,
            currentPage: Number(page)
        };

        this.updateData();
        this.updateQuery();
    }

    /**
     * Set the per page results.
     *
     * @param per the amount of results to display.
     */
    setPerPage(per: number): void {
        this.pagination = {
            currentPage: 1,
            perPage: Number(per)
        };

        this.resetPagination();
        this.updateData();
        this.updateQuery();
    }

    toggleOrderBy(column: string): void {
        const orderDirection = OrderDirection.ASC === this.ordering.orderDirection ?
            OrderDirection.DESC :
            OrderDirection.ASC;
        const orderBy = orderDirection === OrderDirection.DESC && !!this.ordering.orderBy ?
            '' :
            this.ordering.orderBy;

        if (column === this.ordering.orderBy) {
            this.ordering = {
                orderBy,
                orderDirection
            };
        } else {
            this.ordering = {
                ...this.ordering,
                orderBy: column
            };
        }

        this.updateData();
        this.updateQuery();
    }

    updateColumns(): void {
        this.columns = this.data?.length > 0 ? Object.keys(
            this.mapper(this.data[0])
        ) : [];
    }

    updateData(): void {
        this.rawData = this.data ? this.data.map(data => ({
            raw: data,
            mapped: this.mapper(data)
        })) : [];

        this.searchedData = this.rawData.filter(
            (item: TableData<T>) => this.columns.some(column => new RegExp(
                escapeRegex(this.search), 'i'
            ).test(item.mapped[column]))
        );

        this.filteredData = this.searchedData.sort((a, b) => {
            if (this.ordering.orderBy && this.columns.includes(this.ordering.orderBy)) {
                return genericSorter(
                    a.mapped[this.ordering.orderBy], b.mapped[this.ordering.orderBy], this.ordering.orderDirection
                );
            }
            return 0;
        }).slice(
            (this.pagination.currentPage - 1) * this.pagination.perPage, this.pagination.currentPage * this.pagination.perPage
        );
    }

    resetPagination(): void {
        this.pagination = {
            ...this.pagination,
            currentPage: 1
        };
    }

    updateQuery(): void {
        if (this.stateless) {
            const params: Params = {
                search: this.search,
                currentPage: this.pagination.currentPage,
                perPage: this.pagination.perPage,
                orderBy: this.ordering.orderBy,
                orderDirection: this.ordering.orderDirection
            };

            void this.router.navigate([], {
                relativeTo: this.activatedRoute,
                queryParams: params,
                replaceUrl: true
            });
        }
    }

    getOrderIconClass(column: string): string {
        if (column !== this.ordering.orderBy) {
            return 'icon-level';
        }

        if (this.ordering.orderDirection === OrderDirection.ASC) {
            return 'icon-chevron-up';
        }

        return 'icon-chevron-down';
    }
}

type Pagination = {
    perPage: number;
    currentPage: number;
};

type Ordering = {
    orderBy: string;
    orderDirection: OrderDirection;
};

type Search = string;
