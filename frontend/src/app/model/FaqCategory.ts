import {Translatable} from './Translatable';
import {FaqItem} from './FaqItem';

export class FaqCategory {
    constructor(
        public id: string,
        public name: Translatable,
        public description: Translatable,
        public createdAt: Date,
        public updatedAt: Date,
        public items?: FaqItem[],
        public children?: FaqCategory[],
        public parent?: FaqCategory,
    ) {
    }

    static fromJson(category: Partial<FaqCategory> = {}) {
        return new FaqCategory(
            category.id,
            category.name,
            category.description,
            category.createdAt,
            category.updatedAt,
            category.items,
            category.children,
            category.parent
        );
    }
}
