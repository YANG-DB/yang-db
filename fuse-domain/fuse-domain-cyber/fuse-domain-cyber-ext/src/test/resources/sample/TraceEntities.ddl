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
    insert_time timestamp not null,
    update_time timestamp,

-- Does this state this table is a relation ?
    CONSTRAINT pk_trace_entities primary key (trace_id,entity_id),

    CONSTRAINT fk_traces FOREIGN KEY (trace_id) REFERENCES Traces(trace_id),
    CONSTRAINT fk_trace_behavior FOREIGN KEY (behavior_id) REFERENCES Behaviors(behavior_id),
    CONSTRAINT fk_trace_behavior_types FOREIGN KEY (behavior_type_id) REFERENCES lov_BehaviorsTypes(type_id),
    CONSTRAINT fk_trace_entities FOREIGN KEY (entity_id) REFERENCES Entities(entity_hash)

);

