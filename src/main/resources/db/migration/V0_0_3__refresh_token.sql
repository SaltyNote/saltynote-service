create table refresh_token
(
    id            MEDIUMINT                           NOT NULL AUTO_INCREMENT primary key,
    user_id       MEDIUMINT                           NOT NULL,
    refresh_token varchar(512)                        not null,
    created_time  timestamp default CURRENT_TIMESTAMP not null
);

create index refresh_token_owner_index on refresh_token (user_id);
create index refresh_token_created_index on refresh_token (created_time);