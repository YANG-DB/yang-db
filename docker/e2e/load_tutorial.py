#!/usr/bin/env python3
"""Load the Dragons tutorial data into a running quickstart stack, following the documented init sequence:

    GET  /fuse/idgen/init?names=Entity,Relation,Evalue,Rvalue,workerId
    POST /fuse/catalog/ontology                       (Dragons.json)
    GET  /fuse/load/ontology/Dragons/init             (creates the indices)
    POST /fuse/load/ontology/Dragons/csv/load?directive=INSERT&type=vertex|edge&label=<Type>   (every tutorial CSV)

Afterwards the stored document counts are compared with the CSV row counts (edges are stored twice: `.in` / `.out`).
Exits non-zero if any step, row or count fails. Python 3 standard library only.
"""
import argparse, csv, json, os, sys, urllib.error, urllib.parse, urllib.request

HERE = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.normpath(os.path.join(HERE, "..", ".."))
CSV_DIR = os.path.join(REPO, "docs", "tutorial", "sample", "dragons", "csv")
ONTOLOGY = os.path.join(REPO, "distribution", "resources", "assembly", "Dragons", "ontology", "Dragons.json")

# (csv file, vertex|edge, ontology label, index pattern holding the stored documents)
FILES = [
    ("Dragons.csv", "vertex", "Dragon", "dragon"),
    ("Person.csv", "vertex", "Person", "person"),
    ("horses.csv", "vertex", "Horse", "horse"),
    ("Kingdoms.csv", "vertex", "Kingdom", "kingdom"),
    ("Guilds.csv", "vertex", "Guild", "guild"),
    ("Fire.csv", "edge", "Fire", "idx_fire_*"),
    ("Freeze.csv", "edge", "Freeze", "idx_freeze_*"),
    ("KnowsPerson.csv", "edge", "Know", "know"),
    ("MembersOfGuilds.csv", "edge", "MemberOf", "memberof"),
    ("OriginatedDragons.csv", "edge", "OriginatedIn", "originatedin"),
    ("OriginatedHorse.csv", "edge", "OriginatedIn", "originatedin"),
    ("OwnsDragon.csv", "edge", "Own", "own"),
    ("OwnsHorse.csv", "edge", "Own", "own"),
    ("RegisteredGuilds.csv", "edge", "RegisteredIn", "registeredin"),
    ("SubjectOfPerson.csv", "edge", "SubjectOf", "subjectof"),
]


def http(method, url, body=None, timeout=600):
    """JSON request. `Accept: application/json` is required: without it the API renders Java toString() (A1)."""
    data = body if isinstance(body, bytes) else (json.dumps(body).encode() if body is not None else None)
    req = urllib.request.Request(url, data=data, method=method,
                                 headers={"Content-Type": "application/json", "Accept": "application/json"})
    with urllib.request.urlopen(req, timeout=timeout) as r:
        raw = r.read()
    return json.loads(raw) if raw else None


def count_outcomes(resp):
    """(successes, failures, first failure) summed over a LoadResponse {responses: [{successes, failures}]}."""
    ok, bad, first = 0, 0, None
    def walk(o):
        nonlocal ok, bad, first
        if isinstance(o, dict):
            if isinstance(o.get("successes"), list): ok += len(o["successes"])
            if isinstance(o.get("failures"), list) and o["failures"]:
                bad += len(o["failures"]); first = first or o["failures"][0]
            for k, v in o.items():
                if k not in ("successes", "failures"): walk(v)
        elif isinstance(o, list):
            for x in o: walk(x)
    walk(resp)
    return ok, bad, first


def error_text(err):
    """Short text of an error object; Java exceptions arrive as {message, stackTrace: [...]}."""
    if isinstance(err, dict):
        top = (err.get("stackTrace") or [{}])[0]
        return f"{err.get('errorCode') or err.get('localizedMessage') or err.get('message') or ''} " \
               f"at {top.get('className', '?')}.{top.get('methodName', '?')}".strip()
    return str(err)


def rows(path):
    with open(path, newline="") as f:
        return sum(1 for _ in csv.reader(f)) - 1


def doc_count(os_url, index):
    try:
        return http("GET", f"{os_url}/{index}/_count")["count"]
    except urllib.error.HTTPError as e:
        return f"HTTP {e.code}"


def load(fuse, os_url, csv_dir=CSV_DIR, ontology=ONTOLOGY, reset=False):
    """Run the init sequence and load every CSV. Returns the number of problems found."""
    problems = 0
    def step(name, fn, tolerate=None):
        nonlocal problems
        try:
            r = fn()
            err = None
            if isinstance(r, dict):
                err = r.get("error") or (r["data"].get("error") if isinstance(r.get("data"), dict) else None)
            print(f"  {'FAIL' if err else 'ok  '}  {name}" + (f": {error_text(err)[:200]}" if err else ""))
            problems += bool(err)
            return r
        except urllib.error.HTTPError as e:
            body = e.read()[:300].decode(errors="replace")
            if tolerate and tolerate in body:
                print(f"  ok    {name}: {tolerate}")
                return None
            print(f"  FAIL  {name}: HTTP {e.code} {body[:200]}")
        except Exception as e:  # noqa: BLE001 - report and continue with the next step
            print(f"  FAIL  {name}: {type(e).__name__}: {e}")
        problems += 1

    print(f"Init sequence against {fuse}")
    if reset:
        step("GET load/ontology/Dragons/drop (reset)", lambda: http("GET", f"{fuse}/load/ontology/Dragons/drop"))
    # not idempotent: answers HTTP 500 "already exists" once the id generator index is there
    step("GET idgen/init", lambda: http("GET", f"{fuse}/idgen/init?names=Entity,Relation,Evalue,Rvalue,workerId"),
         tolerate="already exists")
    with open(ontology) as f:
        onto = json.load(f)
    step("POST catalog/ontology (Dragons)", lambda: http("POST", f"{fuse}/catalog/ontology", onto))
    step("GET load/ontology/Dragons/init", lambda: http("GET", f"{fuse}/load/ontology/Dragons/init"))

    print("\nCSV load (directive=INSERT)")
    expected = {}
    for name, kind, label, index in FILES:
        path = os.path.join(csv_dir, name)
        n = rows(path)
        expected[index] = expected.get(index, 0) + n * (2 if kind == "edge" else 1)
        qs = urllib.parse.urlencode({"directive": "INSERT", "type": kind, "label": label})
        try:
            with open(path, "rb") as f:
                resp = http("POST", f"{fuse}/load/ontology/Dragons/csv/load?{qs}", f.read())
            ok, bad, first = count_outcomes(resp)
            if isinstance(resp, dict) and resp.get("error"):  # whole-file failure, e.g. an unparsable partition date
                bad, first = max(bad, 1), first or error_text(resp["error"])
            verdict = "ok  " if bad == 0 else "FAIL"
            print(f"  {verdict}  {name:22} -> {label:12} rows={n:6}  successes={ok}  failures={bad}"
                  + (f"  first: {json.dumps(first, default=str)[:200]}" if first else ""))
            problems += bad > 0
        except Exception as e:  # noqa: BLE001
            print(f"  FAIL  {name:22} -> {label:12} {type(e).__name__}: {str(e)[:200]}")
            problems += 1

    print("\nStored documents (edges are stored twice, as .in and .out)")
    http("POST", f"{os_url}/_refresh")
    for index, want in expected.items():
        got = doc_count(os_url, index)
        verdict = "ok  " if got == want else "FAIL"
        problems += got != want
        print(f"  {verdict}  {index:14} stored={got}  expected={want}")
    return problems


def main(argv=None):
    p = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    p.add_argument("--fuse", default="http://localhost:8888/fuse", help="Fuse base URL")
    p.add_argument("--opensearch", default="http://localhost:9200", help="OpenSearch URL (to verify stored counts)")
    p.add_argument("--csv-dir", default=CSV_DIR)
    p.add_argument("--ontology", default=ONTOLOGY)
    p.add_argument("--reset", action="store_true", help="drop the Dragons indices first (needed to reload: INSERT rejects existing ids)")
    a = p.parse_args(argv)
    problems = load(a.fuse, a.opensearch, a.csv_dir, a.ontology, a.reset)
    print(f"\n{'LOAD OK' if not problems else f'LOAD FAILED: {problems} problem(s)'}")
    return 1 if problems else 0


if __name__ == "__main__":
    sys.exit(main())
