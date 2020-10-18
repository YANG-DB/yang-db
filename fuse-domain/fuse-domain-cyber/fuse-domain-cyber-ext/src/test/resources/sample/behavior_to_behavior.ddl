create table behavior_to_behavior
(
    customer_id      int,
    source_id        numeric(39),
    behavior_id      int                           not null,
    behavior_type_id int                           not null,
    attributes_hash  BIGINT                   not null,
    base_behavior_id int                           not null,
    is_alerted       boolean   default true        not null,
    insert_time      timestamp not null,

    -- add cyber obj FK
    CONSTRAINT pk_behavior2behavior primary key (base_behavior_id,behavior_id),

    CONSTRAINT fk_behavior2behavior_types FOREIGN KEY (behavior_type_id) REFERENCES lov_BehaviorsTypes(type_id),
    CONSTRAINT fk_behavior2behavior_source FOREIGN KEY (base_behavior_id) REFERENCES Behaviors(behavior_id),
    CONSTRAINT fk_behavior2behavior_dest FOREIGN KEY (behavior_id) REFERENCES Behaviors(behavior_id)

);

