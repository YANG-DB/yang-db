#!/usr/bin/env python3
"""Saves every playground query's full response (all rows, every field) from a running stack, with only the fields
that differ between runs taken out (request id, timings, query/cursor ids and urls, timestamps), so that two engine
builds can be compared byte for byte: a change that is meant to be invisible must leave every file identical.

    python3 docker/bench/snapshot.py OUT_DIR          # then, after the change: snapshot.py OTHER_DIR; diff -r
"""
import json, os, re, sys, urllib.parse

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(HERE, "..", "e2e"))
sys.path.insert(0, HERE)
import bench  # noqa: E402
from load_tutorial import http  # noqa: E402

VOLATILE = {"requestId", "elapsed", "renderElapsed", "totalElapsed", "resourceId", "resourceUrl", "timestamp",
            "queryId", "cursorId", "cursorStoreUrl", "explainPlanUrl", "v1QueryUrl", "asgUrl", "pageStoreUrl",
            "resourceUrls", "dataUrl", "elasticQueryUrl"}


def scrub(o):
    if isinstance(o, dict):
        return {k: scrub(v) for k, v in o.items() if k not in VOLATILE}
    if isinstance(o, list):
        return [scrub(v) for v in o]
    if isinstance(o, str):
        return re.sub(r"[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}", "<uuid>", o)
    return o


def main(out, fuse="http://localhost:8888/fuse"):
    os.makedirs(out, exist_ok=True)
    for qid, language, query in bench.queries():
        d = http("GET", f"{fuse}/query/{language}/run?" + urllib.parse.urlencode(
            {language: query, "ontology": "Dragons", "pageSize": "100000"}))
        rid = (d.get("data") or {}).get("resourceId")
        if rid:
            http("DELETE", f"{fuse}/query/{rid}")
        with open(os.path.join(out, qid.replace(":", "_") + ".json"), "w") as f:
            json.dump(scrub(d), f, indent=1, sort_keys=True)
    print(f"{out}: {len(os.listdir(out))} responses")


if __name__ == "__main__":
    main(sys.argv[1])
