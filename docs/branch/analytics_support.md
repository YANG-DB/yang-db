Abstract: 

The purpose of this branch is to integrate the E/S transformer API into yandDb offering advance analytics 

**Details**

The transform API defines a transform, which copies data from source indices, transforms it, and persists it into an entity-centric destination index. The entities are defined by the set of group_by fields in the pivot object.
 
You can also think of the destination index as a two-dimensional tabular data structure (known as a data frame).

Every YangDb Entity/Relation has a logical representation in an ontology structure, We would like to allow the ability to group and collect common features from an entity/relation into a materialized index that will optimize certain aggregation query and will reflect some informative feature that is otherwise available only using query.

Lets review the next sample...

Node type Person with the following fields: 
 
 * id
 * name
 * gender
 * birth date
 * birth location
 * family status

We also have the following relation of type friend (person to person):

 * source Id
 * source Name
 * target Id
 * target Name
 * date Of Friendship
 * friendship description
 
Lets assume that we have an analytics that would like to express the following characteristics:

* amount of people having more than 100 friends gained recently (last month)
* amount of people born near-by (some radius) and are friends
* amount of single people (male/female) having fewer than 10 friends

These questions can be queried on demand but will drain memory and cause large resource spikes

Lets assume that we would like to query these questions often and would like to materialize them for performance considerations 

We can also take advantage of these materialized views in order to more efficiently join this information directly with standard yangDb queries... 
