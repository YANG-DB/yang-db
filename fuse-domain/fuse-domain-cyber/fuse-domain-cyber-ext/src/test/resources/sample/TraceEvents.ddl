-- auto-generated definition
create table TraceEvents
(
    trace_id             int,
    trace_type           int,
    behavior_id          int                           not null,
    behavior_type_id     int                           not null,
    customer_id          int                           not null,
    source_id            numeric(39),
    network_id           varchar(64),
    source_type          varchar(64),
    session_id           int,
    event_id             int                           not null,
    event_type_id        int                           not null,
    event_severity       int,
    event_detected_time  timestamp                     not null,
    by_uid BIGINT,
    by_name              varchar(256),
    by_type_id           int,
    by_linked_entity_uid BIGINT,
    by_file_type         int,
    by_file_md5          varchar(50),
    by_process_pid       int,
    by_file_directory    varchar(1024),
    by_sid               varchar(256),
    to_uid BIGINT,
    to_name              varchar(256),
    to_type_id           int,
    to_linked_entity_uid                        BIGINT,
    to_file_type         int,
    to_file_md5          varchar(50),
    to_process_pid       int,
    is_insight           boolean                       not null,
    insert_time          timestamp  not null,
    stage_id             int,

-- Does this state this table is a relation ?
    CONSTRAINT pk_trace_events primary key (trace_id,event_id),

    CONSTRAINT fk_trace_entities FOREIGN KEY (trace_id) REFERENCES Traces(trace_id),
    CONSTRAINT fk_trace_events FOREIGN KEY (event_id) REFERENCES EnrichmentEvents(event_id),
    CONSTRAINT fk_trace_behavior FOREIGN KEY (behavior_id) REFERENCES Behaviors(behavior_id),
    CONSTRAINT fk_trace_behavior_types FOREIGN KEY (behavior_type_id) REFERENCES lov_BehaviorsTypes(type_id),
    CONSTRAINT fk_trace_by_type_obj FOREIGN KEY (by_type_id) REFERENCES lov_CyberObjectTypes(type_id),
    CONSTRAINT fk_trace_to_type_obj FOREIGN KEY (to_type_id) REFERENCES lov_CyberObjectTypes(type_id)



);

