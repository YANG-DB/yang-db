#!/usr/bin/env bash
# Runs the benchmark (bench.py) against several YANG-DB releases and OpenSearch versions, one after another, each on a
# fresh cluster with the same data, and writes one results file per run into the output folder.
#
#   docker/bench/compare_versions.sh <out-dir> <scale> "<label> <opensearch version> <yang.db image tag>" ...
#   docker/bench/compare_versions.sh /tmp/bench 10 "v0.8.0 1.3.0 v0.8.0" "v0.9.1 2.19.7 v0.9.1" "main 2.19.7 Dragons"
#
# The images must exist locally (docker/buildDocker.sh <tag>, from a checkout of that tag). Every run uses its own
# compose project with its own volumes, removed afterwards (a 1.3 cluster can't read 2.19 indices). The container names
# are fixed in docker-compose-os.yml, so stop the regular quickstart stack first. Keep the machine awake for the whole
# run (`caffeinate -dims` on macOS): if it sleeps, the Docker VM's clock stalls and then jumps, which stalls Docker's
# health checks and makes engines before 0.10 refuse requests for a while (A11). Runs the queries of this checkout's
# playground against every version: queries an older release gets wrong or can't run show up as such in its results.
set -euo pipefail
here=$(cd "$(dirname "$0")" && pwd)
compose="$here/../docker-compose-os.yml"
out=$1; scale=$2; shift 2
mkdir -p "$out"
for run in "$@"; do
  read -r label os tag <<<"$run"
  project="yangdb-bench-${label//[^a-zA-Z0-9]/}"
  echo "=== $label: OpenSearch $os, yangdb/yang.db:$tag, scale $scale"
  export OS_VERSION=$os YANGDB_TAG=$tag
  docker compose -p "$project" -f "$compose" up -d opensearch fuse
  for i in $(seq 1 90); do
    curl -sf -o /dev/null http://localhost:8888/fuse/health && break
    if curl -s -m 5 http://localhost:8888/fuse/health 2>/dev/null | grep -q "Clock moved backwards"; then
      echo "  (engine refusing requests after a clock step, A11: waiting)"; sleep 30
    fi
    [ "$i" = 90 ] && { echo "$label did not become healthy"; curl -s -m 5 http://localhost:8888/fuse/health 2>/dev/null | head -c 300 || true; echo; docker logs dragons-graph 2>&1 | grep -v "|-" | tail -20 || true; docker compose -p "$project" -f "$compose" down -v; exit 1; }
    sleep 5
  done
  python3 "$here/bench.py" --load --scale "$scale" --label "$label (OpenSearch $os)" \
    --out "$out/$label-x$scale.json" | tee "$out/$label-x$scale.log" || echo "$label: bench reported problems"
  docker compose -p "$project" -f "$compose" down -v
done
