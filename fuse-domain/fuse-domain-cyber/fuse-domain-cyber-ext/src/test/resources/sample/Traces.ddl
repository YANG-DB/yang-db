create table Traces
(
    insert_time timestamp  not null,
    update_time timestamp  not null,
    customer_id int not null,
    source_id numeric(39) not null,
    trace_id int not null         primary key,
    trace_type int not null,
    trace_status int not null,
    closing_reason int,
    is_alert boolean not null,
    trace_severity float default 0,
    start_time timestamp,
    end_time timestamp,
    min_detected_time timestamp,
    max_detected_time timestamp,
    status_update_time timestamp,
    update_by varchar(32),
    distinct_alerting_behavior_count int default 0 not null,
    trace_identifier numeric(38) default Traces.trace_id,

    -- dictionary table
    CONSTRAINT fk_trace_to_type_obj FOREIGN KEY (trace_type) REFERENCES lov_CyberObjectTypes(type_id)

);

