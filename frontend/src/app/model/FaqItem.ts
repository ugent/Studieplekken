import {FaqCategory} from './FaqCategory';
import {Translatable} from './Translatable';

export class FaqItem {
    constructor(
        public id: string,
        public category: FaqCategory,
        public title: Translatable,
        public content: Translatable,
        public isPinned: boolean,
        public createdAt: Date,
        public updatedAt: Date,
    ) {
    }

    static fromJson(item: FaqItem) {
        return new FaqItem(
            item.id,
            item.category,
            item.title,
            item.content,
            item.isPinned,
            item.createdAt,
            item.updatedAt
        );
    }
}
