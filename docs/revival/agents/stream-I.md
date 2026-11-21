# Stream I: legacy @Ignore triage (Phase 3)

## Goal
Sort every legacy `@Ignore`d test that no other stream owns into one of four categories: OBSOLETE, FIXABLE,
KEEP-DISABLED or UNKNOWN. Verify by temporarily un-ignoring, then write `docs/revival/IGNORED_TESTS.md`.

## Tasks
- [x] List the @Ignore lines: 95 in total, 86 in scope after exclusions
- [x] Un-ignore each module's tests in one batch and run them (temporary; reverted)
- [x] Classify the tests and write the report
- [x] Commit the report only (d6f821478)

## Status
Done. The next step is the maintainer's: decide on the report's recommended actions. These are to delete 21 obsolete
tests, re-enable 3 classes that pass, apply 5 small test fixes, and file 4 product bugs.

## Discoveries
- `OpensearchIndexProviderMappingFactoryIT` has 5 ignores, not 4. Line 558 is listed in the report and deferred to the
  stream that owns that file.
- fuse-dv-epb has 10 ignores. The 4 "Pre-existing" ones are excluded; the other 6 are triaged.
- `AsgMappingStrategyTest` is a JUnit 3 `TestCase`, so the `FIXME_` prefix keeps that method from running.
- `ThinPathTest` has `while(true)` loops. Never un-ignore it.
- The OWL Pizza and SIOC tests are copy-paste placeholders of the Social OWL test.
- `PromiseEdgeIndexAppenderTest`: making the mock rTypes match the edge labels was not enough to fix it.
- There's no `timeout` binary on this Mac, so don't wrap mvn in it (the command silently doesn't run).

## Log
- Worktree at e48e7ce09.
- Ran the fuse-asg, fuse-core, fuse-dv-{unipop,core,gta,epb} batches. Every source change was reverted; the worktree
  is clean apart from this file.
- Committed IGNORED_TESTS.md as d6f821478.
