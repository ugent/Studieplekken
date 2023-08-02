import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import {TableAction, TableColumn, TableData, TableMapper} from '../../../../extensions/model/Table';
import {ActivatedRoute, Params, Router} from '@angular/router';
import {debounceTime, distinctUntilChanged, filter, first, map} from 'rxjs/operators';
import {combineLatest, Observable, ReplaySubject, Subject, Subscription} from 'rxjs';
import {genericSorter, OrderDirection} from '../../../../extensions/util/Util';

type Pagination = {
    perPage: number;
    currentPage: number;
};

type Ordering = {
    by: string;
    direction: OrderDirection;
};

type Search = string;

@Component({
    selector: 'app-management-table',
    templateUrl: './management-table.component.html',
    styleUrls: ['./management-table.component.scss']
})
export class ManagementTableComponent implements OnInit, OnDestroy {

    @Input() mapper: TableMapper;
    @Input() actions: TableAction[];

    @Input() loading: Observable<boolean>;
    @Input() data: Observable<TableData[]>;

    protected ordering: Subject<Ordering>;
    protected pagination: Subject<Pagination>;
    protected searcher: Subject<Search>;
    protected search: Observable<Search>;

    protected columns: Observable<TableColumn[]>;
    protected searchedData: Observable<TableData[]>;
    protected filteredData: Observable<TableData[]>;

    private subscription: Subscription;

    constructor(
        private router: Router,
        private activatedRoute: ActivatedRoute
    ) {
        this.searcher = new ReplaySubject();
        this.ordering = new ReplaySubject();
        this.pagination = new ReplaySubject();
        this.subscription = new Subscription();

        // Debounce search input for better performance.
        this.search = this.searcher.pipe(
            debounceTime(250),
            distinctUntilChanged()
        );
    }

    ngOnInit(): void {
        // Filter the columns out of the data observable.
        this.columns = this.data.pipe(
            filter(data =>
                data.length > 0
            ),
            map(data =>
                Object.keys(
                    this.mapper(data[0])
                )
            )
        );

        // Filter the data with the search query.
        this.searchedData = combineLatest([
            this.data, this.columns, this.search
        ]).pipe(
            map(([data, columns, search]) => {
                return data.filter(
                    (item: TableData) => columns.some(column => new RegExp(search, 'i').test(this.mapper(item)[column]))
                );
            })
        );

        // Filter the data with the search query, pagination and order.
        this.filteredData = combineLatest([
            this.searchedData, this.ordering, this.pagination, this.columns
        ]).pipe(
            map(([data, ordering, pagination, columns]) => {
                return data.sort((a, b) => {
                    if (ordering.by && columns.includes(ordering.by)) {
                        const one: any = this.mapper(a)[ordering.by];
                        const two: any = this.mapper(b)[ordering.by];

                        return genericSorter(one, two, ordering.direction);
                    }
                    return 0;
                }).slice(
                    (pagination.currentPage - 1) * pagination.perPage, pagination.currentPage * pagination.perPage
                );
            })
        );

        // Extract the filter data from the query params.
        // We only have to do this once on component load, hence the first() pipe.
        this.activatedRoute.queryParams.pipe(first()).subscribe(params => {
            this.searcher.next(params.search ?? '');
            this.pagination.next({
                currentPage: Number(params.currentPage ?? 1),
                perPage: Number(params.perPage ?? 15)
            });
            this.ordering.next({
                by: params.orderBy ?? '',
                direction: Number(params.orderDirection ?? OrderDirection.DESC)
            });
        });

        // On each filter update, we want to reflect these changes in the current URL.
        // This way, the component is stateless in the sense that the state can be shared by URL.
        this.subscription.add(
            combineLatest([
                this.search, this.pagination, this.ordering
            ]).subscribe(([search, pagination, ordering]) => {
                const params: Params = {
                    search,
                    currentPage: pagination.currentPage,
                    perPage: pagination.perPage,
                    orderBy: ordering.by,
                    orderDirection: ordering.direction
                };

                void this.router.navigate([], {
                    relativeTo: this.activatedRoute,
                    queryParams: params,
                    replaceUrl: true
                });
            })
        );
    }

    ngOnDestroy(): void {
        this.subscription.unsubscribe();
    }

    /**
     * Get the start index of the displayed items.
     */
    getStartIndex(pagination: Pagination): number {
        return (pagination.currentPage - 1) * Number(pagination.perPage) + 1;
    }

    /**
     * Get the end index of the displayed items.
     */
    getEndIndex(pagination: Pagination, resultCount: number): number {
        return Math.min(
            this.getStartIndex(pagination) + pagination.perPage - 1, resultCount
        );
    }

    /**
     * Get the total pagination count.
     */
    getPageCount(pagination: Pagination, resultCount: number): number {
        return Math.ceil(resultCount / pagination.perPage);
    }

    /**
     * Get a list of page numbers for pagination.
     */
    getPages(pagination: Pagination, resultCount: number): number[] {
        const length = this.getPageCount(pagination, resultCount);
        const maxLength = 6;

        if (length <= maxLength) {
            return Array.from({length}, (_, i) =>
                i + 1
            );
        }

        const firstPage = Math.max(1, pagination.currentPage - Math.floor(maxLength / 2));
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
    getPerPageOptions(pagination: Pagination): number[] {
        const perPage = [15, 25, 50];

        if (!perPage.includes(pagination.perPage)) {
            perPage.push(pagination.perPage);
        }

        return perPage.sort();
    }

    /**
     * Checks whether pagination is possible.
     */
    hasPages(pagination: Pagination, resultCount: number): boolean {
        return this.getPageCount(pagination, resultCount) > 0;
    }

    /**
     * Checks whether a next page exists.
     */
    hasNextPage(pagination: Pagination, resultCount: number): boolean {
        return pagination.currentPage < this.getPageCount(pagination, resultCount);
    }

    /**
     * Checks whether a previous page exists.
     */
    hasPreviousPage(pagination: Pagination): boolean {
        return pagination.currentPage > 1;
    }

    /**
     * Go to the next page, if possible.
     */
    nextPage(pagination: Pagination, resultCount: number): void {
        if (this.hasNextPage(pagination, resultCount)) {
            this.setCurrentPage(
                pagination, pagination.currentPage + 1
            );
        }
    }

    /**
     * Go to the previous page, if possible.
     */
    previousPage(pagination: Pagination): void {
        if (this.hasPreviousPage(pagination)) {
            this.setCurrentPage(
                pagination, pagination.currentPage - 1
            );
        }
    }

    /**
     * Check the current page number against the given.
     *
     * @param pagination the pagination object.
     * @param page the page number to check.
     */
    isCurrentPage(pagination: Pagination, page: number): boolean {
        return pagination.currentPage === page;
    }

    /**
     * Set the current page to the given page number.
     *
     * @param pagination the pagination object.
     * @param page the new page number.
     */
    setCurrentPage(pagination: Pagination, page: number): void {
        this.pagination.next({
            ...pagination,
            currentPage: Number(page)
        });
    }

    /**
     * Set the per page results.
     *
     * @param per the amount of results to display.
     */
    setPerPage(per: number): void {
        this.pagination.next({
            currentPage: 1,
            perPage: Number(per)
        });
    }

    toggleOrderBy(ordering: Ordering, column: string): void {
        const direction = OrderDirection.ASC === ordering.direction ? OrderDirection.DESC : OrderDirection.ASC;
        const by = direction === OrderDirection.ASC ? '' : ordering.by;

        if (column === ordering.by) {
            this.ordering.next({
                by,
                direction
            });
        } else {
            this.ordering.next({
                ...ordering,
                by: column
            });
        }
    }

    getOrderIconClass(ordering: Ordering, column: string): string {
        if (column !== ordering.by) {
            return 'icon-level';
        }

        if (ordering.direction === OrderDirection.DESC) {
            return 'icon-chevron-down';
        }

        return 'icon-chevron-up';
    }
}


