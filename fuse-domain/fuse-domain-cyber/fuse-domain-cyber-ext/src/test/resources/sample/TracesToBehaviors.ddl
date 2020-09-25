create table TracesToBehaviors
(
	insert_time timestamp default "sysdate"() not null,
	customer_id int not null,
	source_id numeric(39) not null,
	trace_id int not null,
	trace_type int not null,
	behavior_id int not null,
	behavior_type_id int,
	by_uid int,
	by_name varchar(256),
	by_type_id int,
	by_linked_entity_uid int,
	by_file_md5 varchar(50),
	to_uid int,
	to_name varchar(256),
	to_type_id int,
	to_linked_entity_uid int,
	to_file_md5 varchar(50),
	behavior_is_alert boolean default false,
	attributes_hash numeric(38) default 0
);

