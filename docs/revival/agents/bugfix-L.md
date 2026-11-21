# bugfix-L
## Goal
Fix P1 (time-field inSet treated as intersection in EBaseStatisticsProvider) and T2 (QueryTest q11/q2, EpbJoinSelectionTests enum compare).
## Tasks
- [x] P1 failing test (eRelDateInSetFilterTwoIndicesKeepsBothIndicesTest: expected [idx-..-09, idx-..-10] but was [])
- [x] P1 fix (shared helper; inSet = retainAll(union of buckets); notInSet prunes nothing)
- [x] T2 QueryTest (Q011.json + q2Obj); Q011.json fixed: Quant2->Quant1, rType 6->subject, eType 4->Guild
- [x] T2 EpbJoinSelectionTests enum ==
## Status
Done: committed cb3d964cd (P1), 034de854b (T2). fuse-model 27/0 fail, fuse-dv-epb 433/0 fail (4 skipped). Next: coordinator merge.
## Discoveries
- Q011.json used removed type Quant2 and numeric ontology ids; only QueryTest references it.
## Log
- reset to origin/phase3-structural-assertions (d709e0887)
- fail-before confirmed for P1 test; eq test passes before and after
- full run green; committed
