# V1 query language: specification

V1 is YangDB's own graph query language. A query is a JSON document posted to `POST /fuse/query/v1`
(see [api.md](../../info/api.md)). This folder holds its specification for other tools: client libraries,
editors, linters, and translators.

| File | What it is |
|---|---|
| [query.schema.json](query.schema.json) | JSON Schema (draft 2020-12) of a V1 query: the wire format. The engine serves it at `GET /fuse/query/v1/schema`. |
| [rules.md](rules.md) | The rules a schema can't check (references, cycles, the ontology), each with an example and the engine's answer. |

The schema is tested against every V1 query in the repo's test fixtures, the examples in the docs and the
OpenAPI file, and the engine's own serializer output
([V1QuerySchemaTest](../../../fuse-model/src/test/java/com/yangdb/fuse/model/V1QuerySchemaTest.java)), so a change
to the query model that breaks the schema fails the build. The examples in rules.md run through the engine's
checks in CI ([V1RulesTest](../../../fuse-dv/fuse-dv-asg/src/test/java/com/yangdb/fuse/asg/V1RulesTest.java)).

## Using the schema

Validate a query before sending it, for example with Python:

```python
import json, jsonschema
schema = json.load(open("docs/spec/v1/query.schema.json"))
jsonschema.Draft202012Validator(schema).validate(json.load(open("my-query.json")))
```

Or generate types from it (e.g. `quicktype`, `datamodel-code-generator`, `json-schema-to-typescript`). A running
engine serves the copy that matches its version: `curl http://localhost:8888/fuse/query/v1/schema`.

## Shape of a query

```json
{
  "ont": "Dragons",
  "name": "people who own a dragon",
  "elements": [
    { "eNum": 0, "type": "Start",  "next": 1 },
    { "eNum": 1, "type": "ETyped", "eTag": "A", "eType": "Person", "next": 2 },
    { "eNum": 2, "type": "Rel",    "rType": "Own", "dir": "R", "next": 3 },
    { "eNum": 3, "type": "ETyped", "eTag": "B", "eType": "Dragon" }
  ]
}
```
Text form: `start (A:Person) -[:Own]-> (B:Dragon)`

`elements` is a flat list. Each element has a number (`eNum`) and points to what follows with `next`
(one eNum, or a list of eNums on a `Quant1`), and to the properties that filter a relation with `b`.
Read together, the pointers form a tree rooted at `Start`.

## Core and extension types

The **core** types are specified in full, and the schema rejects fields they do not define:

| Group | Types |
|---|---|
| Root | `Start` |
| Entities | `ETyped` (any entity of a type), `EConcrete` (one entity by id), `EUntyped` (any of several types) |
| Relations | `Rel`, `RelPattern` (a path of `length.lower`..`length.upper` hops), `RelUntyped` (any of the types in `vTypes`) |
| Branching | `Quant1` (combine branches: `all` = AND, `some` = OR, ...), `OptionalComp` (optional branch) |
| Properties | `EProp`, `EPropGroup`, `RelProp`, `RelPropGroup` |
| Constraints | `Constraint` (`op` + `expr`), `WhereByConstraint` (compare with another entity's property) |
| Projections | `Identity` |

The other type names the engine accepts (`CountComp`, `InnerQueryConstraint`, ...) are **extension** types: the
schema accepts them with any fields, and does not specify them. `HQuant` is one of them and is deprecated: use a
`RelPropGroup`. Type names the engine does not know are rejected, as the engine rejects them.

Known gaps, with workarounds in [rules.md](rules.md) and details in
[KNOWN_ISSUES](../../revival/KNOWN_ISSUES.md):
- `some` between property filters of one entity behaves as `all` (Q16); `some` between relation branches works.
- `RelUntyped` needs at least one type in `vTypes` (Q19), and `WhereByConstraint` needs `joinType` (Q17). The
  schema requires both.

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

Some rules relate elements to each other or to the ontology, so a JSON Schema can't check them: references point
at elements, no cycles, entities and relations alternate, types exist in the ontology, properties belong to their
element, values fit the property's type, and tag rules. [rules.md](rules.md) lists them, each with an example and
what the engine answers.

## Text form

`POST /fuse/query/v1/print` (and `V1TextPrinter` in fuse-model) prints a query as one line, for logs and docs. It is
for reading only: there is no parser. The notation:

| V1 | Text |
|---|---|
| `Start` | `start` |
| `ETyped`, `EConcrete`, `EUntyped` | `(A:Person)`, `(A:Person #"12345")`, `(A:Dragon\|Horse !Person)`, `(A:*)` |
| `Rel` with `dir` R, L, RL | `-[:Own]->`, `<-[:Own]-`, `-[:Own]-`; a tag goes before the colon: `-[o:Own]->` |
| `RelPattern`, `RelUntyped` | `-[:Know*1..3]->`, `-[:Own\|MemberOf]->` |
| relation filters (`b`) | inside the brackets: `-[:Own {startDate > "2000-01-01"}]->` |
| `Quant1`, `OptionalComp` | `all{ branch \| branch }`, `some{ ... }`, `optional{ ... }` |
| `EProp`, `EPropGroup` | `{height > 180}`, `{a, b}` (all), `some{a, b}`; a projection is the bare name: `{height}` |
| operators | `=` `!=` `>` `>=` `<` `<=`, others by name: `name contains "x"`, `name empty`, `height in range [1, 5)` |
| `WhereByConstraint` | `height > any(A.height)` (`FOR_EACH`), `height > all(A.height)` (`FULL`) |
| problems | `<missing 9>` (a reference to no element), `<cycle 1>`, `<HQuant>` (a type outside the core) |

Type sets are printed sorted. Not printed: `eName`, `pTag`, `wrapper`, and the `expr` of a `WhereByConstraint`.
