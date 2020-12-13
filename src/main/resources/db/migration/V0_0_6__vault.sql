create table vault
(
    id           MEDIUMINT                           NOT NULL AUTO_INCREMENT primary key,
    user_id      MEDIUMINT                           not null,
    secret       varchar(512)                        not null,
    type         varchar(64)                         not null,
    created_time timestamp default CURRENT_TIMESTAMP not null,
    constraint vault_secret_uindex unique (secret)
);