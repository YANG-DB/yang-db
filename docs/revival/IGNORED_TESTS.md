# Legacy `@Ignore`d tests: triage (Phase 3, stream I)

Baseline: `main` at `e48e7ce09`. Source list: `git grep -n "^\s*@Ignore" -- '*/src/test/java/*.java'`, which finds 95 lines.

**Scope: 86 entries.** Nine lines were excluded because other streams own them:
- `OpensearchIndexProviderMappingFactoryIT` lines 40, 175, 344 and 455
- the four "Pre-existing ..." ignores in fuse-dv-epb: `EpbJoinSelectionTests:342`, `StatisticalCostEstimatorTests:186`,
  `BasicPatternCostEstimatorTest:121` and `EBaseStatisticsProviderIndicesTests:183`
- `EmbeddedNodeRunner:35`

Two notes on the exclusions:
- `OpensearchIndexProviderMappingFactoryIT` has a fifth `@Ignore`, at line 558. It is listed below and marked for the
  stream that owns that file.
- fuse-dv-epb has 10 ignores. The other 6 are in scope here.

**How "Ran" was done.** Each ignore was removed temporarily, and `@Test` was added where it had been commented out. The
classes were then run one module at a time with
`mvn -o test -pl <module> -am -Dtest='<classes>, !**/._*' -Dmaven.test.failure.ignore=true` on test ports 9290/9390.
The OpenSearch-backed `GraphInitiatorIT` ran through `TestSuiteIndexProviderSuite`, on the embedded node with those test
ports. Every temporary edit was reverted afterwards. "Not run" means one of the following:
- the test has no assertions, or has an infinite loop or sleep loop
- it needs port 9300
- it's a manual large-data generator
- it belongs to another stream's file

**What the categories mean here:**
- **OBSOLETE**: delete the test.
- **FIXABLE**: the test passes as it is, or needs only a small change in the test itself.
- **KEEP-DISABLED**: it needs a product feature or bug fix, or a large rewrite of the test.
- **UNKNOWN**: it fails and the root cause wasn't found within this triage's time budget.

## Summary

| Module | OBSOLETE | FIXABLE | KEEP-DISABLED | UNKNOWN | Total |
|---|---:|---:|---:|---:|---:|
| examples/dragons/datagen | 0 | 0 | 2 | 0 | 2 |
| fuse-asg | 0 | 4 | 24 | 1 | 29 |
| fuse-core | 0 | 3 | 2 | 0 | 5 |
| fuse-dv-core | 1 | 1 | 1 | 1 | 4 |
| fuse-dv-epb (in-scope 6) | 2 | 0 | 2 | 2 | 6 |
| fuse-dv-gta | 0 | 0 | 3 | 1 | 4 |
| fuse-dv-stat | 1 | 0 | 0 | 0 | 1 |
| fuse-dv-unipop | 6 | 0 | 18 | 0 | 24 |
| unipop-core | 11 | 0 | 0 | 0 | 11 |
| **Total** | **21** | **8** | **52** | **5** | **86** |

**Tests that pass once the `@Ignore` is removed, with no other change:**
- `OWLOntologyTranslatorTest`: 4 of 4 pass
- `AsgCompositeValidationStrategyTest`: 2 of 2 pass
- `SparqlSimpleSelectQueryTranslatorTest`: 3 methods that were disabled only by the class-level ignore pass:
  `testSimpleSelectVarsAndPatternTriplet`, `testSimpleSelectVarsAndMulriPatternTriplet` and
  `testSimpleSelectVarsAndMulriPatternTraversingGraphTriplet`
- `AsgRelPatternValidationStrategyTest`: 3 of 6 pass, but see that entry below

**Gotchas found during triage:**
- `AsgMappingStrategyTest` extends JUnit 3 `TestCase`. A method named `FIXME_test...` never runs, even with `@Test`.
- `AsgMappingStrategyTest:102` and `DefaultRelationSelectionAsgStrategyTest:44` have `@Test` commented out. Removing
  the `@Ignore` alone doesn't enable them.
- `ThinPathTest` contains `while(true)` loops. Never un-ignore it.

## Per-test triage

### examples/dragons/datagen
| File:line | Test | Category | Reason | Ran |
|---|---|---|---|---|
| `DragonsGeneratorTest.java:56` | `testDragonsDataGenerationMediumGraph` | KEEP-DISABLED | Manual data generator: 100k dragons, 10k persons. The small-graph variant already runs. Better as a CLI than a test. | not run (heavy) |
| `PersonsGeneratorTest.java:50` | `personsDataGenerationMassiveGraphTest` | KEEP-DISABLED | Manual data generator: 1M dragons, 100k persons. | not run (heavy) |

### fuse-asg
| File:line | Test | Category | Reason | Ran |
|---|---|---|---|---|
| `AsgInnerQueryCompositeTransformerTest.java:208` | `testTransformToMultiInnerComposite` | KEEP-DISABLED | Needs nested inner queries (more than one level), which aren't supported. Feature work, medium. | FAIL (`expected 0 but was 1`) |
| `strategy/mapping/AsgMappingStrategyTest.java:102` | `FIXME_testQ4TransformMapping` | FIXABLE | The expected string is stale: the actual output also includes the `Person_p01` endpoint that `findPathQuery(...d01...p01)` asks for, so the actual output looks right. Restore `@Test`, rename to `test...` (JUnit 3 class) and refresh the expected string. Small. | FAIL after rename (expected-string drift only) |
| `strategy/selection/DefaultRelationSelectionAsgStrategyTest.java:44` | `FIXME_testSelectionForRel` | UNKNOWN | `@Test` is commented out. With it restored, the `IdentityProjection` props for `own` are not found (line 76), although the strategy code looks like it should add them. Needs debugging. | FAIL (`:76`) |
| `strategy/type/RelationRangeAsgStrategyWithFiltersInRangTest.java:318` | `testUntypedToTypedWithQuantPropsAfterRelStrategyWithoutQuantsInPath` | KEEP-DISABLED | Real bug: with both a relProp and an end-pattern filter, validation fails with "No Parent Element found Asg(EPropGroup…)". Medium. | FAIL (validation error) |
| `strategy/type/UntypedRelationInferTypeStrategyTest.java:229` | same name | KEEP-DISABLED | "Under Development": inference doesn't type the untyped end. The expected string can't match anyway, because the query embeds `new Date()`. Feature plus a test redesign. | FAIL (different tree) |
| `strategy/validation/AsgCompositeValidationStrategyTest.java:29` | class | FIXABLE | Passes as it is. Remove the class `@Ignore`. Optionally delete the empty `//todo` stub methods. | **PASS 2/2** |
| `strategy/validation/AsgRelPatternValidationStrategyTest.java:32` | class | KEEP-DISABLED | `AsgRelPatternValidatorStrategy.apply` is an empty `//todo implement` stub, so the 3 "not valid" rel tests can't pass. The 2 passing `...QuantProp...` tests actually test `AsgEntityPropertiesValidatorStrategy`; move them there. Implementing the validator is medium. | 3/6 pass, 3 FAIL |
| `translator/cypher/CypherMatchGreaterThanEqualWithWhereOrOpLabelTranslatorTest.java:261` | `testMatch_A_OR_where_A_OfType_And_OR_B_OfType_AND_Return_` | KEEP-DISABLED | "todo add Rule for same variable with multiple operators": the translator splits the branches differently. Cypher feature, medium. | FAIL |
| `translator/cypher/CypherMatchHasLabelWithWhereOrOpLabelTranslatorTest.java:134` | `testMatch_A_where_A_OfType_B_eq_C_eq_dug_OR_A_OfType_Return_A_with_pattern` | FIXABLE | The output has the same structure as expected. Only element numbering and the rel tag differ (`Rel(:* c#5)` vs `Rel(:null Rel_#2#6)`). Refresh the expected value. Caveat: both expected and actual drop the rel property `{color:'white'}`, which is a separate translator gap worth a bug ID. | FAIL (numbering/tag drift) |
| same file`:569` | `testMatch_A_OR_where_A_OfType_And_OR_B_OfType_AND_Return_` | KEEP-DISABLED | Same missing "same variable, multiple operators" rule as `:261`. | FAIL |
| `translator/cypher/CypherMatchSimpleTranslatorTest.java:86` | `testMatch_A_ofRelType_Dragon_Person_Return_A` | KEEP-DISABLED | `[c:Fire\|Freeze]` multi-type edges aren't supported: the translator keeps only `Fire`, and the expected value (`null`) is wrong too. Cypher feature, medium. | FAIL |
| `translator/sparql/SparqlSimpleSelectQueryTranslatorTest.java:32` | class | FIXABLE | Remove the class `@Ignore` and add method `@Ignore`s to the 2 methods that only the class-level ignore was disabling: `testSimpleSelectVarsAndMulriPatternWithFilterTriplet` and `testFindCountries`. That gains 3 passing tests. | 3 PASS, 2 un-annotated FAIL |
| same file`:97, 220, 407, 496, 562, 637, 713, 774, 809, 904, 1029, 1140, 1190, 1217, 1245, 1272, 1327` (17 methods) | `testSelectPatternWithDistanceTriplet`, `testSimpleSelectAndProjectTriplet`, `...WithOptionalFilterTriplet`, `...MuseumFilterTriplet`, `...UnionInPropertiesTriplet`, `...UnionInStepsTriplet`, `testQueryNamedGraphs`, `testQueryFreeTextSearch`, `testQueryWithAggregation`, `testFindArtworkAndPerson`, `testSimpleSelectPersonWithSingleTriplet`, `testSimpleSelectAggregations`, `...PersonWithFriendTriplet`, `...DistinctPersonWithRelatedOtherSideTriplet`, `...MovieWithFilterTriplet`, `...MovieWithActorActedInFilterTriplet`, `...MovieWithCoAutorFilterTriplet` | KEEP-DISABLED (×17) | The SPARQL translator is incomplete. Failures are `FuseError` on unsupported operators (OPTIONAL, UNION, FILTER, GRAPH, aggregation), `MalformedQuery` on non-standard SPARQL in the test strings, or empty or `owl:Thing` output where the expected values assume a Knowledge (Entity/Evalue) ontology. Large: SPARQL is an experimental front end. | all FAIL (4 assertion, 13 `FuseError`/`MalformedQuery`) |

### fuse-core
| File:line | Test | Category | Reason | Ran |
|---|---|---|---|---|
| `dispatcher/query/rdf/OWLOntologyTranslatorTest.java:18` | class | FIXABLE | Passes as it is. Remove `@Ignore`. | **PASS 4/4** |
| `dispatcher/query/rdf/OWLPizzaOntologyTranslatorTest.java:20` | class | FIXABLE | The test bodies are copy-pasted placeholders that assert the *Social* ontology (`user#…`, 18 props, `countries` enum). The translator loads pizza.owl fine (74 entity types, 8 relationship types). Rewrite the 4 assertions against pizza.owl. Small. Note: `assertEquals` args are reversed in these files, so the failure messages read backwards. | FAIL 4/4 (placeholder assertions) |
| `dispatcher/query/rdf/OWLSIOCOntologyTranslatorTest.java:20` | class | FIXABLE | Same copy-paste placeholders. sioc.owl translates to 16 entity types, 27 properties and 69 relationship types. Rewrite the assertions. Small. | FAIL 4/4 (placeholder assertions) |
| `dispatcher/query/rdf/OWLUCSOntologyTranslatorTest.java:20` | class | KEEP-DISABLED | `OWLToOntologyTransformer.transform` throws `FuseError` in `setUp` on ucs.owl. A translator gap; effort unknown. | ERROR in `setUp` |
| `dispatcher/utils/CsvToRdfConverterTest.java:15` | `csvToRdf` | KEEP-DISABLED | `CsvToRdfConverter` is a stub that throws `NotImplementedException`. Implement the converter, or delete both the converter and the test. | ERROR (`NotImplemented`) |

### fuse-dv/fuse-dv-core
| File:line | Test | Category | Reason | Ran |
|---|---|---|---|---|
| `executor/ontology/schema/GraphInitiatorIT.java:52` | `testCreateIndices` | FIXABLE | Ran in `TestSuiteIndexProviderSuite` on the embedded node and returned 19 instead of 13. 19 is the suite's nested-schema index count, the same value `testSchema`, `testNestedInit` and `testNestedDrop` assert. Change the expected value to 19, after checking that the result doesn't depend on test order. | FAIL (`expected 13 but was 19`) |
| `executor/opensearch/OpensearchIndexProviderMappingFactoryIT.java:558` | `createSingleIndicesTest` | KEEP-DISABLED | "fix template pattern naming". This file belongs to another stream (the mapping-factory fixtures); leave the decision to that stream. | not run |
| `executor/opensearch/TimeoutAdvisorClientTests.java:13` | class | OBSOLETE | The assertions are vacuous: `testClientTimeout` passes on every path, and `testClientNoTimeout` fails because the mock client never delays. The ignore reason names `ElasticIndexProviderMappingFactoryTests`, which no longer exists. If `TimeoutClientAdvisor` coverage is wanted, write a real test. | 1 PASS (vacuous), 1 FAIL |
| `executor/utils/FileUtilsTest.java:13` | class | UNKNOWN | Needs `soc-LiveJournal1.txt.zip`, which isn't in the repo (a large SNAP dataset), so it fails with an NPE before testing anything. Could become FIXABLE with a tiny generated gzip fixture and a recomputed split count. | ERROR (missing resource) |

### fuse-dv/fuse-dv-epb (in scope)
| File:line | Test | Category | Reason | Ran |
|---|---|---|---|---|
| `epb/plan/SmartEpbComplexQueries.java:281` | `testPathSelectionEConcreteComplexQuery` | KEEP-DISABLED | "Stats Double Bug": the planner picks a different plan from expected. Planner and statistics investigation, medium. | FAIL (plan mismatch) |
| `epb/plan/SmartEpbRedundancyTests.java:262` | `redundantPropertyPathSelectionTest` | KEEP-DISABLED | Product bug: `ClassCastException: ontology.Value cannot be cast to Comparable` when a range op (`ge`) is used on an enumerated `Value` in statistics. Small to medium. | ERROR (`ClassCast`) |
| `epb/plan/estimation/pattern/BasicPatternCostEstimatorTest.java:74` | `calculateEntityOnlyPattern` | OBSOLETE | Its own reason: "No more single op pattern". | ERROR (NPE) |
| `epb/plan/estimation/pattern/BasicPatternCostEstimatorWithStatisticsProviderTest.java:85` | `calculateEntityOnlyPattern` | OBSOLETE | Same as above. | ERROR (NPE) |
| `epb/plan/statistics/EBaseStatisticsProviderBasicTests.java:336` | `eTypedEnumeratedFieldTest` | UNKNOWN | `IllegalArgumentException: No Such ontology value present 3` for prop `"3"` with `Value(2,"male")`. Probably a stale ontology fixture, but not confirmed. | ERROR |
| same file`:350` | `eTypedEnumeratedSingleValueBucketFieldTest` | UNKNOWN | Same error as `:336`. | ERROR |

### fuse-dv/fuse-dv-gta
| File:line | Test | Category | Reason | Ran |
|---|---|---|---|---|
| `gta/strategy/discrete/UnionOpTranslationStrategyTest.java:45` | class | KEEP-DISABLED | "Still under construction": union translation is unfinished. Even the method that isn't individually ignored (`testUnion_none`) fails. Feature work, medium to large. | 3/3 FAIL |
| same file`:99` | `testUnion_One_Branches_One_Hierarchy` | KEEP-DISABLED | "fix expected": the actual traversal differs structurally, not just in numbering. | FAIL |
| same file`:148` | `testUnion_Two_Branches_One_Hierarchy` | KEEP-DISABLED | Throws `FuseError`. | ERROR |
| `gta/translation/discrete/M1ChainedPlanOpTraversalTranslatorTest.java:94` | `FIXME_test_concrete_rel_typed_Agg` | UNKNOWN | `ClassCastException: Quant1 -> EPropGroup`. The fixture builds `EntityFilterOp(element 3)` where element 3 is a quant, and the expected traversal ignores the `agg` element. Either a broken fixture or unsupported aggregation. | ERROR (`ClassCast`) |

### fuse-dv/fuse-dv-stat
| File:line | Test | Category | Reason | Ran |
|---|---|---|---|---|
| `stat/DemoStatTest.java:17` | `runDemo` | OBSOLETE | Demo runner with no assertions. It points at a demo index on port 9300, and `StatCalculatorTest` already covers `StatCalculator.run`. When deleting it, also remove it from `StatTestSuite`. | not run (port 9300) |

### fuse-dv/fuse-dv-unipop
| File:line | Test | Category | Reason | Ran |
|---|---|---|---|---|
| `unipop/ElementControllerCompositTest.java:98` | `testSingleIdPromiseVertexWithLimit` | KEEP-DISABLED | The mock fixture is stale. Its hits carry `fields` but no `_source`, so `SearchHitPromiseVertexConverter` NPEs on `getSourceAsMap()`. It also asserts 10 vertices from a single mocked hit. Needs a test rewrite (about 2h); no product change. | FAIL |
| `unipop/controller/promise/appender/CompositeAllAppendersTest.java:47, 105, 176` | `testSimpleCompositeAppender`, `testCompositeAppender_No_Label_AND_Statement`, `testCompositeAppender_Label_OR_Statement` | OBSOLETE (×3) | Built around the `@Deprecated` `ElementGlobalTypeSearchAppender`, and they assert ES 2 `filtered`/`_type` query JSON. Mapping types no longer exist. | 3 FAIL |
| `unipop/controller/promise/appender/ElementGlobalTypeSearchAppenderTest.java:38` | `testSimpleConstraint` | OBSOLETE | Its own comment says "This appender is deprecated"; it asserts a `_type` terms filter. | ERROR (NPE) |
| `unipop/controller/promise/appender/PromiseEdgeIndexAppenderTest.java:47, 69, 90, 114, 138, 162, 186, 210, 234, 260, 286, 311, 336, 370, 405, 439, 473` | 17 × `appendTest_*` (static or time-series index selection by time constraints) | KEEP-DISABLED (×17) | `PromiseEdgeIndexAppender` is live code (used by `PromiseVertexController`), but the mock ontology and schema fixture is stale: all 17 throw `NoSuchElementException` from `getIndexPartitions().get()`. Making the mock relationship rTypes match the edge labels (they're both `"Fire"`) did not fix it. Needs a fixture rewrite against the current `OntologySchemaProvider`, about 0.5 day. Worth doing, because time-series partition pruning has no other unit coverage. | 17 ERROR |
| `unipop/process/traversal/traverser/ThinPathTest.java:15, 49` | `test1`, `test2` | OBSOLETE (×2) | Micro-benchmarks with no assertions that end in `while(true) sleep`. Never un-ignore them. | not run (infinite loop) |

### unipop-core
| File:line | Test | Category | Reason | Ran |
|---|---|---|---|---|
| `common/valueSuppliers/CompiledSupplierFactoryTests.java:13, 26, 39, 52, 65` | `test1`–`test5` | OBSOLETE (×5) | Print-and-sleep demos (20 × `Thread.sleep(1000)`) with no assertions. | not run |
| `common/valueSuppliers/FixedValueSupplierFactoryTests.java:13, 23` | `test1`, `test2` | OBSOLETE (×2) | Same. | not run |
| `common/valueSuppliers/LinearBufferedSupplierFactoryTests.java:13, 26` | `test1`, `test2` | OBSOLETE (×2) | Same. | not run |
| `common/valueSuppliers/LinearDecayingValueSupplierFactoryTests.java:13, 23` | `test1`, `test2` | OBSOLETE (×2) | Same. | not run |

## Recommended actions

1. **Delete 21 obsolete tests (about 790 lines).**
   - Whole files (709 lines):
     - the 4 `unipop-core/.../valueSuppliers/*Tests.java` files
     - `ThinPathTest`
     - `CompositeAllAppendersTest`
     - `ElementGlobalTypeSearchAppenderTest`
     - `DemoStatTest` (also remove it from `StatTestSuite`)
     - `TimeoutAdvisorClientTests`. Its helper `TestExecutorModule.java` has no other users and can go with it.
   - Methods (about 80 lines): both `calculateEntityOnlyPattern` methods in fuse-dv-epb.
2. **Re-enable now; these pass with no other change (+11 passing tests):**
   - `OWLOntologyTranslatorTest`: remove the class `@Ignore`
   - `AsgCompositeValidationStrategyTest`: remove the class `@Ignore`
   - `SparqlSimpleSelectQueryTranslatorTest`: remove the class `@Ignore` and add method `@Ignore`s to
     `testSimpleSelectVarsAndMulriPatternWithFilterTriplet` and `testFindCountries`
   - Move the 2 passing `AsgEntityPropertiesValidatorStrategy` tests out of `AsgRelPatternValidationStrategyTest`.
3. **Re-enable after a small test-only fix, then verify:**
   - `AsgMappingStrategyTest.FIXME_testQ4TransformMapping`: rename, add `@Test` and refresh the expected string
   - `CypherMatchHasLabelWithWhereOrOpLabelTranslatorTest:134`: refresh the expected string, and open a bug for the
     dropped rel property
   - `GraphInitiatorIT.testCreateIndices`: change 13 to 19
   - `OWLPizzaOntologyTranslatorTest` and `OWLSIOCOntologyTranslatorTest`: rewrite the placeholder assertions
4. **Worth a test rewrite (not a product fix):**
   - `PromiseEdgeIndexAppenderTest` (17 tests, about 0.5 day). This is the only unit coverage of time-series partition
     pruning.
   - `ElementControllerCompositTest` (about 2h)
5. **Product bugs to file in KNOWN_ISSUES.md:**
   - `Value` isn't `Comparable` in epb statistics for range ops on enums (`SmartEpbRedundancyTests:262`)
   - relProp combined with an end-pattern filter gives "No Parent Element found" (`RelationRangeAsgStrategyWithFiltersInRangTest:318`)
   - Cypher rel-property map `[c {k:v}]` is dropped (seen in `CypherMatchHasLabel…:134`)
   - Cypher multi-type edges `[:A|B]` keep only the first type (`CypherMatchSimpleTranslatorTest:86`)
6. **Leave disabled (feature gaps):**
   - the 17 SPARQL translator methods
   - union translation (`UnionOpTranslationStrategyTest`)
   - multi-level inner queries
   - "same variable, multiple operators" Cypher rule (2 tests)
   - untyped-relation inference
   - `AsgRelPatternValidatorStrategy`, which is an empty stub
   - OWL UCS translation
   - `CsvToRdfConverter`, which is a stub
   - the "Stats Double Bug" plan test
   - the 2 heavy datagen generators
7. **Investigate (UNKNOWN, roughly 1–2h each):**
   - `DefaultRelationSelectionAsgStrategyTest.FIXME_testSelectionForRel`
   - `EBaseStatisticsProviderBasicTests:336` and `:350`
   - `M1ChainedPlanOpTraversalTranslatorTest.FIXME_test_concrete_rel_typed_Agg`
   - `FileUtilsTest`, which needs a fixture
