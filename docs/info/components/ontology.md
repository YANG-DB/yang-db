#Ontology Structure
The purpose of this document is to explain and show example for the logical ontology (schema) structure that is used
for the definition of the entities and relationship representing the logical data model.

###Structure
The ontology schema is constructed from the next elements:

-------------

 #####Entities
  The basic block element that is used to query and traverse accross the data domain
  
       {
         "eType": "Person",
         "name": "Person",
         "idField": [
             "id"
         ]
         "mandatory": [
            "id"
         ],
         "properties": [
           "firstName",
           "lastName",
           "gender",
           "birthDate",
           "deathDate",
           "height",
           "name"
         ]
       }
       
   Every Entity has the following elements:
    
   - eType: representing the schematic (logical) type of entity
   - name: representing the schematic (logical) name of entity
   - idField: representing the id fields names of entity (resembels Primary Key constraint)
   - mandatory: representing the mandatory fields names of entity
   - properties: representing the properties names of entity
   
-------------
    
 #####Relationships
  The basic relation between the entities that is used to query and traverse accross the data domain
  
  Each relationship can be defined for numerous pair of entities ... 
 
        {
          "rType": "call",
          "name": "call",
          "directional": true,
          "idField": [
            "call_id",
            "time"
          ],
          "mandatory": [
            "call_id",
            "time"
          ],
          "ePairs": [
            {
              "name": "fk_call",
              "eTypeA": "person",
              "sideAIdField": "id",
              "eTypeB": "person",
              "sideBIdField": "id"
            }
          ],
          "properties": [
            "time",
            "phone",
            "network",
            "ISDN",
            "location"
          ]
        }
  
   - rType: representing the schematic (logical) type of relation
   - name: representing the schematic (logical) name of relation
   - direction: representing if the relation has direction
   - idField: representing the id fields names of relation (resembels Primary Key constraint)
   - mandatory: representing the mandatory fields names of relation

   - properties: representing the properties names of relation
   - ePairs: representing the two sides of the relation elements
        - name: the specific relationship pair name (resembels Foreign Key constraint)
        - eTypeA: the left entity type 
        - sideAIdField: the left entity id field name 
        - eTypeB: the right entity type 
        - sideBIdField: the right entity id field name 
         
-------------
       
 #####Properties
  The representation of fields belonging to a specific entity or relation
  
        {
          "pType": "id",
          "name": "id",
          "type": "string"
        }
        
   -pType: representing the type logical of the field 
   -name: representing the name of the field 
   -type: representing the schematic type of the field (primitive mostly)
  
  #####Basic available types:
   - int
   - string
   - text
   - float
   - geo
   - long
   - float
   - long
   - enum reference -> referencing the enum list
   - sub type -> referencing the entities list 
   
  
 #####Enumeration
  The dictionary of lost of predefined values that correspond to basic enumerated list
  
      {
        "eType": "TYPE_Color",
        "values": [
          {
            "val": 0,
            "name": "RED"
          },
          {
            "val": 1,
            "name": "BLUE"
          },
          {
            "val": 2,
            "name": "GREEN"
          },
          {
            "val": 3,
            "name": "YELLOW"
          }
        ]
      }

-------------

  
 ###API
 The ontology json is used almost in every aspect of the database, since YangDb can contain multiple logical
 ontologies (in addition to multiple physical schemas) the api allows adding of new ontologies...
 
     Create a new ontology 
        
       [POST] http://localhost:8888/fuse/catalog/ontology
             Body => Ontology json document
  
     Get All ontologies
     
       [GET] http://localhost:8888/fuse/catalog/ontology

     Get specific ontology by name

       [GET] http://localhost:8888/fuse/catalog/ontology/:id
           id => ontology Name


**For additional API - see swagger reference**