## Plugins

YANG-DB plugins are found with Java's `ServiceLoader`, through the `fuse-extension-api` module. A plugin can add Guice
modules, REST routes and new query languages without changing core configuration.

- How to write one: [CONTRIBUTING.md, *Writing an extension*](../../../CONTRIBUTING.md#writing-an-extension)
- Where plugins fit in the engine: [the modules](../../tutorial/architecture/modules.md#fuse-extension-api-the-plugin-contract)
  and [where a new language joins the query pipeline](../../tutorial/architecture/query-lifecycle.md#other-languages)
- A working example: `extensions/graphql` (the GraphQL language)
