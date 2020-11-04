-- dictionary table
create table lov_BehaviorsTypes
(
    type_id               int         primary key,
    classification_id     int,
    alert_classification  boolean,
    title                 varchar(100),
    workflow_name         varchar(128),
    is_external           boolean,
    description           varchar(500),
    graphvisibility_value int,
    category_value        int default 104
);

