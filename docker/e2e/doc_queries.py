#!/usr/bin/env python3
"""End-to-end check of every example query in the Dragons docs against a running quickstart stack.

Expected results are computed from the documents actually stored in OpenSearch (not from the CSVs), then compared
with what the engine returns: entity ids per type and distinct relationship ids. Each query lists the open issues
(docs/revival/KNOWN_ISSUES.md) it currently depends on, so a failure prints e.g. `FAIL (known: Q1)`.

    python3 docker/e2e/doc_queries.py                  # data already loaded
    python3 docker/e2e/doc_queries.py --load --reset   # (re)load the tutorial data first, then run

Exits non-zero if any query or cursor check does not pass (known or not). Python 3 standard library only.
"""
import argparse, json, os, sys, urllib.error, urllib.parse, urllib.request
from collections import defaultdict

import load_tutorial
from load_tutorial import http

# The queries come from the Dragons playground spec (served at /dragons/swagger), so every example offered there is
# verified here. Each example carries its doc page (x-doc) and the issues it once depended on (x-known).
SPEC = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "..", "examples", "dragons", "ext", "src", "main",
                    "resources", "dragons", "playground", "openapi.json")


def spec_queries(spec_path=SPEC):
    """[(doc, id, cypher, known issues)] from the cypher parameter's examples, in spec order."""
    spec = json.load(open(spec_path))
    params = spec["paths"]["/query/cypher/run"]["get"]["parameters"]
    examples = next(p for p in params if p["name"] == "cypher")["examples"]
    return [(e["x-doc"], qid, e["value"], e.get("x-known", [])) for qid, e in examples.items()]


QUERIES = spec_queries()


def scan(os_url, index):
    """All _source docs of an index (pattern) via scroll."""
    out, r = [], http("POST", f"{os_url}/{index}/_search?scroll=2m&size=5000", {"query": {"match_all": {}}})
    while r["hits"]["hits"]:
        out += [h["_source"] for h in r["hits"]["hits"]]
        r = http("POST", f"{os_url}/_search/scroll", {"scroll": "2m", "scroll_id": r["_scroll_id"]})
    return out


def expectations(os_url):
    """Expected result per query id, from stored data: {"ents": {type: count}, "rels": count}."""
    ent = {t: {d["id"]: d for d in scan(os_url, t.lower())} for t in ["Dragon", "Person", "Horse", "Kingdom", "Guild"]}

    def edges(index):  # one tuple per logical edge (its "out" document): (typeA, idA, typeB, idB, temperature)
        return [(d["entityA"]["type"], d["entityA"]["id"], d["entityB"]["type"], d["entityB"]["id"], d.get("temperature"))
                for d in scan(os_url, index) if d.get("direction") == "out"]

    def exists(t, i): return i in ent[t]
    def prop(t, i, p): return ent[t].get(i, {}).get(p)
    def funds(k):
        try: return float(prop("Kingdom", k, "funds") or 0)
        except ValueError: return 0

    E = {"Own": edges("own"), "Know": edges("know"), "MemberOf": edges("memberof"), "Fire": edges("idx_fire_*"),
         "RegisteredIn": edges("registeredin"), "OriginatedIn": edges("originatedin")}
    know = [e for e in E["Know"] if exists("Person", e[1]) and exists("Person", e[3])]
    fire = [e for e in E["Fire"] if exists("Dragon", e[1]) and exists("Dragon", e[3])]
    own_pd, own_ph = defaultdict(set), defaultdict(set)
    for a_t, a, b_t, b, _ in E["Own"]:
        if a_t == "Person" and exists("Person", a):
            if b_t == "Dragon" and exists("Dragon", b): own_pd[a].add(b)
            if b_t == "Horse" and exists("Horse", b): own_ph[a].add(b)
    fire_nb = defaultdict(set)
    for _, a, _, b, _ in fire: fire_nb[a].add(b); fire_nb[b].add(a)
    know_nb = defaultdict(set)
    for _, a, _, b, _ in know: know_nb[a].add(b); know_nb[b].add(a)

    exp = {"Q1": {"ents": {"Dragon": len(ent["Dragon"])}, "rels": 0},
           "Q2a": {"rels": len(know)},
           "Q2": {"rels": sum(1 for e in know if "FEMALE" in (prop("Person", e[1], "gender"), prop("Person", e[3], "gender")))}}
    exp["Q3"] = {"rels": sum(1 for e in E["RegisteredIn"] if e[0] == "Guild" and exists("Guild", e[1])
                             and e[2] == "Kingdom" and funds(e[3]) > 0)}
    exp["Q4"] = exp["Q3"]  # RegisteredIn is stored Guild -> Kingdom, so the directed form returns the same relationships
    exp["Q5"] = {"rels": sum(1 for e in fire if "BLACK" in (prop("Dragon", e[1], "color"), prop("Dragon", e[3], "color")))}
    # Q6: female p -Own- black dragon d that fired on/was fired by a male dragon; p -Own- horse from a kingdom with funds > 0
    orig_ok = {e[1] for e in E["OriginatedIn"] if e[0] == "Horse" and e[2] == "Kingdom" and funds(e[3]) > 0}
    q6 = {p for p in own_pd if prop("Person", p, "gender") == "FEMALE"
          and any(prop("Dragon", d, "color") == "BLACK" and any(prop("Dragon", o, "gender") == "MALE" for o in fire_nb[d])
                  for d in own_pd[p])
          and any(h in orig_ok for h in own_ph[p])}
    exp["Q6"] = {"ents": {"Person": len(q6)}}
    # count(k) > n keeps the p1 persons with more than n (filtered) outgoing Know edges; return * also yields their p2s
    def count_filter(n, p2_ok):
        out = defaultdict(set)
        for _, a, _, b, _ in know:
            if p2_ok(b): out[a].add(b)
        p1 = {a for a, bs in out.items() if len(bs) > n}
        return {"ents": {"Person": len(p1 | set().union(*[out[a] for a in p1]))},
                "note": f"({len(p1)} matching p1 persons)"}
    exp["Q7"] = count_filter(15, lambda b: True)
    # regressions: count-filter operators (C1), more than 10 matching groups (C2), relationship property map (Q10)
    def count_cmp(ok):
        p1 = {a for a, bs in know_out_all.items() if ok(len(bs))}
        return {"ents": {"Person": len(p1 | set().union(*[know_out_all[a] for a in p1]))}, "note": f"({len(p1)} matching p1 persons)"}
    know_out_all = defaultdict(set)
    for _, a, _, b, _ in know: know_out_all[a].add(b)
    exp["R1"] = count_cmp(lambda n: n == 16)
    exp["R2"] = count_cmp(lambda n: n >= 15)
    exp["R3"] = count_cmp(lambda n: n > 3)
    exp["R4"] = {"rels": sum(1 for e in fire if e[4] is not None and float(e[4]) == 373)}
    exp["R5"] = exp["R4"]
    exp["R6"] = {"rels": 0}
    braden = {i for i, d in ent["Person"].items() if d.get("name") == "Braden Heaney"}
    exp["R7"] = {"ents": {"Person": sum(1 for i in braden if ent["Person"][i].get("gender") == "FEMALE")}}
    exp["R8"] = {"rels": sum(1 for _, a, _, b, _ in know if a in braden)}  # no fire has date == 1 ms; before the fix the second property was ignored (32)
    # the count is taken over all Know edges (they don't store p2's gender); the row filter then restricts the p2s
    know_out = defaultdict(set)
    for _, a, _, b, _ in know: know_out[a].add(b)
    q8_p1 = {a for a, bs in know_out.items() if len(bs) > 10}
    q8_p2 = {b for a in q8_p1 for b in know_out[a] if prop("Person", b, "gender") == "FEMALE"}
    q8_p1 = {a for a in q8_p1 if any(prop("Person", b, "gender") == "FEMALE" for b in know_out[a])}
    exp["Q8"] = {"ents": {"Person": len(q8_p1 | q8_p2)}, "note": f"({len(q8_p1)} matching p1 persons)"}
    exp["Q9"] = {"rels": len({(e[1], e[3]) for e in fire})}
    exp["QA"] = {"ents": {t: len(v) for t, v in ent.items()}}
    exp["QB"] = {"ents": {"Person": len(ent["Person"]), "Dragon": len(ent["Dragon"])}}
    exp["QC"] = {"ents": {"Person": len(ent["Person"])}}
    exp["QD"] = {"ents": {"Person": sum(1 for p in ent["Person"].values() if p.get("name") == "Braden Heaney")}}
    exp["QE"] = {"rels": sum(len(ds) for ds in own_pd.values())}
    bolling = {g for g, d in ent["Guild"].items() if d.get("name") == "Bolling"}
    members = {e[1] for e in E["MemberOf"] if e[3] in bolling and exists("Person", e[1])}
    qf = {p for p in members if own_pd[p]}
    exp["QF"] = {"ents": {"Person": len(qf), "Guild": len(bolling) if qf else 0,
                          "Dragon": len(set().union(*[own_pd[p] for p in qf]))}}
    hot_nb = defaultdict(set)
    for _, a, _, b, t in fire:
        if t is not None and float(t) > 600: hot_nb[a].add(b); hot_nb[b].add(a)
    owner_of = defaultdict(set)
    for p, ds in own_pd.items():
        for d in ds: owner_of[d].add(p)
    qg_p, qg_d = set(), set()
    for p1, ds in own_pd.items():
        for d1 in ds:
            for d2 in hot_nb[d1]:
                for p2 in owner_of[d2]:
                    qg_p |= {p1, p2}; qg_d |= {d1, d2}
    exp["QG"] = {"ents": {"Person": len(qg_p), "Dragon": len(qg_d)}}
    return exp


def run(fuse, cypher, cursor=None):
    params = {"cypher": cypher, "ontology": "Dragons", "pageSize": "100000"}
    if cursor: params["cursorType"] = cursor
    try:
        d = http("GET", f"{fuse}/query/cypher/run?" + urllib.parse.urlencode(params))
    except urllib.error.HTTPError as e:
        body = e.read().decode(errors="replace")
        try:
            body = json.loads(body)["data"]["errorDescription"]
        except (ValueError, KeyError, TypeError):
            pass
        return {"error": f"HTTP {e.code}: {str(body)[:300]}"}
    except Exception as e:  # noqa: BLE001
        return {"error": f"{type(e).__name__}: {e}"}
    ents, rels, errs = defaultdict(set), set(), []
    def walk(o):
        if isinstance(o, dict):
            if "eType" in o: ents[o["eType"]].add(o.get("id") or o.get("eID"))
            if "rType" in o: rels.add(o.get("rID") or o.get("id"))
            for k, v in o.items():
                if k in ("error", "errorDescription") and v: errs.append(str(v)[:200])
                walk(v)
        elif isinstance(o, list):
            for x in o: walk(x)
    walk(d)
    return {"ents": {k: len(v) for k, v in ents.items()}, "rels": len(rels), "errors": errs, "raw": d}


def verdict(got, exp):
    if "error" in got or got["errors"]: return "ERROR"
    ok = all(got["ents"].get(t, 0) == n for t, n in exp.get("ents", {}).items())
    if "rels" in exp: ok &= got["rels"] == exp["rels"]
    if ok and not any(exp.get("ents", {}).values()) and not exp.get("rels"):
        return "PASS (vacuous: expected nothing)"  # passes, but proves little - check the data
    return "PASS" if ok else "FAIL"


def playground(fuse, os_url, one_click):
    """The /dragons/swagger page and spec respond; with one_click, POST /dragons/tutorial/load reloads exactly the
    tutorial data (it resets the indices, so it runs after the query checks)."""
    print("\nDragons playground")
    failed = 0
    base = fuse.rsplit("/fuse", 1)[0]
    for path in ["/dragons/swagger", "/dragons/swagger/openapi.json", "/dragons/swagger/ui/swagger-ui-bundle.js"]:
        try:
            code = urllib.request.urlopen(base + path, timeout=60).status
        except urllib.error.HTTPError as e:
            code = e.code
        except Exception as e:  # noqa: BLE001
            code = type(e).__name__
        failed += code != 200
        print(f"  {'ok' if code == 200 else 'FAIL':5} GET {path}  ({code})")
    if not one_click:
        return failed
    try:
        r = http("POST", f"{fuse}/dragons/tutorial/load?reset=true")
        ok = bool(r.get("ok"))
    except Exception as e:  # noqa: BLE001
        r, ok = {"error": str(e)[:300]}, False
    http("POST", f"{os_url}/_refresh")
    want = {}
    for name, kind, _, index in load_tutorial.FILES:
        want[index] = want.get(index, 0) + load_tutorial.rows(os.path.join(load_tutorial.CSV_DIR, name)) * (2 if kind == "edge" else 1)
    wrong = {i: (load_tutorial.doc_count(os_url, i), n) for i, n in want.items() if load_tutorial.doc_count(os_url, i) != n}
    good = ok and not wrong
    failed += not good
    print(f"  {'PASS' if good else 'FAIL':5} POST /fuse/dragons/tutorial/load  "
          + ("stored counts match the tutorial CSVs" if good else f"ok={ok} wrong={wrong} {str(r.get('error', ''))[:200]}"))
    return failed


def main(argv=None):
    p = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    p.add_argument("--fuse", default="http://localhost:8888/fuse")
    p.add_argument("--opensearch", default="http://localhost:9200")
    p.add_argument("--load", action="store_true", help="load the tutorial data first (load_tutorial.py)")
    p.add_argument("--reset", action="store_true", help="with --load: drop the Dragons indices before loading")
    p.add_argument("--only", nargs="*", help="run only these query ids")
    a = p.parse_args(argv)

    failed = 0
    if a.load:
        failed += load_tutorial.load(a.fuse, a.opensearch, reset=a.reset) > 0
        print()

    exp = expectations(a.opensearch)
    print("Doc queries (expected = computed from stored data)")
    for doc, qid, cypher, known in QUERIES:
        if a.only and qid not in a.only: continue
        got, e = run(a.fuse, cypher), exp.get(qid, {})
        v = verdict(got, e)
        failed += not v.startswith("PASS")
        label = v if v.startswith("PASS") or not known else f"{v} (known: {', '.join(known)})"
        shown = got.get("error") or f"ents={got['ents']} rels={got['rels']}" + (f" errors={got['errors'][:1]}" if got["errors"] else "")
        want = ", ".join(f"{k}={x}" for k, x in e.items() if k != "note")
        print(f"  {qid:4} {label} - {doc}\n       {cypher}\n       got:      {shown}\n       expected: {want} {e.get('note', '')}")

    # Cursor types used in projection-and-count.md / api.md
    print("\nCursor types")
    count_q = "Match (p1:Person)-[k:Know]->(p2:Person) return *"
    c = run(a.fuse, count_q, "count")
    want_know = exp["Q2a"]["rels"]
    counts = {}
    def find_counts(o):
        if isinstance(o, dict):
            if "Know" in o and isinstance(o["Know"], int): counts.update(o)
            for x in o.values(): find_counts(x)
        elif isinstance(o, list):
            for x in o: find_counts(x)
    find_counts(c.get("raw"))
    ok = counts.get("Know") == want_know and counts.get("Person") == 2 * want_know
    failed += not ok
    print(f"  {'PASS' if ok else 'FAIL':5} count cursor  {count_q}\n        got: {c.get('error') or counts}  expected: Know={want_know}, Person={2 * want_know}")
    for ct in ["graph", "csv", "forwardPaths", "projection"]:
        r = run(a.fuse, "Match (d:Dragon) return *", ct)
        bad = "error" in r or r["errors"]
        failed += bool(bad)
        print(f"  {'ERROR' if bad else 'ok':5} cursorType={ct:12} {r.get('error') or (r['errors'][:1] if r['errors'] else 'responds')}")

    failed += playground(a.fuse, a.opensearch, one_click=a.load)

    print(f"\n{'ALL PASS' if not failed else f'{failed} check(s) not passing'}")
    return 1 if failed else 0


if __name__ == "__main__":
    sys.exit(main())
