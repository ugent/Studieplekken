import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {FaqCategory} from '@/model/FaqCategory';
import {FaqItem} from '@/model/FaqItem';
import {Cache} from '../../cache/Cache';
import {Observable} from 'rxjs';
import {api} from '../endpoints';

@Injectable({
    providedIn: 'root'
})
export class FaqService {

    private categoryCache: Cache<string, FaqCategory>;
    private itemCache: Cache<string, FaqItem>;

    /**
     * Constructor.
     *
     * @param client
     */
    constructor(
        private client: HttpClient
    ) {
        this.categoryCache = new Cache(this.client,
            (category: FaqCategory) => category.id,
            (json: FaqCategory) => FaqCategory.fromJson(json)
        );
        this.itemCache = new Cache(this.client,
            (item: FaqItem) => item.id,
            (json: FaqItem) => FaqItem.fromJson(json)
        );
    }

    /**
     * Get all faq categories.
     *
     * @param invalidate whether to invalidate the cache
     * @returns an observable of all faq categories
     */
    getCategories(invalidate: boolean = false): Observable<FaqCategory[]> {
        return this.categoryCache.getAllValues(api.faq.categories.index, invalidate);
    }

    /**
     * Get a faq category by id.
     *
     * @param id the id of the category
     * @param invalidate whether to invalidate the cache
     * @returns an observable of the faq category
     */
    getCategory(id: string, invalidate: boolean = false): Observable<FaqCategory> {
        const url = api.faq.categories.retrieve.replace('{categoryId}', String(id));
        return this.categoryCache.getValue(id, url, invalidate);
    }

    /**
     * Update a faq category.
     *
     * @param categoryId the id of the category
     * @param category the body of the category to update
     * @returns an observable of the updated category
     */
    updateCategory(categoryId: string, category: FaqCategory): Observable<FaqCategory> {
        return this.client.put<FaqCategory>(api.faq.categories.update.replace('{categoryId}', categoryId), category);
    }


    /**
     * Delete a faq category.
     *
     * @param categoryId the id of the category to delete
     * @returns an observable of the deleted category
     */
    deleteCategory(categoryId: string): Observable<void> {
        return this.client.delete<void>(api.faq.categories.delete.replace('{categoryId}', categoryId));
    }

    /**
     * Get all faq items for a category.
     *
     * @param categoryId the id of the category
     * @param invalidate whether to invalidate the cache
     * @returns an observable of all faq items for the category
     */
    getCategoryItems(categoryId: number, invalidate: boolean = false): Observable<FaqItem[]> {
        const url = api.faq.categories.items.replace('{categoryId}', String(categoryId));
        return this.itemCache.getAllValues(url, invalidate);
    }

    /**
     * Create a faq category.
     *
     * @param category the category to create
     * @returns an observable of the created category
     */
    addCategory(category: FaqCategory): Observable<FaqCategory> {
        return this.client.post<FaqCategory>(api.faq.categories.create, category);
    }

    /**
     * Get all faq items.
     *
     * @param invalidate whether to invalidate the cache
     * @returns an observable of all faq items
     */
    getItems(invalidate: boolean = false): Observable<FaqItem[]> {
        return this.itemCache.getAllValues(api.faq.items.index, invalidate);
    }

    /**
     * Get all pinned faq items.
     *
     * @param invalidate whether to invalidate the cache
     * @returns an observable of all pinned faq items
     */
    getPinnedItems(invalidate: boolean = false): Observable<FaqItem[]> {
        return this.itemCache.getAllValues(api.faq.items.pinned, invalidate);
    }

    /**
     * Get a faq item by id.
     *
     * @param id the id of the item
     * @param invalidate whether to invalidate the cache
     * @returns an observable of the faq item
     */
    getItem(id: string, invalidate: boolean = false): Observable<FaqItem> {
        const url = api.faq.items.retrieve.replace('{itemId}', String(id));
        return this.itemCache.getValue(id, url, invalidate);
    }

    /**
     * Update a faq item.
     *
     * @param itemId the id of the item
     * @param item the new body of the item
     * @returns an observable of the updated item
     */
    updateItem(itemId: string, item: FaqItem): Observable<FaqItem> {
        return this.client.put<FaqItem>(api.faq.items.update.replace('{itemId}', itemId), item);
    }

    /**
     * Delete a faq item.
     *
     * @param itemId the id of the item to delete.
     * @returns an observable of the deleted item
     */
    deleteItem(itemId: string): Observable<void> {
        return this.client.delete<void>(api.faq.items.delete.replace('{itemId}', itemId));
    }

    /**
     * Create a faq item.
     *
     * @param item the item to create
     * @returns an observable of the created item
     */
    addItem(item: FaqItem): Observable<FaqItem> {
        return this.client.post<FaqItem>(api.faq.items.create, item);
    }
}
