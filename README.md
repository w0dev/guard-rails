# Guard Rails

A configurable misclick guard for Old School RuneScape on RuneLite. Write small rules for your own
clicks — **when** (conditions) → **guard** (an item, object or teleport action) → **mode** (how it is
enforced) — instead of installing a separate single-purpose guard plugin for each case.

> Status: pre-release scaffold. Nothing is enforced yet.

Guard Rails never removes or hides a menu entry: the guarded option is always reachable from the
right-click menu. It cannot target Attack, casting on NPCs or players, or any player option, and it
switches itself off in the Wilderness and on PvP worlds by default. See
[`docs/RULES-COMPLIANCE.md`](docs/RULES-COMPLIANCE.md) (coming in a later commit).

## Development

```bash
./gradlew build   # compile + unit tests (incl. the compliance tripwire)
./gradlew run     # launch a RuneLite dev client with the plugin loaded
```

Gradle 8.10 runs on JDK 11–23. If your default JDK is newer, point `JAVA_HOME` at a JDK 21
(the plugin still compiles with `--release 11`).

To log into the dev client with a Jagex account, follow
[Using Jagex Accounts](https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts).

## AI disclosure

This plugin is developed with substantial help from Claude Code (Anthropic). A human reviews every
commit, tests in-game, and is responsible for the code.

## License

BSD 2-Clause — see [LICENSE](LICENSE).
