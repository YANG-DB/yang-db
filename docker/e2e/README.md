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

When a query is added or changed in the docs, update `QUERIES` and `expectations()` in `doc_queries.py` to match.
Only `--reset` deletes data: it drops the Dragons indices of the stack you point it at.
