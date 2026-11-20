# Agent handoff files — how to resume after a restart

**New session? Start with [COORDINATOR.md](COORDINATOR.md).** Every agent working on the revival keeps a handoff file here, so work can continue from the same point after a
session, machine or agent restart. [STATUS.md](../STATUS.md) is the one-page overview; these files hold the detail.

| File | Agent | Worktree (branch `worktree-agent-<id>`) | State |
|---|---|---|---|
| [lead.md](lead.md) | lead (main session) | main checkout, branch `main` | active |
| [stream-A.md](stream-A.md) | `stream-A-cypher` | `.claude/worktrees/agent-a52a1b8474fd93794` | done (r1–r3 merged) |
| [stream-B.md](stream-B.md) | `stream-B-traversal` | `.claude/worktrees/agent-a43e06c700eb4b7ef` | done; r4 **not merged yet** |
| [stream-C.md](stream-C.md) | `stream-C-service` | `.claude/worktrees/agent-acdc794fa7038a886` | done |
| [stream-D.md](stream-D.md) | `stream-D-data-docs` | `.claude/worktrees/agent-a28d8e52a88b602de` | done |
| [stream-E.md](stream-E.md) | `stream-E-partitions` | `.claude/worktrees/agent-a7164b98179501307` | done (r1–r2 merged) |

## Every file has the same sections
1. **Goal**: what "done" means for this agent.
2. **Tasks**: a checklist.
3. **Status**: the current state, plus the **exact next step**, specific enough that a fresh agent can do it without
   asking.
4. **Discoveries**: root causes, dead ends ruled out, gotchas. Write down what you *ruled out* too.
5. **Commits**: hash and one line each, noting merged or not.
6. **Log**: dated one-line entries, newest last.

## Rules
- **Update your file at every milestone**: root cause found, test written, commit, blocker, before a long-running
  command, and before you report.
- **Stream agents:** edit `docs/revival/agents/stream-<X>.md` **inside your own worktree**. The sandbox blocks
  writes to the main checkout. Leave it **uncommitted**: it stays on disk across restarts, and the lead copies it to
  `main` when syncing.
- **The lead** keeps `lead.md` and this table current in the main checkout, and mirrors stream files when merging.

## Resuming
- **Same Claude session still alive:** message the agent by name (e.g. `stream-B-traversal`). It keeps its context.
- **New session, or the agent is gone:**
  1. Read `lead.md`, STATUS.md and the stream's file. The worktree copy (`<worktree>/docs/revival/agents/stream-X.md`)
     is the freshest.
  2. Start a new agent in that worktree with the stream file as its brief. Tell it to continue from
     "Status → next step".
  3. Check `git -C <worktree> status` and `git log main..HEAD` for uncommitted work and unmerged commits.
- **The worktree is gone:** the branch `worktree-agent-<id>` still exists. Recreate the worktree with
  `git worktree add .claude/worktrees/agent-<id> worktree-agent-<id>`.
