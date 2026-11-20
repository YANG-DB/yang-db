# Coordinator playbook: resuming the revival in a new session

You are the **coordinator (lead)**. You run in the main checkout, spawn stream agents into git worktrees, merge
their work into `main`, run the verification pipeline, and keep the tracking docs current. Read this file first,
then [lead.md](lead.md) for the exact next step, then [STATUS.md](../STATUS.md).

---

## 1. Start-of-session checklist (local machine)
```bash
cd "/Volumes/X9 Pro/projects/yang-db"
# Docker's VM lives on the external drive, inside an APFS disk image; mount it BEFORE starting colima
[ -d /Volumes/DockerVM ] || hdiutil attach "/Volumes/X9 Pro/DockerVM.sparsebundle"
colima status || colima start --cpu 4 --memory 6 --disk 40
colima ssh -- sudo sysctl -w vm.max_map_count=262144        # not persistent; needed by OpenSearch
docker compose -f docker/docker-compose-os.yml start        # containers persist; data volumes persist
curl -s localhost:8888/fuse/health                           # "Alive And Well..."
git config core.fileMode false; find . -path ./.claude -prune -o -name '._*' -delete
git status; git log --oneline origin/main..main              # local commits not yet pushed
git worktree list                                            # stream worktrees
```
- `~/.colima` is a symlink to `/Volumes/DockerVM/colima`. If the image isn't mounted, colima finds no VM. **Don't**
  run `colima delete`; mount the image instead.
- JDK 11: `export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-11.jdk/Contents/Home`. Maven runs offline
  (`-o`).

## 2. Read order
1. `docs/revival/agents/lead.md`: the coordinator's goal, tasks, **exact next step**, discoveries
2. `docs/revival/STATUS.md`: overview table, e2e results, open findings
3. `docs/revival/ROADMAP.md` and `KNOWN_ISSUES.md`: the plan and the bug IDs (L*, Q*, A*, D*, S*, T*)
4. `docs/revival/agents/stream-*.md`: per-stream detail. The copy inside each stream's worktree may be fresher
   (`.claude/worktrees/agent-<id>/docs/revival/agents/stream-X.md`).
5. Memory index (`~/.claude/projects/-Volumes-X9-Pro-projects-yang-db/memory/MEMORY.md`): machine and drive quirks

## 3. Maintainer rules (non-negotiable)
- **Never push without explicit confirmation.** "Stable before push" means `mvn clean test` is green with
  integration tests running, **and** the e2e suite passes on a freshly rebuilt image.
- **Never replace or shadow the root `README.MD`.** It is the maintainer's GitHub profile page. That includes
  `.github/README.md`. Project docs live in `PROJECT.md`.
- Commit messages end with `Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>`. PR bodies end with
  `🤖 Generated with [Claude Code](https://claude.com/claude-code)`.
- Project contact address: `yang.db.dev@gmail.com`. Never use the maintainer's private email.
- Fix-on-sight is scoped. Unrelated breakage gets flagged (`@Ignore` + comment, or a KNOWN_ISSUES entry) rather
  than fixed. Ask before open-ended digs.
- Maintainer decisions so far:
  - D3: keep the data, rewrite the examples
  - L4: range buckets
  - Docker on the external drive
  - Q6: cyclic patterns are unsupported, with an explicit error
  - label union: unsupported; use `WHERE p:A OR p:B`

## 4. Spawning stream agents
Each stream is one agent in its own worktree. A new session has no fork context, so spawn **fresh** agents and
give them a full brief.

**Agent tool call:**
- `subagent_type`: `general-purpose`
- `isolation`: `"worktree"`
- `name`: `stream-<X>-<topic>`, used for `SendMessage` while this session lives
- `description`: 3–5 words
- `prompt`: the brief template below

**Brief template** (fill in the angle brackets):
```
You are stream <X> of the YANG-DB "Stabilize" workstream: an ontology-driven graph query engine over OpenSearch
1.3.0, Java 11 + Maven, repo /Volumes/X9 Pro/projects/yang-db. You work ONLY in your own git worktree.

FIRST: run `git reset --hard main` (or `git rebase main` if you already have commits). New worktrees start from
origin/main, not the local main that holds all the merged work. Then read docs/revival/agents/COORDINATOR.md §6-§7,
docs/revival/KNOWN_ISSUES.md, and docs/revival/agents/stream-<X>.md (your handoff file; continue from "Status →
next step").

Goal: <goal>. Issues: <IDs>. Modules you may edit: <list>; ask the coordinator before touching others.
Ports: -Dfuse.test.http.port=<92X0> -Dfuse.test.transport.port=<93X0> -Dfuse.test.data.dir=/tmp/yangdb-es-<X>
Docker: <"read-only queries to :8888/:9200" | "you own the dragons-graph container: you may swap jars and
restart it, but never reload data">.

Rules:
- Write a failing test first (it must fail for the right reason), then the fix.
- One commit per issue ID, message "Fix <ID>: ...", ending with the Co-Authored-By line.
- Never push. Never run `mvn install`. Never use the default ports 9200/9300 while Docker is up.
- Keep docs/revival/agents/stream-<X>.md IN YOUR WORKTREE updated at every milestone and before long-running
  commands (format in agents/README.md). Leave it uncommitted.
- Final report under 350 words: commits, root cause per issue, tests (fail-before/pass-after), test counts per
  module (0 failures, non-zero integration tests), anything out of scope.
```

**Port and data-dir allocation** (each agent's test node needs its own):

| Stream | HTTP / transport | Data dir |
|---|---|---|
| A | 9230 / 9330 | /tmp/yangdb-es-A |
| B | 9210 / 9310 | /tmp/yangdb-es-B |
| C | 9220 / 9320 | /tmp/yangdb-es-C |
| D | 9240 / 9340 | /tmp/yangdb-es-D |
| E | 9250 / 9350 | /tmp/yangdb-es-E |
| new streams | 9260+ / 9360+ | /tmp/yangdb-es-<X> |

The coordinator's full build uses the defaults (9200/9300) **with Docker stopped**.

## 5. Controlling agents
- **Assign modules so streams don't overlap.** One agent edits a given module at a time. When a root cause crosses
  into another stream's module, the agent reports it and the coordinator reassigns.
- **The Docker container has at most one writer** (jar swaps, restarts, log levels). Everyone else sends read-only
  queries. Nobody reloads or resets the data except the coordinator.
- **Relay findings.** Agents can't see each other. When one finds something relevant to another (e.g. "the service
  wraps the schema provider in a cache"), forward it with `SendMessage`.
- **Follow-up rounds:** `SendMessage` to the agent's name continues it with its context, as long as this session
  lives. After a session restart, spawn a fresh agent into the **same worktree** (tell it the path) and brief it
  from its stream file.
- **Usage or rate limits:** an agent that stops on a limit keeps its saved work. Resume it with `SendMessage` once
  the limit resets. If it's gone, check its worktree: `git -C <wt> status` and `git -C <wt> log main..HEAD`.
- **Agents can't write the main checkout** (sandbox), so they can't update STATUS.md. The coordinator copies their
  reports into STATUS.md and their stream files into `docs/revival/agents/`.
- **Trust but verify:** read each diff before merging. Agents' green module tests have repeatedly *not* meant the
  service works, because the service runs a different path (§8). Only the e2e suite settles a fix.

## 6. Merge procedure (external drive quirks)
```bash
cd "/Volumes/X9 Pro/projects/yang-db"
git log --oneline main..worktree-agent-<id>          # what's new
for c in <hash1> <hash2> ...; do                      # one commit at a time
  find . -path ./.claude -prune -o -name '._*' -delete
  git update-index -q --refresh                       # avoids a phantom "local changes would be overwritten"
  git cherry-pick $c || break
done
```
- **Never** run `git cherry-pick --abort` in the middle of a multi-commit pick. It rolls back every pick already
  applied. If one pick conflicts, resolve that one, or restore the affected paths with
  `git checkout HEAD -- <path>` and remove `.git/CHERRY_PICK_HEAD`.
- When an agent rebases a commit you already picked, it gets a new hash. Check with
  `git diff <yours> <theirs> -- . ':!docs'`; empty output means it's already merged.
- Copy the stream's handoff file: `cp <wt>/docs/revival/agents/stream-X.md docs/revival/agents/`.

## 7. Verification pipeline (the only way to declare "stable")
```bash
S=<scratch dir>; export JAVA_HOME=...; cd "/Volumes/X9 Pro/projects/yang-db"
docker compose -f docker/docker-compose-os.yml stop          # free 9200/9300; fuse-dv-stat needs 9300
find . -path ./.claude -prune -o -name '._*' -delete; rm -rf /tmp/yangdb-es-main
mvn -o clean test -Dfuse.test.data.dir=/tmp/yangdb-es-main > $S/full.log 2>&1   # 0 failures, ITs non-zero
for i in 1 2 3; do find . -path ./.claude -prune -o -name '._*' -delete; rm -rf distrib/dragon distrib/dragon.tar
  mvn -o -q clean install -DskipTests -P core,dragons-assembly > $S/asm.log 2>&1 && break; done
test -f distrib/dragon.tar                                   # never build an image without a fresh artifact
cp docker/Dockerfile distrib/ && find distrib -name '._*' -delete
(cd distrib && docker build -q -t yangdb/yang.db:Dragons .); docker image prune -f
docker compose -f docker/docker-compose-os.yml up -d --force-recreate fuse   # service name "fuse"; container "dragons-graph"
curl -s -XDELETE localhost:9200/projection                   # leftover from the projection-cursor check
python3 docker/e2e/doc_queries.py --load --reset             # exit 0 = all pass
```
- `mvn clean` can fail to delete a `target/` because of `._*` races. Retry; `buildDocker.sh` now fails fast.
- `--reset` drops and recreates the Dragons indices. Fire/Freeze bucket placement (L4) can only be checked on
  freshly emptied indices.
- Last known good full build: **1,113 tests, 0 failures**. e2e run 2: 11 checks failing (since fixed by A r3, B r4
  and E r2; e2e run 3 pending).

## 8. Architecture facts that keep biting
- The service config (`/opt/engine/config/application.conf` in the container):
  - ASG: `M2AsgModule` and `M2AsgStrategyRegistrar`
  - planner: `EpbDfsCountBasedRedundantModule` and `M1DfsRedundantPlanExtensionStrategy`; it runs real counts and
    keeps only the cheapest partial plan per step, so results depend on the data
  - `M1ElasticUniGraphProvider`, `M1PlanTraversalTranslator`
  - cursors from `executor.modules.discrete.CursorsModule`, e.g. `ForwardOnlyPathsTraversalCursor`
- The service **wraps the schema**: a raw-schema chain (`CachedRawSchema` → `PartitionFilteredRawSchema` →
  `PrefixedRawSchema` → `IndexProviderRawSchema`) and a caching schema provider that stores empty lists. Tests must
  build the schema the same way, or they test a different path.
- Identity: entity ids are only unique *per type*. Anything keyed by bare id is a bug. Relationship ids are
  `<label>:<csv id>`.
- Swapping a module jar in the container may not take effect if another jar bundles the same classes (shaded
  jars). The lead's run-2 bisect was misled this way. Prefer reproducing in a test through the service's real wiring.

## 9. Gotchas (machine)
- The external drive (exFAT) creates `._*` sidecar files. They break git, Maven (bogus test classes), Docker
  builds and OpenSearch index deletion. Delete them before every build or git op; keep test data dirs in `/tmp`.
- **Set `JAVA_HOME` inside every script you run in the background.** Shell state doesn't carry over between
  commands. Without it Maven forks Homebrew's default JDK (27), which rejects the pom's `-XX:MaxPermSize`; the fork
  dies at startup and `unipop-core` reports "The forked VM terminated without properly saying goodbye". That's an
  environment failure, not a test failure.
- With `-Dtest`, surefire 2.22 ignores pom excludes. Pass `-Dtest='<pattern>, !**/._*'`.
- `fuse-dv-stat` tests read port 9300 from config. Run them only in the coordinator's full build with Docker
  stopped. Agents exclude it (`-pl '!fuse-dv/fuse-dv-stat'`, or run epb tests by package).
- Internal disk: 14 GB free after the colima move (2026-11-20). Docker data is in `/Volumes/DockerVM` (150 GB
  image).
