create table user
(
    id            MEDIUMINT                             NOT NULL AUTO_INCREMENT primary key,
    username      varchar(128)                          not null,
    password      varchar(128)                          not null,
    register_time timestamp   default CURRENT_TIMESTAMP not null,
    constraint user_username_uindex
        unique (username)
);