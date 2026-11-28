# V1 query language: rules beyond the schema

[query.schema.json](query.schema.json) checks the shape of each element. A query can match the schema and still be
wrong: its elements may not fit together, or may not fit the ontology. This page lists those rules. For each, it
gives an example and what the engine answers.

The examples use the Dragons ontology that ships with the distribution
([Dragons.json](../../../distribution/resources/assembly/Dragons/ontology/Dragons.json)).
Each one also shows its one-line text form, the output of `POST /fuse/query/v1/print`.

"`/run` answers" quotes the engine's reply to `POST /fuse/query/v1/run`, inside `data.errorDescription`. These
replies are checked in CI by `V1RulesTest` (fuse-dv-asg), which runs every example through the same steps as `/run`
up to planning. The examples also match the schema, and their text forms match the printer (`V1QuerySchemaTest`).

> `POST /fuse/query/v1/validate` checks less than `/run`, and for some broken queries it answers with an empty body
> ([KNOWN_ISSUES](../../revival/KNOWN_ISSUES.md) Q18). Run the query to get the error.

A correct query to start from:

<!-- engine: accepts -->
```json
{"ont": "Dragons", "elements": [
  {"eNum": 0, "type": "Start", "next": 1},
  {"eNum": 1, "type": "ETyped", "eTag": "A", "eType": "Person", "next": 2},
  {"eNum": 2, "type": "Rel", "rType": "Own", "dir": "R", "next": 3},
  {"eNum": 3, "type": "ETyped", "eTag": "B", "eType": "Dragon"}
]}
```
Text form: `start (A:Person) -[:Own]-> (B:Dragon)`

## 1. A path starts at `Start` and goes somewhere

Exactly one element is a `Start` (the schema checks this), and its `next` points at the first element to match.

<!-- engine: rejects "No Elements After Start Node" -->
```json
{"ont": "Dragons", "elements": [
  {"eNum": 0, "type": "Start"}
]}
```
Text form: `start`

## 2. Every reference points at an element

Every `next` and `b` names the `eNum` of an element in the query, and every `eNum` is used once.
`0`, `-1` or no value means "none". A reference to a missing element leaves that step with nothing attached:

<!-- engine: rejects "Ontology doesn't Allow Relation with No entity Attached" -->
```json
{"ont": "Dragons", "elements": [
  {"eNum": 0, "type": "Start", "next": 1},
  {"eNum": 1, "type": "ETyped", "eTag": "A", "eType": "Person", "next": 2},
  {"eNum": 2, "type": "Rel", "rType": "Own", "dir": "R", "next": 9},
  {"eNum": 3, "type": "ETyped", "eTag": "B", "eType": "Dragon"}
]}
```
Text form: `start (A:Person) -[:Own]-> <missing 9>`

A repeated `eNum` is not checked: the engine reads the query differently than intended and fails later, in planning
(`No valid plan was found`).

## 3. No cycles

Following `next` never comes back to an element already on the path. To match "A knows B and B knows A", use one tag
per entity, not a pointer back.

<!-- engine: rejects "StackOverflowError" -->
```json
{"ont": "Dragons", "elements": [
  {"eNum": 0, "type": "Start", "next": 1},
  {"eNum": 1, "type": "ETyped", "eTag": "A", "eType": "Person", "next": 2},
  {"eNum": 2, "type": "Rel", "rType": "Know", "dir": "R", "next": 3},
  {"eNum": 3, "type": "ETyped", "eTag": "B", "eType": "Person", "next": 4},
  {"eNum": 4, "type": "Rel", "rType": "Know", "dir": "R", "next": 1}
]}
```
Text form: `start (A:Person) -[:Know]-> (B:Person) -[:Know]-> <cycle 1>`

The engine has a check for this, but it crashes before reaching it: `/run` answers
`failed building the cursor request forwardPaths: java.lang.StackOverflowError`, and `/validate` answers HTTP 500
(KNOWN_ISSUES Q20).

## 4. Entities and relations alternate

Along a path, an entity is followed by a relation (or a quantifier, or properties), and a relation by an entity.

<!-- engine: rejects "Ontology Contains two adjacent Relations without Entity inside" -->
```json
{"ont": "Dragons", "elements": [
  {"eNum": 0, "type": "Start", "next": 1},
  {"eNum": 1, "type": "ETyped", "eTag": "A", "eType": "Person", "next": 2},
  {"eNum": 2, "type": "Rel", "rType": "Own", "dir": "R", "next": 3},
  {"eNum": 3, "type": "Rel", "rType": "Own", "dir": "R", "next": 4},
  {"eNum": 4, "type": "ETyped", "eTag": "B", "eType": "Dragon"}
]}
```
Text form: `start (A:Person) -[:Own]-> -[:Own]-> (B:Dragon)`

A relation must end at an entity:

<!-- engine: rejects "Ontology doesn't Allow Relation with No entity Attached" -->
```json
{"ont": "Dragons", "elements": [
  {"eNum": 0, "type": "Start", "next": 1},
  {"eNum": 1, "type": "ETyped", "eTag": "A", "eType": "Person", "next": 2},
  {"eNum": 2, "type": "Rel", "rType": "Own", "dir": "R"}
]}
```
Text form: `start (A:Person) -[:Own]->`

Two entities in a row are reported by `/validate` (`Ontology Contains two adjacent Entities without relation
inside`), but `/run` gets past the check and fails in planning (`No valid plan was found`).

## 5. The ontology exists, and has every type the query names

`ont` names a loaded ontology. Every `eType`, `rType`, `vTypes`/`nvTypes` entry and `pType` is a type of that
ontology. Names are case-sensitive.

<!-- engine: rejects "No Ontology Relation for value[Rides]" -->
```json
{"ont": "Dragons", "elements": [
  {"eNum": 0, "type": "Start", "next": 1},
  {"eNum": 1, "type": "ETyped", "eTag": "A", "eType": "Person", "next": 2},
  {"eNum": 2, "type": "Rel", "rType": "Rides", "dir": "R", "next": 3},
  {"eNum": 3, "type": "ETyped", "eTag": "B", "eType": "Horse"}
]}
```
Text form: `start (A:Person) -[:Rides]-> (B:Horse)`

An unknown entity type: `No Ontology entity for value[Wizard]`. An unknown ontology:
`No target Ontology field found for Hobbits`.

## 6. The ontology allows the relation between those entity types

Each relation type lists the pairs of entity types it connects, in its direction. `Fire` connects dragons only:

<!-- engine: rejects "Ontology doesn't Allow such Relation with Entities construct" -->
```json
{"ont": "Dragons", "elements": [
  {"eNum": 0, "type": "Start", "next": 1},
  {"eNum": 1, "type": "ETyped", "eTag": "A", "eType": "Person", "next": 2},
  {"eNum": 2, "type": "Rel", "rType": "Fire", "dir": "R", "next": 3},
  {"eNum": 3, "type": "ETyped", "eTag": "B", "eType": "Dragon"}
]}
```
Text form: `start (A:Person) -[:Fire]-> (B:Dragon)`

## 7. Properties belong to the element they filter

An `EProp` names a property of the entity before it; a `RelProp` a property of the relation whose `b` points at it.
Dragons have no `height`:

<!-- engine: rejects "Property type mismatch parent entity" -->
```json
{"ont": "Dragons", "elements": [
  {"eNum": 0, "type": "Start", "next": 1},
  {"eNum": 1, "type": "ETyped", "eTag": "A", "eType": "Dragon", "next": 2},
  {"eNum": 2, "type": "EProp", "pType": "height", "con": {"type": "Constraint", "op": "gt", "expr": 100}}
]}
```
Text form: `start (A:Dragon) {height > 100}`

`Own` has `startDate` and `endDate`, not `temperature`:

<!-- engine: rejects "Property type mismatch parent Relation" -->
```json
{"ont": "Dragons", "elements": [
  {"eNum": 0, "type": "Start", "next": 1},
  {"eNum": 1, "type": "ETyped", "eTag": "A", "eType": "Person", "next": 2},
  {"eNum": 2, "type": "Rel", "rType": "Own", "dir": "R", "next": 3, "b": 4},
  {"eNum": 3, "type": "ETyped", "eTag": "B", "eType": "Dragon"},
  {"eNum": 4, "type": "RelProp", "pType": "temperature", "con": {"type": "Constraint", "op": "gt", "expr": 100}}
]}
```
Text form: `start (A:Person) -[:Own {temperature > 100}]-> (B:Dragon)`

## 8. Values fit the property's type

`expr` must be readable as the property's ontology type: numbers for `int` and `float`, ISO-8601 strings for `date`.

<!-- engine: rejects "For input string" -->
```json
{"ont": "Dragons", "elements": [
  {"eNum": 0, "type": "Start", "next": 1},
  {"eNum": 1, "type": "ETyped", "eTag": "A", "eType": "Person", "next": 2},
  {"eNum": 2, "type": "EProp", "pType": "height", "con": {"type": "Constraint", "op": "gt", "expr": "tall"}}
]}
```
Text form: `start (A:Person) {height > "tall"}`

## 9. Tags

A tag (`eTag`) names an entity in the results. One tag means one entity type, and a tag doesn't start with `_` or
`$` (they are reserved).

<!-- engine: rejects "ETag A appears in more than one entity with a different type" -->
```json
{"ont": "Dragons", "elements": [
  {"eNum": 0, "type": "Start", "next": 1},
  {"eNum": 1, "type": "ETyped", "eTag": "A", "eType": "Person", "next": 2},
  {"eNum": 2, "type": "Rel", "rType": "Own", "dir": "R", "next": 3},
  {"eNum": 3, "type": "ETyped", "eTag": "A", "eType": "Dragon"}
]}
```
Text form: `start (A:Person) -[:Own]-> (A:Dragon)`

Repeating a tag on the same type passes the checks, but it asks for the same entity twice, which closes a cycle, and
the engine doesn't run that (`/run` answers `UnsupportedOperationException`; KNOWN_ISSUES Q6).

## 10. Where-by: compare with another entity's property

A `WhereByConstraint` compares a property with the same or another property of the entity tagged `tagEntity`. The
tag must exist, the field must be a property of the ontology, a query has at most one where-by, and `joinType` is
required (the schema checks that). `FOR_EACH` keeps a match when the comparison holds for at least one value,
`FULL` when it holds for all of them. Persons known by someone taller than 180 cm, and taller than at least one of
those people:

<!-- engine: accepts -->
```json
{"ont": "Dragons", "elements": [
  {"eNum": 0, "type": "Start", "next": 1},
  {"eNum": 1, "type": "ETyped", "eTag": "A", "eType": "Person", "next": 2},
  {"eNum": 2, "type": "Quant1", "qType": "all", "next": [3, 4]},
  {"eNum": 3, "type": "EProp", "pType": "height", "con": {"type": "Constraint", "op": "gt", "expr": 180}},
  {"eNum": 4, "type": "Rel", "rType": "Know", "dir": "R", "next": 5},
  {"eNum": 5, "type": "ETyped", "eTag": "B", "eType": "Person", "next": 6},
  {"eNum": 6, "type": "EProp", "pType": "height",
   "con": {"type": "WhereByConstraint", "op": "gt", "tagEntity": "A", "projectedField": "height", "joinType": "FOR_EACH"}}
]}
```
Text form: `start (A:Person) all{ {height > 180} | -[:Know]-> (B:Person) {height > any(A.height)} }`

A where-by on a missing tag:

<!-- engine: rejects "No value present" -->
```json
{"ont": "Dragons", "elements": [
  {"eNum": 0, "type": "Start", "next": 1},
  {"eNum": 1, "type": "ETyped", "eTag": "A", "eType": "Person", "next": 2},
  {"eNum": 2, "type": "EProp", "pType": "height",
   "con": {"type": "WhereByConstraint", "op": "gt", "tagEntity": "Z", "projectedField": "height", "joinType": "FOR_EACH"}}
]}
```
Text form: `start (A:Person) {height > any(Z.height)}`

## OR between property filters

`some` between property filters of one entity (an `EPropGroup` or `Quant1` with `some`) is OR, also between filters on
the same property (before 0.10 those were merged as AND: KNOWN_ISSUES Q16). Persons shorter than 165 cm or taller than
182 cm:

<!-- engine: accepts -->
```json
{"ont": "Dragons", "elements": [
  {"eNum": 0, "type": "Start", "next": 1},
  {"eNum": 1, "type": "ETyped", "eTag": "A", "eType": "Person", "next": 2},
  {"eNum": 2, "type": "Quant1", "qType": "some", "next": [3, 4]},
  {"eNum": 3, "type": "EProp", "pType": "height", "con": {"type": "Constraint", "op": "lt", "expr": 165}},
  {"eNum": 4, "type": "EProp", "pType": "height", "con": {"type": "Constraint", "op": "gt", "expr": 182}}
]}
```
Text form: `start (A:Person) some{ {height < 165} | {height > 182} }`

Branching the whole pattern, one copy of the entity per filter, gives the same answer:

<!-- engine: accepts -->
```json
{"ont": "Dragons", "elements": [
  {"eNum": 0, "type": "Start", "next": 1},
  {"eNum": 1, "type": "Quant1", "qType": "some", "next": [2, 4]},
  {"eNum": 2, "type": "ETyped", "eTag": "A", "eType": "Person", "next": 3},
  {"eNum": 3, "type": "EProp", "pType": "height", "con": {"type": "Constraint", "op": "lt", "expr": 165}},
  {"eNum": 4, "type": "ETyped", "eTag": "B", "eType": "Person", "next": 5},
  {"eNum": 5, "type": "EProp", "pType": "height", "con": {"type": "Constraint", "op": "gt", "expr": 182}}
]}
```
Text form: `start some{ (A:Person) {height < 165} | (B:Person) {height > 182} }`

`some` between relation branches works as OR.
