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
- **A clock step stops the engine** (A11): when the VM's clock stepped back about 50 seconds, every request failed
  for 47 seconds ("Clock moved backwards"). That run was discarded and repeated.

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
