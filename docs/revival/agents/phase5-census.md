# Phase 5.0 census — handoff notes (uncommitted)

## Goal
Bump to OpenSearch 2.19.7, collect every compile error per module x category, write docs/revival/os2-census.md.

## Status
- [x] reset to origin/phase5-opensearch-2.19; versions bumped (lucene 9.12.3)
- [x] temp fixes applied iteratively until `clean test-compile -Pcore,dragons-assembly` is green (all 23 reactor modules)
- [x] netty / Jackson / ANTLR linkage checks (scratchpad/linkcheck)
- [x] RHLC async API listed via javap
- [x] docs/revival/os2-census.md written and committed
- Next step: none for 5.0. Streams Q/R/S/T start from os2-census.md. Temp changes discarded with `git checkout -- .`.

## Discoveries
- 2.19.7: lucene 9.12.3, log4j 2.25.5, jackson 2.18.9, netty 4.1.139.Final, painless antlr4-runtime 4.13.1
- transport artifact last published 1.3.20; 404 at 2.19.7. Replace with org.opensearch.plugin:transport-netty4-client
- only 207 compiler errors total (plus 24 pre-fixed relocated imports); 152 of them in fuse-dv-core, mostly the 3 Client wrapper classes
- ANTLR conflict: root pins 4.7.2 for graphql-java 2020; painless 2.19.7 is generated with 4.13.1 -> one of GraphQL / Painless breaks at runtime
- A1 netty pin (4.1.73 buffer/common) is incompatible with netty-transport 4.1.139 (Recycler$EnhancedHandle) -> must move to 4.1.139; jooby-netty 1.6.9 resolves 237/237 refs on 4.1.139
- Jackson 2.13.4 pin breaks opensearch-x-content 2.19 (StreamReadConstraints, Jackson 2.15+)
- RHLC 2.19 has no cluster-state call (use low-level client); indices create/mapping/template use client-side request types
