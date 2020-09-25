create table TraceEntities
(
    trace_id int not null,
    trace_type int,
    behavior_id int not null,
    behavior_type_id int not null,
    customer_id int,
    source_id numeric(39),
    session_id int,
    entity_id int,
    md5 varchar(50),
    entity_hash int,
    entity_name varchar(256),
    entity_type_id int,
    entity_file_type int,
    is_linked_entity boolean not null,
    is_in_merge boolean not null,
    insert_time timestamp default "sysdate"() not null,
    update_time timestamp default "sysdate"()
);

