import {Translatable} from './Translatable';
import {FaqItem} from './FaqItem';

export class FaqCategory {
    constructor(
        public id: number,
        public name: Translatable,
        public description: Translatable,
        public createdAt: Date,
        public updatedAt: Date,
        public items: FaqItem[] = null,
        public children: FaqCategory[]|null = null,
        public parent: FaqCategory|null = null,
    ) {
    }

    static fromJson(category: FaqCategory) {
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
