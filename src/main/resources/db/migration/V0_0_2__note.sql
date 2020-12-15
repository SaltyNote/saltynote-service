create table note
(
    id           varchar(40)                         not null primary key,
    user_id      varchar(40)                         NOT NULL,
    text         varchar(512)                        not null,
    url          varchar(512)                        not null,
    note         text,
    is_page_only bit       default b'0'              null,
    created_time timestamp default CURRENT_TIMESTAMP not null
);

create index note_owner_index on note (user_id);
create index note_url_index on note (url);