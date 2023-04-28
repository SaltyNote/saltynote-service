# User Table
alter table user
    drop primary key;

alter table user
    drop column id;

alter table user
    change idx id bigint null;

alter table user
    add constraint user_pk
        primary key (id);

# Note Table
alter table note
    drop primary key;

alter table note
    drop column id;

alter table note
    change idx id bigint null;

alter table note
    add constraint note_pk
        primary key (id);

alter table note
    drop column user_id;

alter table note
    change user_idx user_id bigint null;


# Vault Table
alter table vault
    drop primary key;

alter table vault
    drop column id;

alter table vault
    change idx id bigint null;

alter table vault
    add constraint vault_pk
        primary key (id);

alter table vault
    drop column user_id;

alter table vault
    change user_idx user_id bigint null;

# Login History Table
alter table login_history
    drop column user_id;

alter table login_history
    change user_idx user_id bigint null;
