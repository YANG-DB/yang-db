# Lead (main session)

## Goal
Get the Stabilize workstream to "stable", as defined in [ROADMAP.md](../ROADMAP.md):
1. `mvn clean test` green, with the integration tests actually running.
2. The e2e doc-query suite (`docker/e2e/doc_queries.py`) green against a freshly rebuilt Docker image.

Then push the batch (`S14`), but only after the maintainer confirms.

## Tasks
- [x] Stage 0: T1, L3, configurable test node (pushed, `3ace71186`)
- [x] Stage 1: launch streams A–D; add stream E for L4 after the maintainer chose range buckets
- [x] Merge A r1+r2, B r1–r3, C (A1), D (D1, S13, D2), E r1 (L4), plus lead commits D3, the docs for Q6/Q7, and
      buildDocker.sh
- [x] Full `mvn clean test` on merged main: **1,113 tests, 0 failures** (2026-11-20)
- [x] e2e run 1 (pre-L4) and run 2 (all merged). Run 2 regressed, so I bisected and sent the fixes out (below).
- [x] Merge A r3 (Q7 validator) and E r2 (Fire → `_500`: service RawSchema wrappers dropped time partitions)
- [x] Merge B r4 (`f7eab43ae`, `a501228d4`, `79b8b0e01` on main)
- [x] Full `mvn clean test` after B r4: 1,120 tests, 0 failures
- [x] Move Docker (colima) to the external drive (done 2026-11-20; internal disk 4.5 → 14 GB free)
- [ ] Full `mvn clean test` (Docker stopped) → rebuild image → `python3 docker/e2e/doc_queries.py --load --reset`
- [ ] e2e suite: drop the `projection` index created by the projection-cursor check (`--reset` leaves it behind)
- [x] Tick ROADMAP/KNOWN_ISSUES, update PROJECT.md and the GitHub wiki, push (2026-11-20, at the maintainer's request,
      before e2e run 3)

## Maintainer instruction (2026-11-20)
**When stream B reports, migrate Docker (colima) to the external drive FIRST, before merging B or anything else.**
Then: merge B → full `mvn clean test` (Docker stopped) → rebuild image → e2e `--load --reset`.

## Status (2026-11-20, end of session 1)
- All five streams are done. A, C, D and E are fully merged. **B round 4 (3 commits) is NOT merged yet.**
- Docker was migrated to the external drive (done and verified). The stack runs, and the data survived. The
  `dragons-graph` container still runs B's experimental jars, which is fine because the next image rebuild replaces
  them.
- `main` is pushed to `origin/main` (2026-11-20).
- **Exact next steps (in order):**
  1. ~~Merge B r4~~: done, as `f7eab43ae`, `a501228d4`, `79b8b0e01`.
  2. ~~Full `mvn clean test`~~: done, **1,120 tests, 0 failures**, all 16 modules green. Docker is left **stopped**.
     Next: rebuild the image, `docker compose ... start`, delete the `projection` index, then
     `python3 docker/e2e/doc_queries.py --load --reset` (e2e run 3; COORDINATOR.md §7).
  3. Check that Fire/Freeze land in `idx_*_2000` (L4 e2e confirmation) and that Q7/Q8 count queries return 9 and 16
     p1 persons.
  4. For any failure, spawn or route to the owning stream (see stream files) with the evidence.
  5. ~~Push~~: done 2026-11-20. Next pushes need the maintainer's OK again.
- Known open items that don't block "stable":
  - an undirected pattern written from the target side, e.g. `(k:Kingdom)-[:RegisteredIn]-(g:Guild)`, is rejected
    by the ASG validator (owner: stream A)
  - two count filters with non-overlapping start sides get "No valid plan"
  - `match (p)` is capped at 1,000 results
  - make the e2e suite clean up its own `projection` index

## Update — e2e run 3 (2026-11-20)
- Pushed `56621b0af`. Run 3 on an image built from it: **21/22 pass**; L4, Q7, Q8 counts and Q9 confirmed live.
- Posted GitHub commit status `e2e/docker` = failure on `56621b0af`. From now on use `docker/e2e/verify_and_mark.sh`.
- **Open: QB** (`WHERE p:A OR p:B` rejected by the ASG validator). Stream A hit the usage limit (resets 14:50
  Pacific); the lead is investigating. Next: fix QB → full build → rebuild → `verify_and_mark.sh` → ask the
  maintainer before pushing.

## Discoveries
- Run 2 bisect, done by swapping jars into the container and restarting it:
  - A's `AggregatedRelationDirectionOpValidator` rejects plain directed plans; Q9 passes with it reverted.
  - The undirected and multi-pattern "No valid plan" failure persists with the complete **run-1 jar set
    (`f8a96e4da`)** and the old `YYYY` config. So it's data or state related, not a code regression. The service
    uses the count-based redundant planner (`EpbDfsCountBasedRedundantModule`), which runs real counts.
- All Fire/Freeze docs land in `idx_*_500` end-to-end, although E's IT passes.
- The service config (in the container at `/opt/engine/config/application.conf`): `M2AsgModule`,
  `M2AsgStrategyRegistrar`, `EpbDfsCountBasedRedundantModule`, `M1DfsRedundantPlanExtensionStrategy`,
  `M1ElasticUniGraphProvider`, `M1PlanTraversalTranslator`.
- Merging on the external drive: `git cherry-pick A B C` can stop with a phantom "local changes would be
  overwritten". Pick one commit at a time with `git update-index -q --refresh`. Never use `cherry-pick --abort`
  mid-sequence: it rolls back every pick already applied.
- `mvn clean` can fail to delete a `target/` (it races the `._*` files). `buildDocker.sh` now fails fast; retry the
  assembly up to three times.
- The internal disk is about 4.6 GB free.
  - Docker VM move: APFS sparsebundle `/Volumes/X9 Pro/DockerVM.sparsebundle` (mount `/Volumes/DockerVM`).
  - Script: `scratchpad/move_colima.sh` (copy below).
  - After a reboot, attach the bundle before `colima start`.
- Agents in worktrees can't write to the main checkout, so they keep handoff files in their own worktree.

## Colima move (copy of the script, in case the scratchpad is cleared)
```
hdiutil attach "/Volumes/X9 Pro/DockerVM.sparsebundle"   # if not mounted
colima stop
rsync -aS ~/.colima/ /Volumes/DockerVM/colima/
mv ~/.colima ~/.colima.bak && ln -s /Volumes/DockerVM/colima ~/.colima
colima start --cpu 4 --memory 6 --disk 40 && colima ssh -- sudo sysctl -w vm.max_map_count=262144
# verify: docker ps -a ; curl localhost:8888/fuse/health ; then rm -rf ~/.colima.bak
```

## Log
- 2026-11-20 Stage 0 pushed. Stage 1 agents launched, hit the usage limit, and were resumed with STATUS.md.
- 2026-11-20 A, D, C(A1), B r1 merged; full build green (1,088); e2e run 1: loader green, queries wrong.
- 2026-11-20 D3 (maintainer: rewrite the examples); L4 (maintainer: range buckets) went to stream E.
- 2026-11-20 A r2, B r2+r3 and E merged; full build green (1,113); e2e run 2 regressed; bisected; A r3, B r4 and
  E r2 dispatched.
- 2026-11-20 Maintainer asked to put Docker on the external drive; the sparsebundle is created; the move waits on B.
- 2026-11-20 Maintainer asked for these per-agent handoff files. E r2 and A r3 merged.
- 2026-11-20 B r4 done (all 5 failing queries correct live). Docker migrated to the external drive.
  COORDINATOR.md written for resuming in a new session.
