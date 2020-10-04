-- auto-generated definition
create table AlertsToBehaviors
(
    insert_time          timestamp    not null,
    customer_id          int                             not null,
    source_id            numeric(39)                     not null,
    trace_id             int                             not null,
    trace_type           int                             not null,
    behavior_id          int                             not null,
    behavior_type_id     int,
    by_uid               int,
    by_name              varchar(256),
    by_type_id           int,
    by_linked_entity_uid int,
    by_file_md5          varchar(50),
    to_uid               int,
    to_name              varchar(256),
    to_type_id           int,
    to_linked_entity_uid int,
    to_file_md5          varchar(50),
    behavior_is_alert    boolean     default false,
    attributes_hash      numeric(38) default 0,

-- missing alert ID here
    CONSTRAINT pk_alerts_behaviors primary key (behavior_id) ,

    CONSTRAINT fk_alerts_behaviors FOREIGN KEY (behavior_id) REFERENCES Behaviors( behavior_id),
    CONSTRAINT fk_alerts_behavior_types FOREIGN KEY (behavior_type_id) REFERENCES lov_BehaviorsTypes(type_id),
    CONSTRAINT fk_alrets_by_type_obj FOREIGN KEY (by_type_id) REFERENCES lov_CyberObjectTypes(type_id),
    CONSTRAINT fk_alrets_to_type_obj FOREIGN KEY (to_type_id) REFERENCES lov_CyberObjectTypes(type_id),
    CONSTRAINT fk_alerts_traces FOREIGN KEY (trace_id) REFERENCES Traces( trace_id)



);

