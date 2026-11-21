# bugfix-K — Q9 / Q10 (Cypher relationship patterns)

## Goal
Q10: every `{k: v}` in a relationship pattern becomes a RelProp in the rel's RelPropGroup (named + unnamed).
Q9: reject `[r:A|B]` with "Unsupported Cypher syntax".

## Tasks
- [x] failing fuse-asg tests (CypherRelationshipPropertiesTranslatorTest, CypherRelationshipTypeUnionTranslatorTest)
- [x] Q10 fix + M2 pipeline test (CypherRelationshipPropertiesM2Test, incl. AsgQueryValidator)
- [x] Q9 fix
- [x] full fuse-asg (299, 0F, 26 skipped) + fuse-dv-asg (33, 0F) green; commits 7a519b1a3 (Q10), 368345ca6 (Q9)

## Status
Q9/Q10 merged. Q12 (node-pattern sibling) committed f590f863f; full suites green (fuse-asg 304, fuse-dv-asg 35).
Q12 merged. C1 second half committed a9a4def9e. Next step: coordinator cherry-picks a9a4def9e; e2e R1 verifies live.

## Discoveries
- Coordinator: single named prop worked live; unnamed crashed (None.get), multi-prop dropped all but the first.
- NodePatternCypherTranslatorStrategy.addProps had the same bug for node patterns — fixed as Q12.
- DefaultSelectionAsgStrategy adds projection-only EProps (con null / proj set); filter them when asserting constraints after M2.
- Pattern properties go through EqualityExpression, which only handles literals; `{k: $param}` is unchanged.
- Q11 (rel property + end-node filter) not investigated.

## Log
- reset to origin/phase3-structural-assertions (d709e0887)
- tests failed before: None.get x2, multi-prop kept only timestamp, Q9 no error x2
- Q10 + Q9 fixed; full suites green
- Q12: tests failed before (None.get x3 fuse-asg + x1 M2, multi-prop kept only gender); fixed, f590f863f
- C1 (count = / <>): tests failed before (no RelPropGroup on k; M2 countOps [] instead of [eq]/[ne]); fixed a9a4def9e; fuse-asg 307, fuse-dv-asg 38, 0 failures
