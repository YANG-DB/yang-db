# V1 query language: specification

V1 is YangDB's own graph query language. A query is a JSON document posted to `POST /fuse/query/v1`
(see [api.md](../../info/api.md)). This folder holds its specification for other tools: client libraries,
editors, linters, and translators.

| File | What it is |
|---|---|
| [query.schema.json](query.schema.json) | JSON Schema (draft 2020-12) of a V1 query: the wire format. |

The schema is tested against every V1 query in the repo's test fixtures, the examples in the docs and the
Swagger file, and the engine's own serializer output
([V1QuerySchemaTest](../../../fuse-model/src/test/java/com/yangdb/fuse/model/V1QuerySchemaTest.java)), so a change
to the query model that breaks the schema fails the build.

## Using the schema

Validate a query before sending it, for example with Python:

```python
import json, jsonschema
schema = json.load(open("docs/spec/v1/query.schema.json"))
jsonschema.Draft202012Validator(schema).validate(json.load(open("my-query.json")))
```

Or generate types from it (e.g. `quicktype`, `datamodel-code-generator`, `json-schema-to-typescript`).

## Shape of a query

```json
{
  "ont": "Dragons",
  "name": "people who own a dragon",
  "elements": [
    { "eNum": 0, "type": "Start",  "next": 1 },
    { "eNum": 1, "type": "ETyped", "eTag": "A", "eType": "Person", "next": 2 },
    { "eNum": 2, "type": "Rel",    "rType": "own", "dir": "R", "next": 3 },
    { "eNum": 3, "type": "ETyped", "eTag": "B", "eType": "Dragon" }
  ]
}
```

`elements` is a flat list. Each element has a number (`eNum`) and points to what follows with `next`
(one eNum, or a list of eNums on a `Quant1`), and to the properties that filter a relation with `b`.
Read together, the pointers form a tree rooted at `Start`.

## Core and extension types

The **core** types are specified in full, and the schema rejects fields they do not define:

| Group | Types |
|---|---|
| Root | `Start` |
| Entities | `ETyped` (any entity of a type), `EConcrete` (one entity by id), `EUntyped` (any of several types) |
| Relations | `Rel`, `RelPattern` (a path of `length.lower`..`length.upper` hops) |
| Branching | `Quant1` (combine branches: `all` = AND, `some` = OR, ...), `OptionalComp` (optional branch) |
| Properties | `EProp`, `EPropGroup`, `RelProp`, `RelPropGroup` |
| Constraints | `Constraint` (`op` + `expr`) |
| Projections | `Identity` |

The other type names the engine accepts (`HQuant`, `RelUntyped`, `CountComp`, `WhereByConstraint`,
`InnerQueryConstraint`, ...) are **extension** types: the schema accepts them with any fields, and does not
specify them. Type names the engine does not know are rejected, as the engine rejects them.

## Where the schema is stricter than the engine

The engine ignores fields it does not know, so a misspelled field (`"eTyp"`) is silently dropped and the query
means something else. The schema rejects unknown fields on core types. It also requires type names to be
strings (the engine turns a number into a string).

Fields the engine writes in its own output (`typed`, `parentTyped`, `constraint`, `projection`, and `qType` on
`OptionalComp`) are allowed so that engine output validates, but they are ignored on input.

## Constraint values

How many values `expr` holds depends on `op`. The schema checks the same rules as the engine
(`AsgConstraintExpressionValidatorStrategy`):

| `op` | `expr` |
|---|---|
| `empty`, `not empty` | none |
| `eq` `ne` `gt` `ge` `lt` `le` `contains` `not contains` `starts with` `not starts with` `not ends with` `fuzzy eq` `fuzzy ne` `match` `match_phrase` `not match` `query_string` | one value, not a list |
| `in range`, `not in range` | a list of exactly two values; `iType` says which ends are included: `[]` (default), `[)`, `(]`, `()` |
| `in set`, `not in set`, `like any` | a list |
| `ends with`, `like`, `within`, `distinct` | not checked by the engine, so not by the schema |

The value's JSON type should match the property's type in the ontology (dates as ISO-8601 strings). That depends
on the ontology, so the schema cannot check it.

## Rules the schema cannot express

These rules relate elements to each other or to the ontology. A JSON Schema cannot check them, and the engine
checks them when the query runs:

1. Every `eNum` is unique, and every `next` and `b` points at an `eNum` in the query.
   `0` and `-1` mean "none".
2. Exactly one `Start` (the schema checks this), and nothing points back at it.
3. No cycles: the pointers form a tree (`AsgCycleValidatorStrategy`).
4. Entities and relations alternate along a path: no two entities, or two relations, in a row
   (`AsgStepsValidatorStrategy`).
5. `eTag`s are unique (`AsgEntityDuplicateETagValidatorStrategy`).
6. `ont` names a loaded ontology, and every `eType`, `rType` and `pType` exists in it, with the relation allowed
   between those entity types (`AsgOntology*ValidatorStrategy`, `AsgStartEntityValidatorStrategy`).
