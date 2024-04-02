DROP TABLE IF EXISTS faq_categories;
DROP TABLE IF EXISTS faq_items;

CREATE TABLE faq_categories
(
    id                  bigint generated always as identity,
    name_translation_id bigint,
    created_by_user_id  text,
    created_at          timestamp default current_timestamp,
    updated_at          timestamp default current_timestamp,

    primary key (id),
    foreign key (created_by_user_id) references users (user_id)
);

CREATE TABLE faq_items
(
    id                     bigint generated always as identity,
    category_id            bigint null,
    created_by_user_id     text,
    title_translation_id   bigint,
    content_translation_id bigint,
    is_pinned              boolean   default false,
    created_at             timestamp default current_timestamp,
    updated_at             timestamp default current_timestamp,

    primary key (id),
    foreign key (category_id) references faq_categories (id),
    foreign key (created_by_user_id) references users (user_id)
)