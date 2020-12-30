create table user
(
    id            varchar(40)                         not null    primary key,
    username      varchar(128)                        not null,
    email         varchar(128)                        not null,
    password      varchar(128)                        not null,
    register_time timestamp default CURRENT_TIMESTAMP not null,
    constraint user_email_uindex unique (email),
    constraint user_username_uindex unique (username)
);