> **Start here:** the [architecture tutorial](../../../docs/tutorial/architecture/README.md) explains every module and follows one query through all its translations, using the current code. This page is older background material.

> **Spec:** the V1 wire format is published as a JSON Schema in [docs/spec/v1](../../spec/v1/README.md).

Elements of the query language
-------------

### Entity Types
![Entity](https://raw.githubusercontent.com/LiorKogan/V1/master/Pictures/BB01.png)

A node can be one of the next:

* **CONCRETE ENTITY** – Specific entity in the graph
* **TYPED ENTITY** – A subclass of typed entity category / categories
* **UNTYPED ENTITY** – Entity of Un specified type

### Relationship

A logical relationship type is defined by a pattern:

* Two typed/untyped entities in the pattern
* A relationship type name assigned to each such relationship
* A logical relationship type can be either directional or bidirectional.

![](https://raw.githubusercontent.com/LiorKogan/V1/master/Pictures/Q003-1.png)

### Constraints
A filter on entities or relationships which refer to non-concrete entities.

![](https://raw.githubusercontent.com/LiorKogan/V1/master/Pictures/BB09-01.png)


### Data Types, Operators, and Functions
V1 supports the following primitive data types:

* Integer types (`int`)
* Real types (floating-point, `float`)
* String (`string`, and full-text `text`)
* Date (`date`)
* datetime (`datetime`)
* Geo point (`geo_point`)

### Quantifiers

* Quantifier can be used when there is a need to satisfy more than one constraint.
* Quantifier can combine both constraints and relations

![](https://raw.githubusercontent.com/LiorKogan/V1/master/Pictures/Q003-2.png)

### V1 Vocabulary:
_Start_ – The query start node

#### Entities
Entity can belong to one of the next categories:	

* EUntyped – Untyped Entity
* ETyped –Typed Entity
* Has list of possible types
* EConcrete – Concrete Entity Type
* Has a concrete id

#### Relationships
Relationship has direction & type (Can support untyped relations)

* Rel – Relationship

#### Properties
Every relation & entity can have properties.

* RelProp – Relationship property
* EProp – Entity property

Properties have name, type (data type) & constraint:

* RelPropGroup – Relationship property group
* EPropGroup – Entity property group

#### Constraint

Constraint is combined of an operator and an expression.

A constraint filters assignment to only those assignments for which the value of the expression for the assigned entity/relationship satisfies the constraint.

#### Start node
 Every query begin element, each query element number appears in the rectangle brackets.
 
 Quantifiers sub-elements appear in the curly brackets.

Example
-------

Query as a json document:

```
{
  "name": "Q1",
  "ont": "Dragons",
  "elements": [
    {
      "eNum": 0,
      "type": "Start",
      "next": 1
    },
    {
      "eNum": 1,
      "type": "EConcrete",
      "eTag": "A",
      "eID": "12345678",
      "eType": "Person",
      "eName": "Brandon Stark",
      "next": 2
    },
    {
      "eNum": 2,
      "type": "Rel",
      "rType": "Own",
      "dir": "R",
      "next": 3
    },
    {
      "eNum": 3,
      "type": "ETyped",
      "eTag": "B",
      "eType": "Dragon"
    }
  ]
}
```

Additional simple string representation:
```
    Start [0]: EConcrete [1]: Rel [2]: ETyped [3]
```
Each v1 query has a name and a (linked) list of elements which are labeled with :
* eNum - sequence number
* eTag – a named tag used for results labeling

and names the ontology it runs against in `ont`.


Additional Query with quantifier and property constraint 
```
{
  "name": "Q10",
  "ont": "Dragons",
  "elements": [
    {
      "eNum": 0,
      "type": "Start",
      "next": 1
    },
    {
      "eNum": 1,
      "type": "ETyped",
      "eTag": "A",
      "eType": "Person",
      "next": 2
    },
    {
      "eNum": 2,
      "type": "Quant1",
      "qType": "all",
      "next": [
        3,
        4
      ]
    },
    {
      "eNum": 3,
      "type": "EProp",
      "pType": "firstName",
      "con": {
        "type": "Constraint",
        "op": "eq",
        "expr": "Brandon"
      }
    },
    {
      "eNum": 4,
      "type": "Rel",
      "rType": "Own",
      "dir": "R",
      "next": 5
    },
    {
      "eNum": 5,
      "type": "ETyped",
      "eTag": "B",
      "eType": "Dragon"
    }]
}
```
Additional simple string representation:

```
   Start [0]: ETyped [1]: Quant1 [2]:{3|4}: EProp [3]: Rel [4]: ETyped [5]
```
