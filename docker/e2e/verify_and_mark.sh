#!/bin/bash
# Run the end-to-end doc-query suite against the running Docker stack and publish the result as a GitHub commit
# status (context "e2e/docker") on the current commit, so everyone can see which commit was verified on Docker.
#
#   docker/e2e/verify_and_mark.sh            # load tutorial data fresh, run, mark HEAD
#   docker/e2e/verify_and_mark.sh --no-mark  # run only
#
# Prerequisites:
#   - the stack runs an image built from HEAD (docker/buildDocker.sh, then docker compose up -d --force-recreate fuse)
#   - HEAD is pushed (a status can only be attached to a commit GitHub knows)
#   - the GitHub CLI `gh` is logged in with write access to the repo
set -uo pipefail
cd "$(dirname "$0")"

sha=$(git rev-parse HEAD)
repo=$(gh repo view --json nameWithOwner -q .nameWithOwner 2>/dev/null || echo YANG-DB/yang-db)
log=$(mktemp -t yangdb-e2e).log

if [ -n "$(git status --porcelain --untracked-files=no)" ]; then
  echo "warning: uncommitted changes; the image may not match HEAD ($sha)" >&2
fi

curl -s -XDELETE localhost:9200/projection >/dev/null   # left behind by the projection-cursor check
python3 doc_queries.py --load --reset | tee "$log"
rc=${PIPESTATUS[0]}

failing=$(grep -cE "^  [A-Za-z0-9]+ +(FAIL|ERROR)|^  (FAIL|ERROR) " "$log")
passing=$(grep -cE "^  [A-Za-z0-9]+ +PASS|^  PASS |^  ok    cursorType" "$log")
os_version=$(curl -s localhost:9200 | python3 -c 'import json,sys; print(json.load(sys.stdin)["version"]["number"])' 2>/dev/null || echo "?")

if [ "$rc" -eq 0 ]; then
  state=success; desc="Docker e2e (OpenSearch $os_version): all $passing checks pass"
else
  state=failure; desc="Docker e2e (OpenSearch $os_version): $failing failing, $passing passing"
fi
echo "$state: $desc  (log: $log)"

if [ "${1:-}" != "--no-mark" ]; then
  gh api -X POST "repos/$repo/statuses/$sha" -f state="$state" -f context="e2e/docker" -f description="${desc:0:140}" \
    -f target_url="https://github.com/$repo/blob/main/docs/revival/STATUS.md" --jq '"marked \(.state) on '"${sha:0:9}"'"'
fi
exit "$rc"
