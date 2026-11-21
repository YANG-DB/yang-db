# Phase 4 — agent M: extension API (Stage A)

## Goal
A documented extension point (`fuse-extension-api`, ServiceLoader discovery) that outside contributors can target
without editing core. Fix A3 (Dragons ext routes never registered) through it.

## Tasks
- [x] fuse-extension-api module (YangDbExtension, QueryLanguages MapBinder helper; reuse AppRegistrar / AppUrlSupplier / QueryTransformer)
- [x] BasicJsonQueryTransformerFactory -> MapBinder; Asg{Cypher,Sparql,GraphQL}Module register into it
- [x] FuseApp: ServiceLoader modules + registrars, no double registration
- [x] generic POST /fuse/query/{lang}, GET|POST /fuse/query/{lang}/run; old URLs kept
- [x] A3: Dragons via SPI; playground moved to com.yangdb.dragons.playground
- [x] hello-world test extension (echo language + route)
- [x] CONTRIBUTING.md "Writing an extension"

## Status
Done. Next: stage B (GraphQL extraction), A4 (Dragons clause routes).

## Discoveries
- `QueryTransformer` (fuse-core), `AppUrlSupplier` (fuse-core), `AppRegistrar` (fuse-service) are dependency-free
  interfaces; `AsgQuery`, `QueryInfo` live in fuse-model.
- `ModuleBase.configure` only runs when the class is listed under `modules.<profile>` in config, so an extension module
  must NOT extend ModuleBase (or must be listed in config).
- `CreateQueryRequestMetadata.TYPE_SPARQL` is "sparkQL" (not "sparql").
- Jooby 1.x runs matching routes in registration order; generic `/fuse/query/:lang` routes must be registered last.

- The old factory injected the three transformer classes directly (JIT), regardless of config; now a language
  exists only if its Asg*Module is configured. Every config that binds M2AsgModule lists all three: same behaviour.
- Two AppleDouble files are tracked upstream (docs/revival/._KNOWN_ISSUES.md, agents/._bugfix-K.md); the `._*`
  cleanup deletes them. Not staged in my commits.
- Jooby 1.x: a fixed route registered earlier does win over a later `/:lang` pattern (tested).

## Log
- 115c5c1f2 API module; 165e83d57 MapBinder; b5e053465 generic run + routes; 56f892c8d FuseApp SPI + hello test (9/9)
- f1bd99785 A3 via SPI (+A4 recorded); d7602f4ba CONTRIBUTING
- Tests: model 27, ext-api 0, core 37, asg 312, dv-unipop 152, dv-core 77, dv-gta 61, dv-asg 38, dv-epb 433, service 19 (all 0 failures); dragons-ext 7, datagen 18
