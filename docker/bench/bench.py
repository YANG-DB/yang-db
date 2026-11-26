#!/usr/bin/env python3
"""Benchmark: latency and OpenSearch work per query, on the Dragons data, against a running quickstart stack.

Runs every query of the Dragons playground (the same set the e2e suite checks: Cypher and GraphQL), one at a time.
For each query it records:
  - latency: p50 / p95 over the measured runs (wall clock, client side; noisy on shared machines)
  - OpenSearch work per run: shard-level search operations (query, fetch, scroll) and gets, the deltas of
    `GET /_stats/search,get` around the run. With nothing else using the cluster these are exact and repeatable,
    which makes them the number to compare between changes (Phase 7 in docs/revival/ROADMAP.md).
  - result size: entities and relationships returned
Each query first runs `--warmup` times unmeasured, so the planner's cached counts are in place.

    python3 docker/bench/bench.py                                  # tutorial data already loaded
    python3 docker/bench/bench.py --load --scale 10                # load 10 copies of the tutorial data first
    python3 docker/bench/bench.py --baseline docker/bench/baseline.json   # compare OpenSearch work with a baseline
    python3 docker/bench/bench.py --out bench.json --write-baseline docker/bench/baseline.json
    python3 docker/bench/bench.py --report old.json new.json       # one comparison table from several results files

With --baseline, exits non-zero if any query fails, or does more than 10% more OpenSearch work than the baseline
(a fresh load can change a few queries' plans slightly; see compare()). Less work is reported, not an error: update
the baseline in the same pull request. Python 3 standard library only.
"""
import argparse, datetime, json, math, os, re, subprocess, sys, tempfile, time

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(HERE, "..", "e2e"))
import doc_queries  # noqa: E402  (query set and the run helper)
import load_tutorial  # noqa: E402
from load_tutorial import http  # noqa: E402
import scale_data  # noqa: E402

OPS = ("query", "fetch", "scroll", "get")
PAGE_SIZE = 100000  # rows in the first (only) page each run fetches; --page-size


def os_ops(os_url):
    """Cluster-wide counters: shard-level query/fetch/scroll operations and gets, since the node started."""
    t = http("GET", f"{os_url}/_stats/search,get")["_all"]["total"]
    s, g = t["search"], t["get"]
    return {"query": s["query_total"], "fetch": s["fetch_total"], "scroll": s["scroll_total"], "get": g["total"]}


def percentile(values, p):
    """Nearest-rank percentile."""
    v = sorted(values)
    return v[max(0, math.ceil(p / 100 * len(v)) - 1)]


def queries():
    """[(id, language, query)]: the playground's Cypher examples, then its GraphQL examples (ids prefixed gql:)."""
    out = [(qid, "cypher", q) for _doc, qid, q, _known in doc_queries.QUERIES]
    out += [(f"gql:{qid}", "graphQL", q) for _doc, qid, q, _known in doc_queries.GRAPHQL_QUERIES]
    return out


CLOCK_REFUSAL = re.compile(r"Clock moved backwards\. Refusing for (\d+) seconds")


def run_query(fuse, language, query, retries=3):
    """doc_queries.run, but waits out an engine that refuses requests after the clock stepped back (A11 in
    KNOWN_ISSUES: releases up to 0.9.1), so older versions can still be measured."""
    for _ in range(retries):
        got = doc_queries.run(fuse, query, language=language, page_size=PAGE_SIZE)
        m = CLOCK_REFUSAL.search(str(got.get("error") or ""))
        if not m:
            return got
        print(f"    (engine refuses requests for {m.group(1)} s after a clock step: waiting)")
        time.sleep(int(m.group(1)) + 2)
    return got


def run_and_drop(fuse, language, query):
    """One run, then DELETE the query it created, as a client should: the engine keeps every query (cursor, results)
    in memory until its TTL (5 minutes) passes, so hundreds of benchmark runs on big data would fill its heap."""
    got = run_query(fuse, language, query)
    qid = got.get("raw", {}).get("data", {}).get("resourceId") if isinstance(got.get("raw"), dict) else None
    if qid:
        try:
            http("DELETE", f"{fuse}/query/{qid}")
        except Exception:  # noqa: BLE001 - cleanup only
            pass
    return got


def measure(fuse, os_url, language, query, runs, warmup):
    for _ in range(warmup):
        got = run_and_drop(fuse, language, query)
        if "error" in got or got["errors"]:
            return {"error": got.get("error") or got["errors"][0]}
    times, ops = [], []
    for _ in range(runs):
        before = os_ops(os_url)
        t0 = time.perf_counter()
        got = doc_queries.run(fuse, query, language=language, page_size=PAGE_SIZE)
        times.append((time.perf_counter() - t0) * 1000)
        after = os_ops(os_url)
        if CLOCK_REFUSAL.search(str(got.get("error") or "")):  # A11: wait it out, then measure this run again
            run_query(fuse, language, "MATCH (d:Dragon) RETURN d LIMIT 1")
            return measure(fuse, os_url, language, query, runs, 0)
        qid = got.get("raw", {}).get("data", {}).get("resourceId") if isinstance(got.get("raw"), dict) else None
        if qid:  # outside the measured window; see run_and_drop
            try:
                http("DELETE", f"{fuse}/query/{qid}")
            except Exception:  # noqa: BLE001
                pass
        if "error" in got or got["errors"]:
            return {"error": got.get("error") or got["errors"][0]}
        ops.append({k: after[k] - before[k] for k in OPS})
    stable = all(o == ops[0] for o in ops)
    return {"p50_ms": round(percentile(times, 50), 1), "p95_ms": round(percentile(times, 95), 1),
            "ops": ops[0] if stable else min(ops, key=lambda o: sum(o.values())),
            "ops_stable": stable, "ops_total": sum(ops[0].values()) if stable else None,
            "rows": {"entities": sum(got["ents"].values()), "relationships": got["rels"]}}


def git_sha():
    try:
        return subprocess.run(["git", "rev-parse", "--short", "HEAD"], cwd=HERE, capture_output=True, text=True,
                              check=True).stdout.strip()
    except (OSError, subprocess.CalledProcessError):
        return None


def total_ops(r):
    return sum(r["ops"].values())


def table(results):
    lines = ["| Query | p50 ms | p95 ms | OpenSearch ops (query/fetch/scroll/get) | Total ops | Rows (ents/rels) |",
             "|---|---:|---:|---|---:|---:|"]
    for qid, r in results.items():
        if "error" in r:
            lines.append(f"| {qid} | ERROR | | {str(r['error'])[:80]} | | |")
            continue
        o = r["ops"]
        flag = "" if r["ops_stable"] else " (varied)"
        lines.append(f"| {qid} | {r['p50_ms']} | {r['p95_ms']} | {o['query']}/{o['fetch']}/{o['scroll']}/{o['get']}{flag}"
                     f" | {total_ops(r)} | {r['rows']['entities']}/{r['rows']['relationships']} |")
    ok = [r for r in results.values() if "error" not in r]
    lines.append(f"| **all {len(ok)}** | {round(sum(r['p50_ms'] for r in ok), 1)} | | | "
                 f"{sum(total_ops(r) for r in ok)} | |")
    return "\n".join(lines)


TOLERANCE = 0.10  # see compare()


def compare(results, baseline):
    """(problems, report lines) of OpenSearch work against a baseline run.

    Op counts repeat exactly within a run, but after a fresh load the planner can settle on a different, equally costed
    plan for a few queries (seen: 77 vs 78 ops at scale 1; 297-393 at scale 10). So a query fails only when it does
    more than TOLERANCE (and at least 2 ops) above its baseline; smaller differences are reported."""
    problems, lines = 0, []
    base = baseline["queries"]
    for qid, r in results.items():
        b = base.get(qid)
        if "error" in r:
            problems += 1
            lines.append(f"  FAIL  {qid}: error: {str(r['error'])[:150]}")
        elif b is None or "error" in b:
            lines.append(f"  new   {qid}: {total_ops(r)} ops (not in the baseline)")
        elif total_ops(r) > max(total_ops(b) * (1 + TOLERANCE), total_ops(b) + 1):
            problems += 1
            lines.append(f"  MORE  {qid}: {total_ops(b)} -> {total_ops(r)} ops  {b['ops']} -> {r['ops']}")
        elif total_ops(r) > total_ops(b):
            lines.append(f"  more  {qid}: {total_ops(b)} -> {total_ops(r)} ops (within {TOLERANCE:.0%})")
        elif total_ops(r) < total_ops(b):
            lines.append(f"  less  {qid}: {total_ops(b)} -> {total_ops(r)} ops  (update the baseline)")
        if b and "error" not in r and "error" not in b and r["rows"] != b["rows"]:
            lines.append(f"  note  {qid}: rows {b['rows']} -> {r['rows']}")
    for qid in base.keys() - results.keys():
        lines.append(f"  gone  {qid} (in the baseline, not run)")
    return problems, lines


def report(paths):
    """Markdown comparison of several results files: p50 latency and OpenSearch ops per query, one column pair per
    file. A cell is "error" when that version failed the query, and marked * when its result size differs from the
    first file's (that version returns different rows, so the numbers aren't like for like)."""
    runs = []
    for path in paths:
        with open(path) as f:
            runs.append(json.load(f))
    labels = [r["meta"].get("label") or os.path.basename(p) for r, p in zip(runs, paths)]
    head = "| Query | " + " | ".join(f"{l} ms | {l} ops" for l in labels) + " |"
    lines = [head, "|---|" + "---:|---:|" * len(runs)]
    ref = runs[0]["queries"]
    for qid in ref:
        cells = []
        for r in runs:
            q = r["queries"].get(qid)
            if q is None:
                cells += ["–", "–"]
            elif "error" in q:
                cells += ["error", "–"]
            else:
                mark = "" if "error" in ref[qid] or q["rows"] == ref[qid]["rows"] else "*"
                cells += [f"{q['p50_ms']}{mark}", f"{total_ops(q)}{mark}"]
        lines.append(f"| {qid} | " + " | ".join(cells) + " |")
    common = [q for q in ref if all(q in r["queries"] and "error" not in r["queries"][q] for r in runs)]
    cells = []
    for r in runs:
        qs = r["queries"]
        cells += [f"**{round(sum(qs[q]['p50_ms'] for q in common))}**", f"**{sum(total_ops(qs[q]) for q in common)}**"]
    lines.append(f"| **{len(common)} queries all versions run** | " + " | ".join(cells) + " |")
    meta = [f"- {l}: scale {r['meta']['scale']}, {r['meta']['stored_docs']} stored docs, {r['meta']['runs']} runs, "
            f"git {r['meta'].get('git')}, {r['meta']['date']}" for l, r in zip(labels, runs)]
    return "\n".join(lines) + "\n\n" + "\n".join(meta)


def main(argv=None):
    p = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    p.add_argument("--fuse", default="http://localhost:8888/fuse")
    p.add_argument("--opensearch", default="http://localhost:9200")
    p.add_argument("--load", action="store_true", help="reset the Dragons indices and load the data first")
    p.add_argument("--scale", type=int, default=1, help="with --load: copies of the tutorial data (default 1)")
    p.add_argument("--chunk-rows", type=int, default=20000,
                   help="with --load: post CSVs in pieces of this many rows (default 20000; whole files fail: L5)")
    p.add_argument("--runs", type=int, default=5, help="measured runs per query (default 5)")
    p.add_argument("--warmup", type=int, default=1, help="unmeasured runs per query first (default 1)")
    p.add_argument("--page-size", type=int, default=100000,
                   help="rows per run: the first page only (default 100000: the whole result)")
    p.add_argument("--label", help="a name for this run, kept in the results (e.g. the version)")
    p.add_argument("--report", nargs="+", metavar="RESULTS", help="print a comparison table of these results files")
    p.add_argument("--only", nargs="*", help="run only these query ids")
    p.add_argument("--out", help="write the results as JSON")
    p.add_argument("--baseline", help="compare OpenSearch work with this results file; exit 1 if any query does more")
    p.add_argument("--write-baseline", help="write the results to this file as the new baseline")
    p.add_argument("--summary", help="append the results table (markdown) to this file, e.g. $GITHUB_STEP_SUMMARY")
    a = p.parse_args(argv)
    global PAGE_SIZE
    PAGE_SIZE = a.page_size
    if a.report:
        print(report(a.report))
        return 0

    if a.load:
        with tempfile.TemporaryDirectory() as d:
            rows = sum(scale_data.scale(a.scale, d).values())
            print(f"Loading {a.scale} cop{'y' if a.scale == 1 else 'ies'} of the tutorial data ({rows} rows)")
            t0 = time.perf_counter()
            if load_tutorial.load(a.fuse, a.opensearch, csv_dir=d, reset=True, chunk_rows=a.chunk_rows):
                print("LOAD FAILED"); return 1
            print(f"Loaded in {time.perf_counter() - t0:.0f} s\n")
    stored = http("GET", f"{a.opensearch}/_stats/docs")["_all"]["primaries"]["docs"]["count"]

    results = {}
    for qid, language, query in queries():
        if a.only and qid not in a.only:
            continue
        results[qid] = r = measure(a.fuse, a.opensearch, language, query, a.runs, a.warmup)
        print(f"  {qid:12} " + (f"ERROR {str(r['error'])[:120]}" if "error" in r else
                                f"p50 {r['p50_ms']:8.1f} ms  p95 {r['p95_ms']:8.1f} ms  ops {total_ops(r):5}"
                                + ("" if r["ops_stable"] else " (varied between runs)")))

    out = {"meta": {"label": a.label, "scale": a.scale, "page_size": a.page_size, "runs": a.runs, "warmup": a.warmup, "stored_docs": stored, "git": git_sha(),
                    "date": datetime.datetime.now(datetime.timezone.utc).isoformat(timespec="seconds")},
           "queries": results}
    md = table(results)
    print("\n" + md)
    for path in filter(None, [a.out, a.write_baseline]):
        with open(path, "w") as f:
            json.dump(out, f, indent=1, sort_keys=False)
            f.write("\n")
    problems = sum("error" in r for r in results.values())
    diffs = []
    if a.baseline:
        with open(a.baseline) as f:
            baseline = json.load(f)
        if baseline["meta"]["scale"] != a.scale:
            print(f"\nBaseline is for scale {baseline['meta']['scale']}, this run is scale {a.scale}: not compared")
        else:
            problems, diffs = compare(results, baseline)
            print(f"\nOpenSearch work against {a.baseline} (git {baseline['meta'].get('git')}):")
            print("\n".join(diffs) if diffs else "  same for every query")
    if a.summary:
        with open(a.summary, "a") as f:
            f.write(f"### Benchmark (scale {a.scale}, {stored} stored docs, {a.runs} runs per query)\n\n{md}\n\n")
            if a.baseline and diffs:
                f.write("Against the baseline:\n\n```\n" + "\n".join(diffs) + "\n```\n")
    print(f"\n{'BENCH OK' if not problems else f'BENCH: {problems} problem(s)'}")
    return 1 if problems else 0


if __name__ == "__main__":
    sys.exit(main())
