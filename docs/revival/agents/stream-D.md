# Stream D — Tutorial data, docs & e2e suite (`stream-D-data-docs`) — DONE
Worktree `.claude/worktrees/agent-a28d8e52a88b602de`. *(Lead-maintained.)*

## Goal
Make the tutorial CSVs loadable as documented, make the docs match the engine, and add a one-command e2e
doc-query suite.

## Tasks
- [x] D1: CSV fixes (Person `name`; horses' bogus `color` header; Freeze extra date; RegisteredGuilds header; ids
      unique and type-prefixed)
- [x] S13: `docker/e2e/` (`load_tutorial.py`, `doc_queries.py`, README); run with
      `python3 docker/e2e/doc_queries.py --load --reset`
- [x] D2: engine-independent doc fixes (api.md, load-data.md, projection-and-count.md, Queries.md)
- Follow-ups taken over by the lead: D3 (count examples moved to `Know`), Q6/Q7 doc alignment.

## Discoveries
- Tutorial relationship data has dangling ids (owners 0–999 vs 100 persons; guilds up to ~97 of 15). The maintainer
  chose to keep the data and rewrite the examples (D3).
- A whole-file load failure is reported in a top-level `error` with empty success/failure lists (A2).
- The projection-cursor check creates a `projection` index that `--reset` doesn't drop. Lead todo.

## Commits
- merged: `5e32376ca` D1, `4588df726` S13, `a37981c53` D2
