-- auto-generated definition
create table lov_EventsTypes
(
    type_id     int         primary key,
    title       varchar(100),
    is_external boolean,
    type_name   varchar(128),
    group_name  varchar(128),
    copy_path   varchar(512),
    move_path   varchar(512)
);

