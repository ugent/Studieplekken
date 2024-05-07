import {FaqCategory} from './FaqCategory';
import {Translatable} from './Translatable';

export class FaqItem {
    constructor(
        public id: string,
        public isPinned: boolean,
        public createdAt: Date,
        public updatedAt: Date,
        public category?: FaqCategory,
        public title?: Translatable,
        public content?: Translatable
    ) {
    }

    static fromJson(item: Partial<FaqItem> = {}) {
        return new FaqItem(
            item.id ?? '',
            item.isPinned ?? false,
            item.createdAt ?? new Date,
            item.updatedAt ?? new Date,
            item.category,
            item.title,
            item.content
        );
    }
}
