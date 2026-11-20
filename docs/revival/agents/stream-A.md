# Stream A — Cypher translator & query semantics
*(Mirrored by the lead from the agent's worktree copy, 2026-11-20.)*

Agent `stream-A-cypher` · worktree `.claude/worktrees/agent-a52a1b8474fd93794` · branch
`worktree-agent-a52a1b8474fd93794`

## Goal
Every Cypher example in the docs either returns the right result or fails with an explicit, actionable error, never
silently wrong or empty. Scope: Cypher translation (`fuse-asg`) and, from round 2 on, the ASG/plan layers it feeds
(`fuse-dv-asg`, `fuse-dv-epb`, `fuse-dv-gta`). `fuse-dv-unipop`, `fuse-dv-core` and the EPB *redundant planner's own
code* belong to stream B (report before touching the latter).

## Tasks
- [x] Q1: undirected `-[r]-` runs as `->`. Root cause is **not** in fuse-asg; handed to stream B (fixed there). Guard test added.
- [x] Q5: label union `(p:A|B)` gives an explicit "unsupported" error. Untyped `match (p)` was a downstream bug (stream B fixed it).
- [x] Q8: `match (p) where p:A OR p:B` crashed with "EUntyped cannot be cast to ETyped" (fuse-dv-asg)
- [x] Q7: `WHERE count(k) > 15` returned nothing. The planner reversed the count-filtered relation (fuse-dv-epb validator).
- [x] Q6: cyclic pattern rejected by the ontology validator. Now an explicit "Cyclic patterns are not supported" error at translation.
- [x] Q7 follow-up (round 3): validator broke `return d1, distinct(f), d2` (Q9, "No valid plan"). Fixed in `e99f9082d`.
- [ ] Lead: merge `e99f9082d` and rerun e2e Q7/Q9 on a rebuilt image (I can't touch Docker)

## Status
**Done, awaiting merge** of `e99f9082d` (rebased on main `e498ad46a`).

**Exact next step:** none for stream A until the lead's e2e run. If that run shows Q7 or Q9 still failing, read the
query's plan with `GET /fuse/query/<id>/plan/print` (read-only), then add a case to
`fuse-dv/fuse-dv-epb/src/test/java/com/yangdb/fuse/epb/plan/AggregatedRelationPlanSearcherTests.java`, which
drives the service's planner (redundant DFS extension plus the M1/M2 plan validators).

## Discoveries
- **Q1 (ruled out in fuse-asg):** the translator emits `Rel.Direction.RL`, every M1/M2 ASG strategy copies the direction,
  and GTA maps `RL` to `bothE()`. The bug was below GTA (stream B). The ASG printer draws every non-`R` direction as
  `<--`, which looks like an `L` but isn't.
- **Q5:** the openCypher 9 parser has no node-label union. `(n:A:B)` translates to an untyped node matching *any* of
  the labels (non-standard; Cypher means AND). Documented alternative: `WHERE n:A OR n:B`. OR is translated into a
  `some` quant with one untyped branch per label, and the planner unions the branches (`UnionPlanSearcher`).
- **Q8:** the `Exact`, `Like` and `LikeAny` constraint strategies in `fuse-dv-asg/.../strategy/schema` cast the nearest
  entity to `ETyped`. They now skip untyped nodes. Note: fuse-dv-asg wasn't on my module list; no other stream owns it.
- **Q7 root cause:** GTA pushes `count` down as `CountFilterP`, grouped by the edge document's `entityA.id`, i.e. the
  entity the traversal comes from. The planner started at `p2` and walked Know in reverse, so it counted *incoming*
  edges. Data check: 9 persons have more than 15 outgoing Know edges, 0 have more than 15 incoming.
- **Q7 docs:** `count(k) > 10 AND p2.gender = FEMALE` counts *all* Know edges before the gender filter (edge docs don't
  store `p2.gender`, so it can't be pushed down). Data: 16 persons, not the 5 the docs claimed; the lead already aligned
  the docs (`dd0b057bc`).
- **Q7 follow-up (round 3), two causes:**
  1. `distinct(f)` becomes a `FunctionRelProp` whose `isAggregation()` is true with **no** count operator, so it was
     wrongly pinned. Only `Constraint.isAggregation()` (a count operator is set) pins a relation now.
  2. `BottomUpPlanSearcher` keeps only the **single cheapest partial plan per step** (`CheapestPlanPruneStrategy` as
     the local prune), including the seed. With symmetric endpoints, ties are broken by plan-string hash, so it can
     commit to the far seed. Rejecting the reversed RelationOp then leaves no plan at all. Fix: reject a plan whose
     *first entity* lies below (target side of) a count-filtered relation.
- **Q7 limitation:** two count-filtered relations whose source sides don't overlap, e.g. `(a)-[c1]->(b)<-[c2]-(c)`,
  have no valid seed, so the planner reports "no valid plan". That's unsupported, not silent, but has no friendly message.
- **Q6:** the ASG is a tree, so a relationship ending on an already-bound node is left without an end entity. The
  engine has no identity join (`EntityJoinOp` only joins branches meeting at one ASG entity). A new node pointing *into*
  a bound node isn't a cycle; it gets a hint to start that pattern from the bound node.
- **Tooling gotchas:**
  - `EUntyped`/`ETyped` print as `UnTyp`/`Typ`.
  - The rule-based cost estimator in EPB tests NPEs unless every entity has an `EPropGroup` after it.
  - The worktree sandbox blocks writes to the main checkout (STATUS.md must be updated by the lead).
  - A rebase on this drive can stall with "local changes would be overwritten": run `git rebase --abort`, delete
    `._*`, retry. `core.fileMode` is false.
- **Read-only inspection of a running query:** `GET /fuse/query/<id>/plan/print` and `/asg/print`; `cypher/run` returns
  the id.

## Commits
- `f599028b0` Fix Q5: reject Cypher node-label union with an explicit unsupported error (merged)
- `ce0426638` Test Q1: lock the translator's undirected pattern to both directions (merged)
- `a66ba46c2` Fix Q8: label predicates on untyped nodes no longer crash the M2 rewrite (merged, was `568c8b5e8`)
- `d25580d0f` Fix Q7: never reverse a count-filtered relation in the plan (merged, was `44d4a43c7`)
- `460153c8b` Fix Q6: reject cyclic Cypher patterns with an explicit error (merged, was `14c2d2ac7`)
- `e99f9082d` Fix Q7: pin only count-filtered relations, and reject bad seeds up front (**not merged**)

## Log
- 2026-11-20 Round 1: Q1 traced through fuse-asg/GTA, found to be downstream, handed to B. Q5 label-union error committed.
- 2026-11-20 Round 1: STATUS.md edits blocked by the sandbox; text handed to the lead in the report.
- 2026-11-20 Round 2: rebased; Q8 fixed (fuse-dv-asg); Q7 root cause confirmed against stored data, validator added; Q6 explicit error.
- 2026-11-20 Round 3: Q9 regression reproduced at planner level (`AggregatedRelationPlanSearcherTests`, both queries had no plan).
- 2026-11-20 Round 3: fixed count-only pinning plus seed rejection; fuse-dv-epb 431 / 0 failures; committed `d8ae42992`.
- 2026-11-20 Round 3: rebase stalled on a drive stat race; aborted and redone, now `e99f9082d` on main `e498ad46a`; handoff file created.
- 2026-11-20 Round 3: post-rebase test run started (fuse-dv-epb with upstream modules, ports 9230/9330).
- 2026-11-20 Round 3: post-rebase run green, 0 failures: fuse-dv-epb 431, fuse-dv-asg 31, fuse-dv-core 74 (ITs ran), fuse-dv-unipop 156, fuse-asg 267. Reported to lead.
