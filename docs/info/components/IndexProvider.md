## Index Provider Explained
Index Provider purpose is to define a low level schematic structure for the underlying physical store

The store will implement the index provider instructions according to its store architecture and capabilities

## Elasticseach Store
The default index store we provide with YangDB is the Elasticsearch database. Elasticsearch is a Index store with the capability
to elastic expand its cluster according to dynamic needs. 

### Indexing Policy
Elasticsearch offers the index data structure as its main indexing facility that represents a document class (in general
this is a virtual concept which has diverse meaning over the years - see elastic documentation
 - https://www.elastic.co/blog/index-vs-type
 - https://www.elastic.co/guide/en/elasticsearch/reference/6.0/removal-of-types.html 
 - https://medium.com/@mena.meseha/understand-the-parent-child-relationship-in-elasticsearch-3c9a5a57f202)
 
 
We can clearly define the notion of entity that is coupled with the notion of an index, this notion can be expanded using
the different tools available on YangDb such as: 
 - Inheritance - Type that inherits fields from a parent Type
 - Redundancy  - Type that contains redundant fields from other index (mostly a relation index)
 - Embedding   - Type that holds an embedded type (single instance) that appear as a property field inside the entity
 - Nested      - Type that holds a nested type (possible for list of nexted elements) that appear as a property field/s inside the entity
 - Unified     - Type that is only used as a logical reference but all its storage is done inside a single unified index
 - Partition   - Type that is partitioned according to some field (mostly time based) and is storde acros multiple partitions indices
 
 These Abilities allow the diversity and ability to reflect different capabilities and logical complexities inside the relativly simple
 schematic structure of elasticsearch so that all the functional and non-functional requeirments will be fulfilled. 
 
 -------------

 ### Entities & Relations
 YangDB logical ontology allows the definition of three different categories:
  - entities
  - relationships
  - enumerations
  
  #### Entities  
  Entities are a logical structure that represents an entity with its attributes. An entity may hold primitive data types such as
  integer fields, date fields, text fields and so...
  
  
  Entity also may contain enumerated dictionary or even a subtype entity which is embedded inside the entity structure.
  
  Example entity:
    
      Person: {
             id:  string
             name: string
             age:  int
             birth: date
             location: geo
             status: bool
             gender: gender_enum
             profession: Profession
           }
     
     Profession: {
            name: string
            description: text
            since: date 
        }
        
We can observe that Person has both simple primitive fields such as string, date, int but also complex fields such as the 
gender enumeration, the propession type fields and the geo location struct.

An Entity can also inherit from a parent entity and will contain the parent attributes in addition to its own attributes

Attributes with the same name will be overidden by the lower hirarchy.
   
   Example Student entity with additional fields:
     
     Student:: Person {
        university: string
        academicFields: string
        started:  date
        avg: float
     }             

-------------

### Schema Store
The 'Elasticsearch' index provider offers the next possibilities to store Person / Student entities : 
  
#### Entities  
##### Static  
  Direct mapping of the entity type to a single index     
   
   
       {
           "type": "Person",
           "partition": "static",  => shcematic mapping type
           "mapping": "Index",      
           "props": {
             "values": ["person"]  => this is the name of the physical E/S index
           },
           "nested": [
             {
               "type": "Profession",  => this is the inner type belonging to the person entity
               "mapping": "child",    => inner type store as an embedded entity (other option in nested) 
               "partition": "nested",
               "props": {
                 "values": ["profession"]   => this is the name of the physical E/S index
               }
             }
           ]
         }             
  
  ##### Partitioned 
  Mapping of the entity type to a multiple indices where each index is called after some partitioned based field
  
         {
           "type": "Person",    => this is the name of the physical E/S index
           "partition": "time", => this is the partitioning type of the index
           "mapping":"Index",
           "symmetric":true
           ],
           "props": {
             "partition.field": "birth", => the partitioned field
             "prefix": "idx_person",     => the inedx common name
             "index.format": "idx_person_%s", => the incremental index naming pattern
             "date.format": "YYYY",           => the date format for the naming pattern
             "values": ["1900", "1950", "2000","2050"] => the indices incremental time buckets
           }
         }

  ##### Unified 
  Mapping the entity type to a universal single index containing all the schematic structure
  
    {
      "type": "Person",
      "partition": "unified", => shcematic mapping type
      "mapping": "Index",
      "props": {
        "values": ["ontology"] => the unified index name
      },
      "nested": [
        {
          "type": "Profession",
          "mapping": "child",
          "partition": "nested",
          "props": {
            "values": ["ontology"]
          }
        }
      ]
    }

  ##### Nested 
  Mapping the entity (sub)type to an index containing as an embedded/nested document 
  
  In our example the Profession is the nested entity here ...
  
    {
      "type": "Person",
      "partition": "unified", => shcematic mapping type
      "mapping": "Index",
      "props": {
        "values": ["ontology"] => the unified index name
      },
      "nested": [
        {
          "type": "Profession",
          "mapping": "child", => "child" represents nested and "embedded" represents embedding the document insde the index
          "partition": "nested",
          "props": {
            "values": ["ontology"]
          }
        }
      ]
    }

-------------

  #### Relationships  
  Entities are a logical structure that represents a relationship between two entity with its attributes. A relationship may hold primitive data types such as
  integer fields, date fields, text fields and so...
  
  ##### Redundancy
  Redundancy is the ability to store redundant data on the relationship element that represents the information residing on the side(s) of the relation.
  
  Example:
  Call relationship type (between the person entity) 
     SideA - is the left Side of the relationship - a Person in our case
     SideB - is the right Side of the relationship - a Person in our case
  
      {
        "type": "Call",
        "partition": "time", 
        "mapping":"Index",
        "symmetric":true,
        "redundant": [  => this section states which fields of the related entities are stored on the relation itself
          {
            "side":["entityA","entityB"], => indicate the side that the fields are taken from
            "redundant_name": "name",     => the field redundant name - in the relation index 
            "name": "name",               => the field original name - in the entity index
            "type": "string"              => the field type
          },
          {
            "side":["entityA","entityB"],
            "redundant_name": "color",
            "name": "color",
            "type": "string"
          }
        ],
        "props": {
          "partition.field": "date",
          "prefix": "idx_call",
          "index.format": "idx_call_%s",
          "date.format": "YYYY",
          "values": ["2001", "2002", "2003","2004"]
        }
      },

-------------
 
 ### API
 The index provider json structure is usaged to create a coresponding schema structure (template)
 
 On YangDb we will call the next API that will generate the coresponding Index templates
  
  - https://blog.ruanbekker.com/blog/2019/04/06/elasticsearch-templates-tutorial/
  
  Call the next API: 
  
    Create the schematic mapping (index template) for the schema: 
    
      [POST] http://localhost:8888/fuse/load/ontology/:id/mapping
            id => ontology Name
            Body => IndexProvider json document
             

    Create the schematic indices (including index template) for the schema: 

      [POST] http://localhost:8888/fuse/load/ontology/:id/indices
            id => ontology Name
            Body => IndexProvider json document
             
 **For additional API - see swagger reference**