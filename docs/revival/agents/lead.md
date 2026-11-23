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

## ▶ RESUME HERE (updated 2026-11-23)
**Phase 5 merged (#169) and released as v0.9.0** (tag on `release/0.9.x`, poms `0.9.0`, notes
`docs/releases/v0.9.0.md`, label `opensearch-2.19`, distribution attached by the Release workflow). Main is now
`0.10.0-SNAPSHOT`. The 1.3 → 2.19 upgrade rehearsal **passed** (2026-11-23,
OPENSEARCH_MIGRATION.md §9). The v0.9.0 GitHub release text says so too. **Next:** (1) update the wiki for 2.19.7 and the REST port
(`opensearch.http.port`, 9200); (2) Phase 6
(OpenSearch 3.x, JDK 21). Releasing: cut `release/X.Y.x` from main, set the poms to `X.Y.0`, add
`docs/releases/vX.Y.0.md`, push, `gh release create vX.Y.0 --target release/X.Y.x --latest`; the workflow attaches
the download.

### Earlier note (2026-11-22 night)
**Phase 5 code is complete.** Branch `phase5-opensearch-2.19`: full build **1,249 tests / 0 failures** on 2.19.7 and
Docker e2e **34/34 on OpenSearch 2.19.7**. Two bugs only a live cluster showed are fixed: the bridge exception without
its index, and REST-parsed aggregations. A PR to main is open; CI checks it. Remaining: re-run the 1.3 → 2.19 in-place
upgrade rehearsal (needs a 1.3-built image to write the data), update the wiki, merge.

**v0.8.0 released (2026-11-23)**: the OpenSearch 1.3 line, tag `v0.8.0` on branch `release/0.8.x` (from main
`a24dd19fd`, poms `0.8.0`). Notes: `docs/releases/v0.8.0.md`. `.github/workflows/release.yml` builds the Dragons
distribution from the tag on every published release and attaches `yangdb-<ver>-distribution.tar.gz` + `.sha256`;
it was green for 0.8.0. The label `opensearch-1.3` is for 1.3-line fixes (which go to `release/0.8.x`). For the
rehearsal, build the 1.3 image from `v0.8.0`. After #169 merges: release it as 0.9.0 the same way (poms
`0.80-SNAPSHOT` → `0.9.0` on a `release/0.9.x` branch, notes `docs/releases/v0.9.0.md`).

### Earlier note (2026-11-22 evening)
**Phase 5 branch `phase5-opensearch-2.19` @ `47bd58411`** = baseline bump + 5.1 bridge (P) + 5.4 dependencies (S) +
5.2 relocations (Q) + 4 commits of 5.3 mapping types (R). P, S and Q are DONE.
- **R stopped on the usage limit (resets 17:30 Pacific).** Its worktree `.claude/worktrees/agent-a8801cce507a1a01b` still
  holds uncommitted fixes for 2 tests: `ClientProviderBridgeIT` (dv-core) and `ClientProviderTest` (dv-stat). The
  untracked `fuse-service/src/main/java/org/` is the census TransportClient STUB: never commit it. Its handoff file is
  stale. **Next:** `SendMessage phase5-R-mapping-types` "resume: commit the 2 test fixes, then verify the full reactor
  `test-compile -Pcore,dragons-assembly` with no placeholders, run the dv-core ITs, report".
- **Then spawn T (5.5 test harness):** the fuse-service `ElasticEmbeddedNode`, `ElasticIndexConfigurer` and
  `MappingElasticConfigurer` still use the transport client (replace with `node.client()` or the bridge); check that
  Painless loads on ANTLR 4.13.1; all ITs green on 2.19 (ports 9410/9510).
- **Then lead verification:** full reactor `mvn clean test`, then image + Docker e2e on OpenSearch 2.19.7
  (`docker/.env` is already 2.19.7), then the PR to main (CI checks it). Re-run the A1 Jooby test and the GraphQL tests
  on 2.19 (S verified them only on 1.3).
- Gotcha (from Q): `-pl` builds pick up stale 1.3 SNAPSHOTs from ~/.m2; always build the full reactor.

### Previous resume note (after the census)
**Latest:** census done (`docs/revival/os2-census.md`). Baseline bump pushed: `76a4bd12e` on `phase5-opensearch-2.19`
(2.19.7, Lucene 9.12.3, transport artifact swapped, docker OS_VERSION 2.19.7; does NOT compile until the streams land).
Running agents, all from `76a4bd12e` except P (pre-#168 base):
| Agent | Step | Worktree |
|---|---|---|
| `phase5-P-bridge` | 5.1 REST bridge | `.claude/worktrees/agent-a14055631ebbe64db` |
| `phase5-S-deps` | 5.4 netty 4.1.139, Jackson ≥2.15, log4j, **ANTLR 4.13.1 + graphql-java upgrade** (blocker), commons-codec | `.claude/worktrees/agent-a7ca49b8b525cbecc` |
| `phase5-Q-relocations` | 5.2 relocations + the 3 Client wrapper classes (PIT methods) | `.claude/worktrees/agent-abc737161747a09a9` |
| `phase5-R-mapping-types` | 5.3 untyped templates, loaders, search, system-index kind field | `.claude/worktrees/agent-a8801cce507a1a01b` |
Merge order: S → Q → R → P, then spawn **T** (5.5 test harness, runtime) on the merged result. All use the census's
TransportClient stubs locally only (never committed). Expect import-line collisions between Q and R: resolve with Q's
imports and R's logic.

### Earlier checkpoint (2026-11-22, session paused)
**Where things stand**
- `main` = `a24dd19fd`. Everything through Phase 4 and CI (#168) is merged.
  - Merged PRs: #160 Phase 1, #161 playground, #162 Phase 2, #163 Phase 3, #164/#165 bug fixes, #167 Phase 4 (+ planning),
    #168 CI. #166 was closed as covered.
- Full build: **1,189 tests, 0 failures**. Docker e2e: **34/34** (tutorial, R1–R8, GraphQL GQ1–GQ3, cursors, playground).
- **CI** (`.github/workflows/ci.yml`) runs Build & test and Docker e2e on every PR (~2.5 min). **CodeQL** runs on main
  pushes and weekly only (~6 min, Java only, no build).
- **Phase 5 (OpenSearch 2.19.7) in progress** on branch `phase5-opensearch-2.19` (= main + roadmap/handoff commit).
  Plan: [OPENSEARCH_UPGRADE.md](../OPENSEARCH_UPGRADE.md). Conclusions: [OPENSEARCH_MIGRATION.md](../OPENSEARCH_MIGRATION.md).

**Agents running at the pause** (both based on the pre-#168 main `09b1c8699`; their commits cherry-pick cleanly. The
only possible overlap is the root pom module list.)
| Agent | Step | Worktree | State at pause |
|---|---|---|---|
| `phase5-census` | 5.0 census | `.claude/worktrees/agent-aa60a66d7ebf83086` | versions bumped (Lucene → 9.12.3; `transport` client → `transport-netty4-client` as a temporary fix); first online compile pending. Deliverable: `docs/revival/os2-census.md` (the only file it commits; the rest is throwaway) |
| `phase5-P-bridge` | 5.1 bridge | `.claude/worktrees/agent-a14055631ebbe64db` | survey done, writing module `fuse-opensearch-client` (`RestBridgeClient`, `ClientFactory`); nothing committed |

**How to resume**
1. Same Claude session: `SendMessage` to `phase5-census` and `phase5-P-bridge`, saying "resume; update your handoff file
   first". Each has a handoff file in its worktree (`docs/revival/agents/phase5-census.md` / `phase5-P.md`).
2. New session: read [COORDINATOR.md](COORDINATOR.md). For each worktree above, check `git -C <wt> status` and
   `git -C <wt> log`, then spawn a fresh agent IN THAT WORKTREE, briefed from ROADMAP Phase 5 + OPENSEARCH_UPGRADE.md +
   its handoff file. Census: if its uncommitted bump is lost, redo it; it's cheap.
3. When the census lands, split 5.2 (package relocations, Q1/Q2), 5.3 (mapping types, R), 5.4 (dependencies, S) and
   5.5 (test harness, T) per its recommendation. Lanes and ports are in OPENSEARCH_UPGRADE.md. Merge in the order
   5.4 → 5.2 → 5.3 → 5.1 → 5.5.
4. Every merge to the branch: full `mvn clean test` (Docker stopped); then image + e2e (COORDINATOR §7, or
   `scratchpad/pipeline.sh`. If the scratchpad is gone, the steps are in COORDINATOR §7). The PR then gets checked by CI.

**Open items for the maintainer** (asked, not done)
- Create the branch **ruleset** for `main`: require a PR (0 approvals), require the `Build & test` and `Docker e2e`
  checks, block force pushes and deletions (steps were given in chat). Offered to do it via `gh api` with explicit OK.
- Uninstall stale GitHub Apps: LGTM.com, Redocly (the always-red `yangDb/v0.51` check), CodeLingo, Mend Bolt,
  SpectralOps; Jules if unused.
- Optional: triage CodeQL's 7 security findings (2 int/long loop conditions, an SVG temp-file permission; dismiss the 3
  hashing and 1 vendored-JS findings).

**Machine notes:** Docker VM on `/Volumes/DockerVM` (attach the sparsebundle before colima). Always set JAVA_HOME inside
scripts; `._*` cleanup before git and Maven; never force-push; never push to main directly (PRs only).

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
- **QB fixed by the lead** (`5cb0117d2`, local): `AsgEntityPropertiesValidatorStrategy` treated an untyped node
  with no vTypes as having no fields, and ignored metadata (`type`). New tests are in
  `AsgEntityPropertiesValidationStrategyTest`; fuse-asg has 270 tests, 0 failures. Stream A is no longer needed
  for QB. Don't resume it for that.
- **e2e run 4: ALL 21 CHECKS PASS**; full build 1,123 / 0 failures. Fixed the description of run 3's ✗ status
  (20/21, not 21/22). Waiting on the maintainer's OK to push; after pushing, run
  `docker/e2e/verify_and_mark.sh` (it reuses the loaded data; the image must match HEAD) to post ✓ on the pushed
  commit.

## Queue (maintainer-approved, 2026-11-20)
1. ~~**Phase 1 PR**~~: merged as #160 (`3ab36bbc0`, 2026-11-20). The remote branch `phase1-examples-layout` is still
   there; delete it only if the maintainer asks. Original plan: branch `phase1-examples-layout` (assembly → `distribution/`, ext/datagen → `examples/dragons/`,
   dragons-sys-test retired). Verify (full build + image + e2e), push the branch, mark `e2e/docker` on the branch
   commit, open the PR for the maintainer to review. Don't merge it yourself.
2. ~~**Dragons playground at `/dragons/swagger`**~~: merged as #161 (`ddcd553bf`, 2026-11-20), with the tutorial main page.
5. **Phase 5 (2026-11-22)**, branch `phase5-opensearch-2.19` (created from main before #168, the CI workflow; rebase
   onto main once #168 merges). Running: `phase5-census` (5.0, report `docs/revival/os2-census.md`) and `phase5-P-bridge`
   (5.1). Next: when the census lands, split 5.2–5.5 per its recommendation (OPENSEARCH_UPGRADE.md lanes and ports).
4. ~~Phase 4~~: merged as #167. **Phase 4 status (2026-11-21):** stage A merged and pushed to `origin/phase4-extension-api`, verified live (1,171
   tests, e2e 31/31, generic routes OK). Stage B (agent `phase4-N-graphql`, worktree
   `.claude/worktrees/agent-ac582b184814bd670`) **stopped on the usage limit** mid-refactor: 51 modified + 5 new files,
   no commits, `extensions/graphql` created. **Next:** after the reset, `SendMessage` to `phase4-N-graphql` to continue.
   If the session is gone, start a fresh agent in that worktree, brief it with ROADMAP Phase 4 B and phase4-M.md, and
   tell it to inspect `git status` first. PR #166 (OpenSearch upgrade planning) awaits review.
   Original plan: **Phase 4** (branch `phase4-extension-api`): stage A = agent M (extension API + wiring); stage B = GraphQL
   extraction + G1–G3 fixes (spawn after M reports, using M's 'what stage B needs'). #165 (bug fixes → main) awaits merge.
3. ~~Phase 2~~: merged as #162. **Now: Phase 3** (streams F–I; see ROADMAP), on branch `phase3-structural-assertions`, merged into one PR.
   Previous plan text: **Next: Phase 2** (one shared source of test fixtures), unless the maintainer reprioritizes. Use a branch and a PR, as for Phases 1 and 2.
   Original playground plan:
   - a hand-written OpenAPI spec served by the Dragons extension (`examples/dragons/ext`), separate from the generated
     `/swagger`
   - `POST /fuse/dragons/tutorial/load`: one click runs idgen → catalog → init → all tutorial CSVs, bundled into the
     distribution
   - the query endpoint gets a dropdown of named examples for every documented query and cursor type
   - the e2e suite reads its query list from that spec (single source of truth)
   - link it from the service home page and the tutorial
   - check that the bundled Swagger UI is 3.x (needed for the examples dropdown); otherwise ship one with the page

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
