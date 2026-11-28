# Benchmark

How fast YANG-DB answers its documented queries, how much work each one asks of OpenSearch, and how that compares
between releases and OpenSearch versions. Measured 2026-11-24 with the benchmark in
[docker/bench](../docker/bench/README.md), which anyone can rerun. CI runs it on every pull request (see
[below](#in-ci)).

## Setup

| | |
|---|---|
| Queries | the 31 queries of the Dragons playground (27 Cypher, 4 GraphQL), the same set the e2e suite checks; their text is in the [playground spec](../examples/dragons/ext/src/main/resources/dragons/playground/openapi.json) |
| Data | **scale 1**: the Dragons tutorial data, 22,151 CSV rows, about 42,700 stored documents (relationships are stored twice). **Scale 10**: 10 disjoint copies of it, 221,510 rows, about 426,800 documents. Every query that isn't limited returns exactly 10 times the rows at scale 10, which the results confirm |
| Runs | 1 unmeasured warm-up, then 5 measured runs per query, one query at a time; each query deleted after its run |
| Measures | **latency**: p50 of the 5 runs, client side. **OpenSearch ops**: shard-level search operations (query + fetch + scroll) plus gets, from `GET /_stats` around each run |
| Machine | Apple M3 (8 cores, 16 GB), Docker in a colima VM with 4 CPUs and 6 GB. OpenSearch single node, 512 MB heap; YANG-DB 1 GB heap (the quickstart's docker-compose settings) |
| Versions | each on a fresh cluster with freshly loaded data: **v0.8.0** on OpenSearch **1.3.0** (transport client), **v0.9.0** and **v0.9.1** on **2.19.7** (REST), and **main** at `cca8edbc4` (0.9.1 plus the dependency updates and the fuse-core packaging fix) on 2.19.7 |

Keep the machine awake for the whole run (`caffeinate -dims` on macOS): when it sleeps, the Docker VM's clock stalls
and then jumps, which stalls Docker's health checks.

Latency depends on the machine and varies by about ±8% between runs of the same build (compare v0.9.1 and main, whose
query code is the same). OpenSearch ops don't: within a run they are identical on every repetition, and between fresh
loads only a few queries vary, because the planner can pick a different plan of equal cost.

## Summary

Totals over the 29 queries that every version runs and answers the same way (GQ4 fails before 0.9.1, R9 returns
different rows before 0.9.1):

| | v0.8.0 · OpenSearch 1.3 | v0.9.0 · 2.19 | v0.9.1 · 2.19 | main · 2.19 |
|---|---:|---:|---:|---:|
| Scale 1: sum of p50 latencies | 1,425 ms | 1,544 ms | 1,520 ms | 1,408 ms |
| Scale 1: OpenSearch ops | 1,360 | 1,359 | 1,360 | 1,360 |
| Scale 10: sum of p50 latencies | 9,008 ms | 9,383 ms | 9,495 ms | 9,155 ms |
| Scale 10: OpenSearch ops | 3,444 | 3,348 | 3,408 | 3,456 |

What it shows:

- **OpenSearch 1.3 → 2.19 costs nothing measurable.** The move from the transport client to the REST API (0.8 → 0.9)
  leaves the OpenSearch work the same, op for op at scale 1, and latency within the run-to-run variation at both
  scales.
- **The 0.9.1 fixes change results, not cost.** GQ4 (GraphQL on a relationship with two target types) works from
  0.9.1 on. R9 (two count conditions on one relationship) returns the right rows from 0.9.1 on, and is faster because
  of it (239 → 61 ms at scale 10).
- **Ten times the data costs 6.5 times the latency, and 2.5 times the OpenSearch work, in total.** Queries that look up
  persons by name barely change (QD, R7, R8: about 1×). Queries that return everything grow with their result (Q5 and Q9:
  10×, 24,370 and 29,640 relationships at scale 10). A few queries make many more OpenSearch requests at scale 10:
  Q9 78 → 636 ops, R4 and R5 63 → 414, R6 36 → 252, Q6 146 → 405, QG 111 → 309. Those are where Phase 7's
  batching and paging changes ([ROADMAP](revival/ROADMAP.md#phase-7--speed-and-efficiency)) should help most, and
  this benchmark is how they will be measured.

The slowest queries at scale 10 are Q9 (2.5 s: every `Fire` relationship between two dragons, 29,640 of them) and Q5
(1.7 s: the dragons that fired on, or were fired on by, a black dragon, 24,370 relationships).

## Limits found while benchmarking

- **A large CSV fails to load as a whole** (L5): the loader sends a whole file as one bulk request, and OpenSearch
  rejects the 115,030-row Fire file at scale 10. The benchmark loads in pieces of 20,000 rows.
- **Queries stay in memory for 5 minutes** (Q14): without deleting them, a few hundred large queries ran the 1 GB
  engine heap out at scale 10. The benchmark deletes each query after its run; clients should too.
- **A clock step stopped the engine** (A11, fixed in 0.10): when the VM's clock stepped back about 50 seconds, every
  request failed for 47 seconds ("Clock moved backwards"). That run was discarded and repeated. The benchmark now
  waits such a refusal out when it measures releases up to 0.9.1.
- **The engine sometimes hung at start-up** (S1, fixed in 0.10): about 1 start in 3, in logback's console setup.
  Found while measuring Phase 7.3.

Details and fixes in [KNOWN_ISSUES.md](revival/KNOWN_ISSUES.md).

## Results per query

p50 latency in ms and OpenSearch ops per query. `error`: that version can't run the query. `*`: that version returns
different rows from v0.8.0, so the numbers aren't like for like. The raw results, with p95 and the ops broken down,
are in [benchmark-results/](benchmark-results/).

### Scale 1 (tutorial data)

| Query | v0.8.0 ms | v0.8.0 ops | v0.9.0 ms | v0.9.0 ops | v0.9.1 ms | v0.9.1 ops | main ms | main ops |
|---|---:|---:|---:|---:|---:|---:|---:|---:|
| Q1 | 51.7 | 12 | 51.8 | 12 | 54.3 | 12 | 44.0 | 12 |
| Q2a | 102.4 | 36 | 110.4 | 36 | 100.2 | 36 | 114.4 | 36 |
| Q2 | 64.3 | 36 | 68.5 | 36 | 67.9 | 36 | 61.4 | 36 |
| Q3 | 31.4 | 36 | 34.5 | 36 | 36.1 | 36 | 28.0 | 36 |
| Q4 | 26.7 | 36 | 31.4 | 36 | 36.0 | 36 | 25.7 | 36 |
| Q5 | 165.3 | 63 | 165.7 | 63 | 160.1 | 63 | 165.4 | 63 |
| Q6 | 103.8 | 146 | 116.3 | 146 | 119.0 | 146 | 110.7 | 146 |
| Q7 | 35.7 | 39 | 39.6 | 39 | 38.6 | 39 | 33.9 | 39 |
| Q8 | 32.8 | 39 | 38.2 | 39 | 35.7 | 39 | 30.5 | 39 |
| Q9 | 239.5 | 78 | 239.8 | 77 | 241.8 | 78 | 228.4 | 78 |
| QA | 52.9 | 60 | 51.5 | 60 | 49.1 | 60 | 46.9 | 60 |
| QB | 42.5 | 96 | 50.9 | 96 | 51.7 | 96 | 50.3 | 96 |
| QC | 15.3 | 12 | 18.4 | 12 | 17.3 | 12 | 12.6 | 12 |
| QD | 14.6 | 10 | 14.6 | 10 | 14.9 | 10 | 10.9 | 10 |
| QE | 34.0 | 36 | 35.1 | 36 | 36.4 | 36 | 31.1 | 36 |
| QF | 25.3 | 57 | 27.4 | 57 | 27.3 | 57 | 28.5 | 57 |
| QG | 87.5 | 111 | 96.0 | 111 | 91.4 | 111 | 95.8 | 111 |
| R1 | 22.7 | 39 | 26.8 | 39 | 25.9 | 39 | 23.6 | 39 |
| R2 | 31.1 | 39 | 34.3 | 39 | 34.1 | 39 | 32.1 | 39 |
| R3 | 42.7 | 39 | 46.0 | 39 | 43.7 | 39 | 42.2 | 39 |
| R4 | 24.5 | 63 | 28.6 | 63 | 31.6 | 63 | 23.2 | 63 |
| R5 | 22.1 | 63 | 30.6 | 63 | 32.8 | 63 | 25.6 | 63 |
| R6 | 17.9 | 36 | 22.2 | 36 | 22.8 | 36 | 17.1 | 36 |
| R7 | 10.7 | 6 | 12.8 | 6 | 12.2 | 6 | 6.8 | 6 |
| R8 | 15.7 | 34 | 19.1 | 34 | 19.3 | 34 | 14.3 | 34 |
| R9 | 38.1 | 39 | 49.2 | 39 | 26.3* | 39* | 22.5* | 39* |
| R10 | 34.1 | 78 | 44.3 | 78 | 40.1 | 78 | 37.1 | 78 |
| gql:GQ1 | 35.1 | 12 | 39.3 | 12 | 32.2 | 12 | 29.1 | 12 |
| gql:GQ2 | 18.4 | 12 | 21.2 | 12 | 19.7 | 12 | 15.1 | 12 |
| gql:GQ3 | 24.5 | 36 | 28.4 | 36 | 27.8 | 36 | 23.1 | 36 |
| gql:GQ4 | error | – | error | – | 33.3 | 48 | 39.2 | 48 |
| **all 30 queries every version ran** | **1463** | **1399** | **1593** | **1398** | **1546** | **1399** | **1430** | **1399** |

### Scale 10

| Query | v0.8.0 ms | v0.8.0 ops | v0.9.0 ms | v0.9.0 ops | v0.9.1 ms | v0.9.1 ops | main ms | main ops |
|---|---:|---:|---:|---:|---:|---:|---:|---:|
| Q1 | 274.5 | 12 | 297.3 | 12 | 295.2 | 12 | 286.9 | 12 |
| Q2a | 724.3 | 48 | 723.7 | 48 | 709.8 | 48 | 668.8 | 48 |
| Q2 | 356.4 | 36 | 326.4 | 36 | 323.1 | 36 | 320.6 | 36 |
| Q3 | 52.6 | 36 | 49.3 | 36 | 49.3 | 36 | 54.0 | 36 |
| Q4 | 39.6 | 36 | 47.9 | 36 | 53.6 | 36 | 42.9 | 36 |
| Q5 | 1662.3 | 180 | 1658.8 | 180 | 1683.4 | 180 | 1688.2 | 180 |
| Q6 | 484.1 | 393 | 574.1 | 297 | 613.4 | 357 | 583.4 | 405 |
| Q7 | 135.1 | 39 | 145.4 | 39 | 142.7 | 39 | 148.3 | 39 |
| Q8 | 118.3 | 39 | 130.4 | 39 | 128.3 | 39 | 124.9 | 39 |
| Q9 | 2492.2 | 636 | 2519.5 | 636 | 2576.9 | 636 | 2471.5 | 636 |
| QA | 372.1 | 81 | 383.5 | 81 | 396.3 | 81 | 374.1 | 81 |
| QB | 250.7 | 96 | 263.6 | 96 | 300.1 | 96 | 262.8 | 96 |
| QC | 34.1 | 12 | 39.9 | 12 | 38.7 | 12 | 32.3 | 12 |
| QD | 11.9 | 12 | 15.1 | 12 | 14.0 | 12 | 8.4 | 12 |
| QE | 165.1 | 42 | 182.5 | 42 | 188.6 | 42 | 177.6 | 42 |
| QF | 38.9 | 60 | 49.5 | 60 | 43.2 | 60 | 44.0 | 60 |
| QG | 628.6 | 309 | 735.0 | 309 | 711.0 | 309 | 696.7 | 309 |
| R1 | 62.0 | 39 | 66.4 | 39 | 66.0 | 39 | 58.6 | 39 |
| R2 | 146.1 | 39 | 149.4 | 39 | 151.7 | 39 | 144.9 | 39 |
| R3 | 254.4 | 39 | 264.1 | 39 | 261.3 | 39 | 251.7 | 39 |
| R4 | 103.4 | 414 | 123.1 | 414 | 110.9 | 414 | 109.5 | 414 |
| R5 | 108.5 | 414 | 113.0 | 414 | 115.8 | 414 | 116.7 | 414 |
| R6 | 70.4 | 252 | 83.4 | 252 | 85.3 | 252 | 79.8 | 252 |
| R7 | 11.2 | 6 | 12.3 | 6 | 12.4 | 6 | 6.7 | 6 |
| R8 | 21.2 | 36 | 23.6 | 36 | 22.6 | 36 | 17.1 | 36 |
| R9 | 232.6 | 39 | 239.1 | 39 | 66.4* | 39* | 61.1* | 39* |
| R10 | 127.0 | 78 | 131.5 | 78 | 134.9 | 78 | 129.9 | 78 |
| gql:GQ1 | 161.5 | 12 | 170.9 | 12 | 163.5 | 12 | 160.3 | 12 |
| gql:GQ2 | 32.4 | 12 | 36.0 | 12 | 36.3 | 12 | 31.4 | 12 |
| gql:GQ3 | 69.3 | 36 | 67.1 | 36 | 66.9 | 36 | 63.1 | 36 |
| gql:GQ4 | error | – | error | – | 103.3 | 48 | 96.7 | 48 |
| **all 30 queries every version ran** | **9241** | **3483** | **9622** | **3387** | **9562** | **3447** | **9216** | **3495** |

## Phase 7.3: plain search for small results

Measured 2026-11-26, same machine and method as above, `main` before and after the change, each on a fresh cluster.

| | before | 7.3 | change |
|---|---:|---:|---:|
| Scale 1: OpenSearch operations | 1,447 | 723 | **−50%** |
| Scale 1: of which scroll operations | 381 | 12 | |
| Scale 1: sum of p50 latencies | 1,455 ms | 1,343 ms | −8% |
| Scale 10: OpenSearch operations | 3,542 | 2,016 | **−43%** |
| Scale 10: of which scroll operations | 969 | 156 | |
| Scale 10: sum of p50 latencies | 8,954 ms | 9,024 ms | +1% |

Every query returns the same rows as before, at both scales, and none does more OpenSearch work. What changed: a
search now starts as a plain search; when the first page isn't full, or OpenSearch's exact hit count fits in it,
that page is the whole result: no scroll is opened, no empty follow-up request is sent, nothing is cleared (before,
every search cost a search, an empty scroll request and a clear). Only a search with more hits than one page (10,000)
continues as a scroll, which leaves out the documents already returned.

Latency barely moves on this data, because these requests are small and fast either way; the saving is in
OpenSearch's work, which is what a busier cluster pays for. One query got slower: Q9 at scale 10 (2.45 → 2.67 s,
+9%), the largest result (29,640 relationships) and the one that still needs the scroll; leaving out the first
10,000 documents costs some query time. Raw results: `benchmark-results/7.3-*.json`; the CI baseline is now the 7.3
scale-1 run.

### Scale 1

| Query | before ms | before ops | 7.3 ms | 7.3 ops |
|---|---:|---:|---:|---:|
| Q1 | 55.5 | 12 | 43.0 | 6 |
| Q2a | 118.9 | 36 | 114.3 | 18 |
| Q2 | 69.3 | 36 | 57.8 | 18 |
| Q3 | 36.5 | 36 | 33.3 | 18 |
| Q4 | 32.6 | 36 | 20.6 | 18 |
| Q5 | 154.1 | 63 | 145.6 | 27 |
| Q6 | 115.7 | 146 | 104.0 | 68 |
| Q7 | 33.1 | 39 | 31.5 | 21 |
| Q8 | 28.4 | 39 | 28.1 | 21 |
| Q9 | 229.1 | 78 | 252.6 | 65 |
| QA | 42.3 | 60 | 40.9 | 30 |
| QB | 50.9 | 96 | 40.9 | 36 |
| QC | 11.3 | 12 | 10.4 | 6 |
| QD | 8.6 | 10 | 7.7 | 4 |
| QE | 27.9 | 36 | 25.8 | 18 |
| QF | 23.3 | 57 | 19.8 | 27 |
| QG | 87.2 | 111 | 75.2 | 51 |
| R1 | 20.2 | 39 | 23.0 | 21 |
| R2 | 28.2 | 39 | 26.3 | 21 |
| R3 | 37.1 | 39 | 37.4 | 21 |
| R4 | 26.5 | 63 | 18.8 | 27 |
| R5 | 27.1 | 63 | 17.5 | 27 |
| R6 | 16.0 | 36 | 18.7 | 18 |
| R7 | 6.7 | 6 | 6.6 | 3 |
| R8 | 13.6 | 34 | 12.1 | 16 |
| R9 | 22.9 | 39 | 17.7 | 21 |
| R10 | 40.5 | 78 | 30.6 | 42 |
| gql:GQ1 | 26.1 | 12 | 26.1 | 6 |
| gql:GQ2 | 15.2 | 12 | 12.9 | 6 |
| gql:GQ3 | 22.5 | 36 | 20.5 | 18 |
| gql:GQ4 | 28.1 | 48 | 23.0 | 24 |
| **all 31** | **1455** | **1447** | **1343** | **723** |

### Scale 10

| Query | before ms | before ops | 7.3 ms | 7.3 ops |
|---|---:|---:|---:|---:|
| Q1 | 287.0 | 12 | 249.3 | 6 |
| Q2a | 646.7 | 48 | 608.2 | 24 |
| Q2 | 305.2 | 36 | 296.5 | 18 |
| Q3 | 44.7 | 36 | 44.2 | 18 |
| Q4 | 38.8 | 36 | 34.6 | 18 |
| Q5 | 1555.6 | 180 | 1559.3 | 120 |
| Q6 | 569.1 | 405 | 556.2 | 183 |
| Q7 | 134.2 | 39 | 127.0 | 21 |
| Q8 | 119.4 | 39 | 117.8 | 21 |
| Q9 | 2449.6 | 635 | 2673.9 | 588 |
| QA | 363.5 | 81 | 399.3 | 81 |
| QB | 244.7 | 96 | 245.2 | 36 |
| QC | 30.2 | 12 | 29.4 | 6 |
| QD | 8.1 | 12 | 6.8 | 6 |
| QE | 179.7 | 42 | 192.7 | 30 |
| QF | 36.4 | 60 | 31.5 | 30 |
| QG | 662.0 | 309 | 638.1 | 159 |
| R1 | 54.8 | 39 | 54.4 | 21 |
| R2 | 140.3 | 39 | 136.8 | 21 |
| R3 | 248.7 | 39 | 236.8 | 21 |
| R4 | 107.9 | 414 | 111.0 | 162 |
| R5 | 111.7 | 414 | 90.2 | 162 |
| R6 | 81.1 | 252 | 68.9 | 126 |
| R7 | 6.0 | 6 | 6.0 | 3 |
| R8 | 16.4 | 36 | 14.8 | 18 |
| R9 | 56.1 | 39 | 55.0 | 21 |
| R10 | 123.6 | 78 | 119.6 | 42 |
| gql:GQ1 | 153.1 | 12 | 151.3 | 6 |
| gql:GQ2 | 29.6 | 12 | 29.6 | 6 |
| gql:GQ3 | 58.3 | 36 | 54.1 | 18 |
| gql:GQ4 | 91.9 | 48 | 85.1 | 24 |
| **all 31** | **8954** | **3542** | **9024** | **2016** |

## Phase 7.4: growing bulks

Measured 2026-11-26 at scale 10, with the whole result in one page (as above) and with a 100-row first page
(`bench.py --page-size 100`), which is what a paging client asks for first.

**What the engine sends.** Captured from the search slow log, every step of a query already sends one search, not one
per id or per index: the previous step's ids go into one `terms` query, and all index partitions into one index list.
Steps depend on each other (each needs the previous step's ids), so one `msearch` per query has nothing to combine.
What multiplies the searches on large data is that a step reads its input in **bulks of 1,000 ids** and sends one
search per bulk: 10,000 dragons make R4 send 10 edge searches.

**Bulk size is a trade-off.** A fixed bulk of 10,000 halves OpenSearch work for whole results, but a 100-row first
page must then process 10,000 start entities: Q9's first page goes from 0.23 to 1.3 s. A growing bulk (1,000, then
doubling to 10,000, per step; `unipop.bulk.start`, `.multiplier`, `.max`) keeps the small first bulk and reaches big
bulks quickly:

| scale 10 | fixed 1,000 (before) | growing 1,000 → 10,000 | fixed 10,000 |
|---|---:|---:|---:|
| whole result: OpenSearch operations | 2,040 | **1,374 (−33%)** | 1,053 |
| whole result: sum of p50 latencies | 9,124 ms | 9,127 ms | 9,216 ms |
| 100-row page: OpenSearch operations | 1,242 | **900 (−28%)** | 957 |
| 100-row page: sum of p50 latencies | 2,526 ms | **2,387 ms (−6%)** | 3,930 ms |

Every query returns the same rows with all three. At scale 1 every step's input is under 1,000 ids, so nothing
changes there. Raw results: `benchmark-results/7.4-*.json`.

## Where the time goes (2026-11-27)

On the tutorial data, the share of a query's time spent in OpenSearch (the slowest shard of each search phase, from
the search slow log, summed) against the client-side total and the engine's own timings in the response
(`elapsed`, `renderElapsed`):

| query | total | OpenSearch | engine: traversal and row assembly | engine: JSON rendering | response |
|---|---:|---:|---:|---:|---:|
| Q9 (2,964 relationships) | 272 ms | 41 ms (15%) | ~170 ms | 41 ms | 2.7 MB |
| Q5 | 146 ms | 15 ms (10%) | ~75 ms | 33 ms | 2.3 MB |
| Q2a | 60 ms | 1 ms (2%) | ~34 ms | 13 ms | 1.1 MB |

Phases 7.3 and 7.4 cut OpenSearch's work by a third to a half, which a busy cluster feels, but most of the latency is
in the engine. Planning is small: about 3–4 ms per query warm, at most about 2 ms more with a cold count cache (7.6).
Next: profile the engine (ROADMAP 7.11).

## Phase 7.11: profiling the engine

Java Flight Recorder on Q9 and Q5 at scale 10 (8 runs each, `settings=profile`, full stacks via
`JAVA_TOOL_OPTIONS=-XX:FlightRecorderOptions=stackdepth=1024`). The largest single cost was **building text with
`String.format`**, 15–20% of the engine's CPU together with the regex `String.format` uses to parse its pattern:

- `Assignment.getId()` is a hash of the row's `toString()`, and Jackson calls it for every row it renders; that text
  includes every property, each formatted by `Property.toString` with `String.format`.
- `AssignmentsQueryResult.distinct` keys every row with `AssignmentDescriptor.print`, also `String.format`, and looked
  up each relationship's entities by scanning the row's entity list.

Fixed by building the same text with plain concatenation (a test pins it to the `String.format` output, because the
row ids are hashes of it) and indexing the entities by id. **Every response is byte-for-byte identical, row ids
included** (`docker/bench/snapshot.py`, all 31 queries at scale 10; two runs of the same build are identical too, so
the check is meaningful). OpenSearch work is unchanged.

Scale 10, same data and cluster, old image then new image: **sum of p50 latencies 9,702 → 8,585 ms
(-12%)**.

| query | before | after | change |
|---|---:|---:|---:|
| Q9 | 2,820 ms | 2,535 ms | -10% |
| Q5 | 1,636 ms | 1,495 ms | -9% |
| QG | 801 ms | 647 ms | -19% |
| Q6 | 644 ms | 554 ms | -14% |
| Q2a | 602 ms | 548 ms | -9% |
| QA | 440 ms | 346 ms | -21% |
| Q2 | 292 ms | 288 ms | -1% |
| QB | 279 ms | 220 ms | -21% |

After this the profile is flat: no method above about 6% of the samples (the largest are hashing of freshly built
strings, and parsing OpenSearch's JSON). Raw results: `benchmark-results/7.11-*.json`.

## Rerun it

```bash
docker compose -f docker/docker-compose-os.yml down            # free the fixed container names
docker/bench/compare_versions.sh /tmp/bench 10 "v0.8.0 1.3.0 v0.8.0" "v0.9.1 2.19.7 v0.9.1" "main 2.19.7 Dragons"
python3 docker/bench/bench.py --report /tmp/bench/v0.8.0-x10.json /tmp/bench/v0.9.1-x10.json /tmp/bench/main-x10.json
```

The images come from `docker/buildDocker.sh <tag>` on a checkout of each tag, or from
`ghcr.io/yang-db/yang.db:<version>` for releases from 0.9.1 on. One query against a running stack:
`python3 docker/bench/bench.py --only Q9 --runs 10`. All options are in the [benchmark README](../docker/bench/README.md).

## In CI

The Docker e2e job runs the benchmark at scale 1 after the e2e suite, adds the table to the run's summary, and
uploads the results. It fails if a query does more than 10% more OpenSearch work than
[docker/bench/baseline.json](../docker/bench/baseline.json) (the main results above). A change that makes queries
cheaper updates the baseline in the same pull request.
