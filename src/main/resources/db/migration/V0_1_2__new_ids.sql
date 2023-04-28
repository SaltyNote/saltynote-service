alter table user
    add idx BIGINT null after id;

alter table note
    add idx BIGINT null after id;

alter table note
    add user_idx BIGINT null after user_id;

alter table vault
    add idx BIGINT null after id;

alter table vault
    add user_idx BIGINT null after user_id;

alter table login_history
    add user_idx BIGINT null after user_id;
