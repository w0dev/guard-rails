# ADR 0001 — Scaffold from runelite/example-plugin, verbatim build

- Status: accepted
- Date: 2026-10-05

## Context

Plugin Hub reviewers diff submissions against the official template. Anything unusual in the build
draws scrutiny that a misclick plugin — already close to a known grey zone — does not need.

Template read at `runelite/example-plugin@46e14f2` (2026-10-01, "sync AGENTS.md").

## Decision

1. Copy `build.gradle`, the Gradle 8.10 wrapper, `.gitignore` and `logback-test.xml` from the
   template, changing only names (`com.guardrails`, `GuardRailsPlugin`, project `guard-rails`).
2. One deliberate addition to `build.gradle`: the `test` task declares `src/main`,
   `runelite-plugin.properties` and `icon.png` as inputs, because `ComplianceTest` scans them and
   would otherwise be skipped as up-to-date after a source-only change (verified: without this a
   banned token in a comment did not fail the build).
3. No dependencies beyond the template's (`client`, Lombok, JUnit 4, `jshell` for the dev client).
4. `ComplianceTest` is a tripwire over our own source for banned APIs (reflection, class loading,
   `Unsafe`, JNI/JNA, processes, Java serialization, `Thread.sleep`, `WidgetInfo`/`WidgetID`,
   any `java.net`/OkHttp use, input-event construction, `Client.menuAction`, and menu-entry
   removal calls `setMenuEntries`/`removeMenuEntry`), plus packaging checks (properties, icon size, no
   `META-INF/services` file).
5. Plugin has no config yet; config arrives with the first feature that reads it.
6. CI: GitHub Actions on Temurin 11 running `./gradlew build`.

## Hub rules cited

From example-plugin `AGENTS.md` (verbatim):

- "`build.gradle` must target Java 11** and match the structure of the example-plugin template."
- "Rename everything from the template. Do not leave `com.example`, `ExamplePlugin`, `ExampleConfig`, or `example` as the config group."
- "Do not include a `META-INF/services/net.runelite.client.plugins.Plugin` file."
- "Do not add transitive dependencies from `runelite-client` directly to `build.gradle`, such as gson, guice, or okhttp."
- "Retain a permissive license, such as BSD-2."
- "Optimize icon PNGs." / "Ensure PNGs are actually PNGs"
- "Remove unused config classes, fields, and imports."

From the `runelite/plugin-hub` README and packager (`plugin-hub@f65ec8e`,
`plugin-hub-tooling@35fc060`): `icon.png` at most 48×72 px and 256 KiB; jar and source archive
each at most 10 MiB; `build=standard` (or `gradle`); hub file name `^[a-z0-9-]+$` (we will use
`plugins/guard-rails`); a LICENSE file is required; Gradle files wrap at 120 characters.

`icon.png` is 48×72 RGBA (~3 KB). `ComplianceTest` checks both icon limits.

## Consequences

- Local builds need a JDK that Gradle 8.10 can run on (11–23). This machine's default is JDK 26,
  so builds use `JAVA_HOME` pointed at a JDK 21; bytecode is still `--release 11`.
- `ComplianceTest` is string matching: it can false-positive (e.g. a comment mentioning
  `WidgetInfo`), which is acceptable — rephrase the comment. It cannot prove absence of a
  violation; human review and `docs/RULES-COMPLIANCE.md` remain the real controls.
