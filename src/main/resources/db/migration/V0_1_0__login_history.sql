create table login_history
(
    id         int auto_increment primary key      not null,
    user_id    varchar(40)                         not null,
    remote_ip  varchar(128)                        null,
    user_agent varchar(256)                        null,
    login_time timestamp default CURRENT_TIMESTAMP not null
);

create index login_history_user_index on note (user_id);