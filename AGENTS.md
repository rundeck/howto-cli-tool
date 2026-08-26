# AGENTS.md

This file provides guidance to AI coding agents (including Claude Code) when working with code in
this repository.

## What this is

`howto` (CLI binary name `how`) discovers "actions" a user can run in a source repo (build, test,
install, etc.) and executes them. It finds actions from a `howto.md`/`readme.md` file, or by
detecting build tools (Gradle, NPM) present in the repo, then runs the chosen action as a shell
command. This repo's own `HOWTO.md` is a working example of the markdown format it parses.

## Common commands

```bash
./gradlew build              # compile + test all subprojects
./gradlew check              # run tests only (JUnit5/Spock)
./gradlew :app:installDist   # build and install locally into app/build/install/how/bin
./app/build/install/how/bin/how "$@"   # run the locally-installed CLI
./gradlew currentVersion     # print current version (axion-release plugin, derived from git tags)
./gradlew release            # tag and release
```

Run a single Spock spec with `--tests`, e.g.:

```bash
./gradlew :gradle-detector:test --tests "org.rundeck.howto.detectors.gradle.GradleDetectorSpec"
```

Once built, `how`/`howto` itself is usable in this repo (it's a Gradle+markdown project), so
`how to` / `how do build` also work here as a live smoke test of the detectors.

## Module structure

Multi-module Gradle build (`settings.gradle`); each module's built jar is named `how-<module>`:

- **utilities** — core engine: `Howto` (orchestrates detection/invocation), `HowtoApp` (interface
  passed to actions), `Detector`/`DiscoveredAction` (SPI contracts), `CommandAction`/`BaseAction`
  (default shell-invocable action), `MarkdownDetector` (parses howto/readme markdown), and the
  built-in `HelpDetector`.
- **gradle-detector**, **npm-detector** — pluggable `Detector` implementations, one module per
  build tool. Each is a separate module so new detectors can be added the same way without
  touching `utilities`.
- **app** — the picocli-based CLI entry point (`org.rundeck.howto.app.App`), packaged with the
  Shadow plugin as the `how` executable/distribution.

### Detector plugin mechanism

Detectors are wired together via `java.util.ServiceLoader`, not direct dependencies from
`utilities` on the detector modules. Each detector module registers itself in
`src/main/resources/META-INF/services/org.rundeck.howto.detectors.Detector`. `Howto.getDetectors()`
loads all registered `Detector`s at runtime. `app/build.gradle` is what actually pulls
`npm-detector` and `gradle-detector` onto the runtime classpath so their services are discovered —
adding a new detector module means: implement `Detector`, add the SPI resource file, and add it as
a dependency of `app`.

### Action resolution flow (`Howto` in utilities)

1. `HelpDetector` actions are always included.
2. `MarkdownDetector` runs first (unless excluded via `-x`); if it finds any actions, tool
   detectors are skipped *unless* `-a`/`--all` is set.
3. Otherwise, every registered `Detector` (from ServiceLoader) contributes actions.
4. `Howto.invoke(name, args)` matches an action name by: exact match, then unique prefix match,
   then unique hyphen-abbreviation match (e.g. `s-a` matches `some-action`). Ambiguous or missing
   matches return `-1`, which `App` reports as "Action not found" and falls back to `help`.

### Markdown parsing rules (`MarkdownDetector`)

Implemented with `commonmark-java` via a `Strategy` list tried in order, first non-empty wins:

1. `howto.md`/`howto.markdown`/`howto` file — every H2 with a code block is an action.
2. `readme.md` — only H2 sections nested under an H1 whose title matches `how[ -]?to` are actions.
3. `readme.md` — any H2 whose own title matches `how[ -]?to ...` (capturing the rest as the action
   title) and that contains a code block.

Whichever strategy wins, `.howto.md`/`.howto.markdown`/`.howto` (a hidden file a user can gitignore)
is then looked up separately and merged on top: its actions are added to the result, and any action
with the same name overrides the one already found. This lets a user add or override actions
locally without editing `howto.md`/`readme.md`. It parses the same way as `howto.md` (every H2 with
a code block is an action).

The H2 title becomes the action name (lowercased, spaces to hyphens). Actions are executed via
`bash -c` with the fenced/indented/inline code block content, with a `DIR` env var set to the
detected base dir, and `"${@}"`-style trailing args appended after `--`.

Note: the parser's state machine only starts capturing H2 actions after it has seen an H1 heading
(any H1, when there's no title pattern to match) — a file with H2 sections but no H1 at all
produces no actions.

## Compatibility note

CI builds against JDK 11 (`.github/workflows/gradle.yml`), so avoid language features newer than
Java 11 in `utilities`, `app`, `gradle-detector`, and `npm-detector`.