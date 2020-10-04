-- auto-generated definition
create table Behaviors
(
    behavior_id                int                           not null        primary key,
    behavior_hash              int,
    customer_id                int                           not null,
    source_id                  numeric(39)                   not null,
    network_id                 varchar(64),
    source_type                varchar(64),
    session_id                 int,
    lone_behavior              boolean,
    type_id                    int                           not null,
    frequency                  float,
    by_uid                     int,
    by_name                    varchar(256),
    by_type_id                 int,
    by_linked_entity_uid       int,
    by_is_linked               boolean,
    by_file_md5                varchar(50),
    to_uid                     int,
    to_name                    varchar(256),
    to_type_id                 int,
    to_linked_entity_uid       int,
    to_is_linked               boolean,
    to_file_md5                varchar(50),
    data_network_ratio         float,
    data_file_extension        varchar(32),
    data_operation_system_hash int,
    data_directory             varchar(1024),
    created_by_analysis_id     int,
    port                       int,
    to_process_pid             int,
    data_memory_functionName   varchar(256),
    insert_time                timestamp  not null,
    stage_id                   int,

    CONSTRAINT fk_behavior_types FOREIGN KEY (type_id) REFERENCES lov_BehaviorsTypes(type_id),
    CONSTRAINT fk_behavior_types FOREIGN KEY (type_id) REFERENCES lov_BehaviorsTypes(type_id),
    CONSTRAINT fk_behavior_by_type_obj FOREIGN KEY (by_type_id) REFERENCES lov_CyberObjectTypes(type_id),
    CONSTRAINT fk_behavior_to_type_obj FOREIGN KEY (to_type_id) REFERENCES lov_CyberObjectTypes(type_id)



);

