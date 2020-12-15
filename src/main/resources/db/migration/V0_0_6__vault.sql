create table vault
(
    id           varchar(40)                         not null primary key,
    user_id      varchar(40)                         not null,
    secret       varchar(512)                        not null,
    type         varchar(64)                         not null,
    created_time timestamp default CURRENT_TIMESTAMP not null,
    constraint vault_secret_uindex unique (secret)
);