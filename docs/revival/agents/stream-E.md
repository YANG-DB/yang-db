# Stream E — Time-partition range buckets (`stream-E-partitions`)
Worktree `.claude/worktrees/agent-a7164b98179501307`, branch `worktree-agent-a7164b98179501307`, ports 9250/9350,
data dir `/tmp/yangdb-es-E`. *(Lead-maintained mirror.)*

## Goal
L4: Fire/Freeze documents land in the range bucket covering their date, through the REST load too. Loader, query
and stats agree.

## Tasks
- [x] r1: `TimeSeriesBuckets` (each value is the inclusive upper bound, numeric order, UTC); loader fails loudly
      past the last bucket; `PromiseEdgeIndexAppender` and `EBaseStatisticsProvider` select buckets numerically;
      `YYYY`→`yyyy`
- [x] r2: the service's `PrefixedRawSchema` and `PartitionFilteredRawSchema` dropped the time-partition type, so
      every row went to the first index (`_500`). Fixed via `TimeSeriesIndexPartitions.mapIndices`.
- [ ] Confirmed end-to-end on a rebuilt image. Needs Fire/Freeze reloaded into **empty** indices; the lead does this
      in the next e2e run with `--reset`.

## Status (2026-11-20)
- Done and merged. Waiting only for e2e confirmation.
- **Next step:** none for this agent unless e2e shows Fire/Freeze outside `idx_*_2000`.

## Discoveries
- The service schema chain is `CachedRawSchema` → `PartitionFilteredRawSchema` → `PrefixedRawSchema` →
  `IndexProviderRawSchema` (`ExecutorModule.bindRawSchema`). ITs that build the schema directly skip it. Always test
  through this chain.
- Out of scope:
  - `DragonsOntologyPhysicalIndexProviderFactory` (removed with the dragons-sys-test module in Phase 1) still matches exact index names.
  - The appender reads only the first time predicate.
  - Stats intersect `inSet` values instead of taking their union.
  - Fixture dates built with `new Date(String)` use the local time zone.

## Commits
- merged: `6b0a1c346` (r1), `e498ad46a` (r2)
