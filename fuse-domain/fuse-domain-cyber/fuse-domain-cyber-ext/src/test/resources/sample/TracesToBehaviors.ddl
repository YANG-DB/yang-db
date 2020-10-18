create table TracesToBehaviors
(
	insert_time timestamp  not null,
	customer_id int not null,
	source_id numeric(39) not null,
	trace_id int not null,
	trace_type int not null,
	behavior_id int not null,
	behavior_type_id int,
	by_uid BIGINT,
	by_name varchar(256),
	by_type_id int,
	by_linked_entity_uid BIGINT,
	by_file_md5 varchar(50),
	to_uid           BIGINT,
	to_name varchar(256),
	to_type_id int,
	to_linked_entity_uid                        BIGINT,
	to_file_md5 varchar(50),
	behavior_is_alert boolean default false,
	attributes_hash BIGINT default 0,

    -- Does this state this table is a relation ?
    CONSTRAINT pk_trace_behavior primary key (trace_id,behavior_id),

    CONSTRAINT fk_trace FOREIGN KEY (trace_id) REFERENCES Traces(trace_id),
    CONSTRAINT fk_trace_behavior FOREIGN KEY (behavior_id) REFERENCES Behaviors(behavior_id),
    CONSTRAINT fk_trace_behavior_types FOREIGN KEY (behavior_type_id) REFERENCES lov_BehaviorsTypes(type_id),
    CONSTRAINT fk_alrets_by_type_obj FOREIGN KEY (by_type_id) REFERENCES lov_CyberObjectTypes(type_id),
    CONSTRAINT fk_alrets_to_type_obj FOREIGN KEY (to_type_id) REFERENCES lov_CyberObjectTypes(type_id)



);

