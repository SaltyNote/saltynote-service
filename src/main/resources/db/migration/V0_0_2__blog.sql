create table blog
(
    id           MEDIUMINT                           NOT NULL AUTO_INCREMENT primary key,
    user_id      MEDIUMINT                           NOT NULL,
    title        varchar(128)                        not null,
    content      text,
    created_time timestamp default CURRENT_TIMESTAMP not null
);

create index blog_owner_index on blog (user_id);