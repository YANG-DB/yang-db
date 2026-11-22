# End-to-end doc-query suite

Checks that every example query in the Dragons docs returns the right result against the
[quickstart stack](../README.md). Python 3, standard library only.

```bash
docker compose -f docker/docker-compose-os.yml up -d          # or `start` if the containers exist
python3 docker/e2e/doc_queries.py --load --reset               # load the tutorial data, then run every query
```

- `load_tutorial.py` runs the documented init sequence - `idgen/init` → `POST catalog/ontology` (Dragons.json) →
  `load/ontology/Dragons/init` → `csv/load?directive=INSERT` for every CSV in
  [docs/tutorial/sample/dragons/csv](../../docs/tutorial/sample/dragons/csv) - and then compares the stored document
  counts with the CSV row counts. `--reset` drops the Dragons indices first (`INSERT` rejects ids that already exist,
  so a reload needs it). Run it on its own to just load the data.
- `doc_queries.py` computes each query's expected result from the documents **stored in OpenSearch** (not from the
  CSVs), runs the query through `/fuse/query/cypher/run`, and compares entity ids per type and distinct relationship
  ids. It also checks the cursor types (`count`, `graph`, `csv`, `forwardPaths`, `projection`). Without `--load` it
  runs against whatever data is loaded. `--only Q2 QE` runs a subset.

Output per query: `PASS`, `FAIL` or `ERROR`. A failure caused by an open issue in
[KNOWN_ISSUES.md](../../docs/revival/KNOWN_ISSUES.md) prints the issue ids, e.g. `FAIL (known: Q1, Q4)`.
`PASS (vacuous: expected nothing)` means the expected result is empty, so the check proves little. The exit code
is non-zero if anything is not passing, known or not.

Options: `--fuse` (default `http://localhost:8888/fuse`), `--opensearch` (default `http://localhost:9200`).

## In CI
The same suite runs on every pull request as the **Docker e2e** job of [CI](../../.github/workflows/ci.yml). On a
failure, the job uploads the suite output and the YANG-DB and OpenSearch container logs as the `e2e-logs` artifact.

## Marking a commit as verified on Docker
`verify_and_mark.sh` runs the suite with `--load --reset` and publishes the result as a GitHub **commit status**
named `e2e/docker` on the current commit. It shows as ✓ or ✗ next to the commit on GitHub, like a CI check, so
anyone can see which commit was verified end to end and with what result.

```bash
docker/buildDocker.sh Dragons && docker compose -f docker/docker-compose-os.yml up -d --force-recreate fuse
docker/e2e/verify_and_mark.sh             # needs `gh` logged in, and HEAD pushed
docker/e2e/verify_and_mark.sh --no-mark   # run without publishing
```
Only mark a commit when the running image was built from it.

The list of queries is **not** in this folder: `doc_queries.py` reads it from the Dragons playground spec,
[examples/dragons/ext/src/main/resources/dragons/playground/openapi.json](../../examples/dragons/ext/src/main/resources/dragons/playground/openapi.json)
(the examples of the `cypher` parameter, each with `x-doc` and `x-known`). Every query offered at `/dragons/swagger` is
therefore verified here. With `--load`, the suite also checks the playground: the page, the spec, and that
`POST /fuse/dragons/tutorial/load` reloads exactly the tutorial data.

When a query is added or changed in the docs, update the spec's examples and `expectations()` in `doc_queries.py` to match.
Only `--reset` deletes data: it drops the Dragons indices of the stack you point it at.
