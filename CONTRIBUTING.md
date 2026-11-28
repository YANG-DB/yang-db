# Contributing

When contributing to this repository, please first discuss the change you wish to make via issue,
email, or any other method with the owners of this repository before making a change. 

Please note we have a code of conduct, please follow it in all your interactions with the project.

## Pull Request Process

1. Branch from `main` and open your pull request against `main` (the only actively developed branch;
   `master` is a frozen 2019 snapshot).
2. Build and test with JDK 11 or 21 and Maven 3.6+: `mvn clean install` must pass. It runs the unit and the integration
   tests (an embedded OpenSearch on ports 9200/9300; pass `-Dfuse.test.http.port=19200 -Dfuse.test.transport.port=19300`
   if those are taken). An ignored test must say why: `@Ignore("reason")`, which CI checks.
3. If your change affects how YANG-DB is run (config keys, ports, docker setup), update the project
   README ([PROJECT.md](PROJECT.md)) and [docker/README.md](docker/README.md) accordingly.
   The root `README.MD` is the maintainer's GitHub profile page, not project documentation.
4. A maintainer will review and merge.

## Before you change engine code
Read the [architecture tutorial](docs/tutorial/architecture/README.md). It describes every module and follows a query
through the whole pipeline, with the classes to read for each stage.

## Writing an extension

You can add query languages, REST routes and Guice bindings to YANG-DB from your own jar, without editing core and
without touching the application config.

### The SPI

Depend on `com.yangdb:fuse-extension-api` (plus `fuse-service` if your routes reuse its controllers). It holds:

| Type | Package | Role |
|---|---|---|
| `YangDbExtension` | `com.yangdb.fuse.extension` | the entry point: returns your Guice modules, config-aware Jooby modules and route registrars (all optional) |
| `QueryLanguages` | `com.yangdb.fuse.extension` | the language registry: a Guice `MapBinder` from a language name to a transformer |
| `QueryTransformer<QueryInfo<String>, AsgQuery>` | `com.yangdb.fuse.dispatcher.query` | what a language implements: query text (with its ontology) to an ASG query |
| `AppRegistrar` | `com.yangdb.fuse.services.appRegistrars` | what a route registrar implements: `register(Jooby app, AppUrlSupplier urls)` |

### Discovery: `META-INF/services`

The service finds extensions with `java.util.ServiceLoader`. List your implementation's fully qualified class name, one
per line, in this file of your jar:

```
src/main/resources/META-INF/services/com.yangdb.fuse.extension.YangDbExtension
```

Put the jar on the service's classpath (in the distribution, its `lib/` folder). At startup the service installs every
extension's modules, registers its routes after the core ones and logs `Loaded YANG-DB extension <name>`. The class needs
a public no-argument constructor.

### Adding a query language

Register a transformer under the language's name from one of your modules:

```java
QueryLanguages.register(binder, "echo").to(EchoTransformer.class);
```

The transformer is created by Guice, so it can inject anything the service binds (e.g. `OntologyProvider`). The
language is then available through the generic routes:

- `POST /fuse/query/echo`: create a query (a `CreateJsonQueryRequest` body; its `type` is set to `echo`)
- `GET|POST /fuse/query/echo/run?ontology=<ont>&query=<text>`: run it and get the first page (the query text can also be
  the request body)

The name is matched exactly first, then ignoring case. An unknown name gets a 404 that lists the available languages.
The built-in languages are registered the same way under `cypher` and `sparql` (`CreateQueryRequestMetadata.TYPE_*`;
`sparql` also answers to its old name, `sparkQL`);
their original URLs (`/fuse/query/cypher/run` etc.) still work. `graphQL` is an extension (`extensions/graphql`) and
registers the same way. Registering a name twice fails at startup.

### Adding routes

Return `AppRegistrar`s from `registrars()`. Each is called once, after the core routes and before the generic language
routes, so a fixed path like `/fuse/query/myfeature/run` wins over `/fuse/query/:lang/run`. To call a core controller,
extend `AppControllerRegistrarBase<SomeController>` (in `fuse-service`) and use `getController(app)`.

### Modules

`modules()` returns plain Guice modules; `joobyModules()` returns modules that need the config
(`configure(env, config, binder)`). Don't return a `ModuleBase` subclass from either: a `ModuleBase` only configures
itself when its class is listed under `modules.<profile>` in the application config.

### A minimal extension

```java
package com.example.hello;

import com.google.inject.Module;
import com.yangdb.fuse.dispatcher.query.QueryTransformer;
import com.yangdb.fuse.extension.QueryLanguages;
import com.yangdb.fuse.extension.YangDbExtension;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.query.QueryInfo;
import com.yangdb.fuse.services.appRegistrars.AppRegistrar;

import java.util.List;

public class HelloExtension implements YangDbExtension {
    @Override
    public List<Module> modules() {
        return List.of(binder -> QueryLanguages.register(binder, "echo").to(EchoTransformer.class));
    }

    @Override
    public List<AppRegistrar> registrars() {
        return List.of((app, urls) -> app.get("/hello", () -> "hello from an extension"));
    }

    /** "translates" a query into an empty ASG query named after the query text */
    public static class EchoTransformer implements QueryTransformer<QueryInfo<String>, AsgQuery> {
        @Override
        public AsgQuery transform(QueryInfo<String> query) {
            return AsgQuery.Builder.start(query.getQuery(), query.getOntology()).build();
        }
    }
}
```

with `META-INF/services/com.yangdb.fuse.extension.YangDbExtension` containing `com.example.hello.HelloExtension`.
This is the hello-world extension of `fuse-service`'s tests (`HelloWorldExtension`, checked by `YangDbExtensionsTest`).
For real ones, see the Dragons example (`examples/dragons/ext`, class `com.yangdb.dragons.DragonsExtension`: routes
only) and GraphQL (`extensions/graphql`, class `com.yangdb.fuse.graphql.GraphQLExtension`: a query language, a
cursor, routes and controllers). Extensions under `extensions/` are part of the default build and ship in the
distribution's `lib/`.

## Code of Conduct

### Our Pledge

In the interest of fostering an open and welcoming environment, we as
contributors and maintainers pledge to making participation in our project and
our community a harassment-free experience for everyone, regardless of age, body
size, disability, ethnicity, gender identity and expression, level of experience,
nationality, personal appearance, race, religion, or sexual identity and
orientation.

### Our Standards

Examples of behavior that contributes to creating a positive environment
include:

* Using welcoming and inclusive language
* Being respectful of differing viewpoints and experiences
* Gracefully accepting constructive criticism
* Focusing on what is best for the community
* Showing empathy towards other community members

Examples of unacceptable behavior by participants include:

* The use of sexualized language or imagery and unwelcome sexual attention or
advances
* Trolling, insulting/derogatory comments, and personal or political attacks
* Public or private harassment
* Publishing others' private information, such as a physical or electronic
  address, without explicit permission
* Other conduct which could reasonably be considered inappropriate in a
  professional setting

### Our Responsibilities

Project maintainers are responsible for clarifying the standards of acceptable
behavior and are expected to take appropriate and fair corrective action in
response to any instances of unacceptable behavior.

Project maintainers have the right and responsibility to remove, edit, or
reject comments, commits, code, wiki edits, issues, and other contributions
that are not aligned to this Code of Conduct, or to ban temporarily or
permanently any contributor for other behaviors that they deem inappropriate,
threatening, offensive, or harmful.

### Scope

This Code of Conduct applies both within project spaces and in public spaces
when an individual is representing the project or its community. Examples of
representing a project or community include using an official project e-mail
address, posting via an official social media account, or acting as an appointed
representative at an online or offline event. Representation of a project may be
further defined and clarified by project maintainers.

### Enforcement

Instances of abusive, harassing, or otherwise unacceptable behavior may be
reported by contacting the project team at yang.db.dev@gmail.com. All
complaints will be reviewed and investigated and will result in a response that
is deemed necessary and appropriate to the circumstances. The project team is
obligated to maintain confidentiality with regard to the reporter of an incident.
Further details of specific enforcement policies may be posted separately.

Project maintainers who do not follow or enforce the Code of Conduct in good
faith may face temporary or permanent repercussions as determined by other
members of the project's leadership.

### Attribution

This Code of Conduct is adapted from the [Contributor Covenant][homepage], version 1.4,
available at [http://contributor-covenant.org/version/1/4][version]

[homepage]: http://contributor-covenant.org
[version]: http://contributor-covenant.org/version/1/4/

## Continuous integration
Every pull request to `main` runs two checks ([.github/workflows/ci.yml](.github/workflows/ci.yml)):
- **Build & test**: `mvn clean test`, unit and integration tests against an embedded OpenSearch node.
- **Docker e2e**: builds the distribution and the Docker image, starts it next to OpenSearch, loads the Dragons
  tutorial data, and checks every documented query, the regression queries and the GraphQL examples
  ([docker/e2e](docker/e2e/README.md)).

Both must be green before merging. To reproduce the e2e check locally, see [docker/e2e/README.md](docker/e2e/README.md).
CodeQL security scanning ([.github/workflows/codeql.yml](.github/workflows/codeql.yml)) runs after every merge to
`main` and weekly, not on pull requests.
