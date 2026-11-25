#!/usr/bin/env python3
"""Write N copies of the Dragons tutorial data as one CSV set, for benchmarking on more than the tutorial's ~22k rows.

Copy 0 is the tutorial data unchanged; copy k (k >= 1) prefixes every id, and every relationship end, with `s<k>_`.
The copies are disjoint graphs, so every query that isn't limited or filtered on a specific id returns exactly N times
the tutorial's result, and each extra copy adds the same amount of work.

    python3 docker/bench/scale_data.py 10 /tmp/dragons-x10     # 10 copies, ~220k rows
"""
import argparse, csv, os, sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(HERE, "..", "e2e"))
import load_tutorial  # noqa: E402  (the tutorial CSV folder and file list)

ID_COLUMNS = ("id", "entityA.id", "entityB.id")


def scale(n, out_dir, csv_dir=load_tutorial.CSV_DIR):
    """Writes the n-copy CSV set into out_dir (created if needed). Returns {file: rows written}."""
    os.makedirs(out_dir, exist_ok=True)
    written = {}
    for name, _kind, _label, _index in load_tutorial.FILES:
        with open(os.path.join(csv_dir, name), newline="") as f:
            reader = csv.reader(f)
            header = next(reader)
            data = list(reader)
        ids = [i for i, col in enumerate(header) if col in ID_COLUMNS]
        with open(os.path.join(out_dir, name), "w", newline="") as f:
            w = csv.writer(f, quoting=csv.QUOTE_ALL, lineterminator="\n")  # quoted like the tutorial files
            w.writerow(header)
            for k in range(n):
                for row in data:
                    if k:
                        row = list(row)
                        for i in ids:
                            row[i] = f"s{k}_{row[i]}"
                    w.writerow(row)
        written[name] = len(data) * n
    return written


def main(argv=None):
    p = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    p.add_argument("copies", type=int)
    p.add_argument("out_dir")
    a = p.parse_args(argv)
    if a.copies < 1:
        p.error("copies must be at least 1")
    written = scale(a.copies, a.out_dir)
    print(f"{sum(written.values())} rows in {len(written)} files -> {a.out_dir}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
