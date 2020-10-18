-- auto-generated definition
create table BehaviorEntities
(
    rel_id             int       default nextval('APD.seq_BehaviorEntitiesIds'),
    behavior_id        int                           not null,
    behavior_frequency float,
    behavior_type_id   int                           not null,
    customer_id        int                           not null,
    source_id          numeric(39)                   not null,
    network_id         varchar(64),
    source_type        varchar(64),
    session_id         int,
    entity_id          int,
    md5                varchar(50),
    entity_hash        BIGINT,
    entity_name        varchar(256),
    entity_type_id     int,
    entity_file_type   int,
    is_linked_entity   boolean                       not null,
    is_in_merge        boolean                       not null,
    md5_hash           BIGINT,
    is_md5_null        int,
    insert_time        timestamp not null,
    stage_id           int,

    CONSTRAINT pk_behavior_entities primary key (behavior_id,entity_hash),
    CONSTRAINT fk_behavior_entities_behavior FOREIGN KEY (behavior_id) REFERENCES Behaviors(behavior_id),
    CONSTRAINT fk_behavior_entities_behavior_type FOREIGN KEY (behavior_type_id) REFERENCES lov_BehaviorsTypes(type_id),
    CONSTRAINT fk_behavior_entity_entity FOREIGN KEY (entity_hash) REFERENCES Entities(entity_hash),
    CONSTRAINT fk_behavior_entity_entity_type FOREIGN KEY (entity_type_id) REFERENCES lov_CyberObjectTypes(type_id)


);

