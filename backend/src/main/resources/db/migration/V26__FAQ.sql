DROP TABLE IF EXISTS faq_categories CASCADE;
DROP TABLE IF EXISTS faq_items CASCADE;

CREATE TABLE faq_categories
(
    id                          bigint generated always as identity,
    name_translatable_id        bigint,
    description_translatable_id bigint,
    category_id                 bigint null,
    created_by_user_id          text,
    icon_class                  text,
    created_at                  timestamp default current_timestamp,
    updated_at                  timestamp default current_timestamp,

    primary key (id),
    foreign key (created_by_user_id) references users (user_id),
    foreign key (category_id) references faq_categories (id),
    foreign key (description_translatable_id) references translatables (id),
    foreign key (name_translatable_id) references translatables (id)
);

CREATE TABLE faq_items
(
    id                      bigint generated always as identity,
    title_translatable_id   bigint,
    content_translatable_id bigint,
    category_id             bigint null,
    created_by_user_id      text,
    is_pinned               boolean   default false,
    created_at              timestamp default current_timestamp,
    updated_at              timestamp default current_timestamp,

    primary key (id),
    foreign key (category_id) references faq_categories (id),
    foreign key (created_by_user_id) references users (user_id),
    foreign key (content_translatable_id) references translatables (id),
    foreign key (title_translatable_id) references translatables (id)
)