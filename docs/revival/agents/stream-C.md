# Stream C — Service rendering (`stream-C-service`) — DONE
Worktree `.claude/worktrees/agent-acdc794fa7038a886`. *(Lead-maintained.)*

## Goal
Make JSON the default REST response (A1), and analyse the time-partition buckets (L4).

## Tasks
- [x] A1: `RegistrarsUtils.with` picked octet-stream for `*/*` or a missing Accept header. Now only explicitly named
      formats are honoured; wildcards get JSON. Test `RegistrarsUtilsContentNegotiationTest` (a real Jooby server,
      7 requests).
- [x] L4 analysis → the maintainer chose range buckets → stream E.

## Discoveries
- netty-buffer 4.1.43 (jooby) mixed with 4.1.73 (OpenSearch) → `NoSuchMethodError ByteBuf.isContiguous` under
  `mvn test`. Pinned to 4.1.73 in fuse-service.
- The `RegistrarsUtils.with` error branch has no `return` (possible double send). Unfiled.

## Commits
- merged: `54896dc37` A1
