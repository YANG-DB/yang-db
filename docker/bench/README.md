# Benchmark

Measures every query of the Dragons playground (the 27 Cypher and 4 GraphQL queries the e2e suite checks) against a
running quickstart stack. For each query it reports:

- **latency**, p50 and p95 over the measured runs (client side, wall clock)
- **OpenSearch work**: shard-level search operations (query, fetch, scroll) plus gets, measured as the change in
  `GET /_stats/search,get` around each run. With nothing else using the cluster this is exact and the same on every
  run and every machine, so it's the number to compare between changes. Latency depends on the machine.
- **result size**: entities and relationships returned

Results and the comparison between releases are in [docs/benchmark.md](../../docs/benchmark.md).

## Run it

```bash
cd docker && docker compose -f docker-compose-os.yml up -d     # the quickstart stack
python3 bench/bench.py --load                    # load the tutorial data, then benchmark (31 queries x 5 runs)
python3 bench/bench.py --load --scale 10         # 10 copies of the tutorial data (~220k rows, ~440k documents)
python3 bench/bench.py --only Q5 Q9 --runs 10    # some queries only
```

Options: `--runs` (default 5) and `--warmup` (default 1, unmeasured, so the planner's cached counts are in place),
`--out results.json`, `--summary file` (appends the markdown table, e.g. to `$GITHUB_STEP_SUMMARY`).
Each query is deleted after its run: the engine keeps queries in memory until their TTL passes (Q14 in
[KNOWN_ISSUES](../../docs/revival/KNOWN_ISSUES.md)).

**Bigger data.** `scale_data.py N DIR` writes N disjoint copies of the tutorial CSVs (copy `k` prefixes every id with
`s<k>_`), so a query that isn't limited or filtered on one id returns N times the tutorial result. `--load --scale N`
uses it and posts the CSVs in pieces of 20,000 rows: a whole large file fails to load (L5).

**Compare releases.** `compare_versions.sh OUT SCALE "label os-version image-tag" ...` runs the benchmark against
each version on a fresh cluster, one after another. Then `bench.py --report OUT/a.json OUT/b.json ...` prints one
table.

```bash
docker compose -f docker/docker-compose-os.yml down      # the container names are fixed: free them first
docker/bench/compare_versions.sh /tmp/bench 1 "v0.8.0 1.3.0 v0.8.0" "v0.9.1 2.19.7 v0.9.1" "main 2.19.7 Dragons"
python3 docker/bench/bench.py --report /tmp/bench/v0.8.0-x1.json /tmp/bench/v0.9.1-x1.json /tmp/bench/main-x1.json
```

**Check that results don't change.** `snapshot.py OUT_DIR` saves every query's full response, with only the
fields that differ between runs removed (request id, timings, query and cursor ids). Snapshot before and after a
change that should be invisible and `diff -r` the two folders: every file must be identical.

**Profile the engine.** Start the engine with full stacks for Java Flight Recorder (a compose override with
`environment: [JAVA_TOOL_OPTIONS=-XX:FlightRecorderOptions=stackdepth=1024]` for the `fuse` service), then
`docker exec dragons-graph jcmd 7 JFR.start name=p settings=profile filename=/tmp/p.jfr`, run the queries,
`JFR.stop name=p`, `docker cp dragons-graph:/tmp/p.jfr .` and read it with `jfr print --events jdk.ExecutionSample`
or JDK Mission Control.

## In CI

The Docker e2e job runs `bench.py` on the tutorial data after the e2e suite. It writes the table to the job summary
and uploads `bench.json`, and it **fails if any query does more OpenSearch work than
[baseline.json](baseline.json)**. When a change makes queries do less work, update the baseline in the same pull
request:

```bash
python3 docker/bench/bench.py --load --write-baseline docker/bench/baseline.json
```
