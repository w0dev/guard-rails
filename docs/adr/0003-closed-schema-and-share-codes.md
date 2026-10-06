# ADR 0003 — Closed JSON schema and share codes

- Status: accepted
- Date: 2026-10-06

## Context

Rules are stored in config and shared as copy-paste codes. Imported codes are untrusted input
that runs on the client thread, and the schema is where "no free-form IDs" either holds or
leaks.

## Decision

1. **Hand-written mapping over the Gson tree model** (`RuleJson`), not reflective binding. Every
   key that may appear is named in one file. `StrictObject.finish()` rejects any key that was not
   read, at every level, so `{"itemId":4151,...}` or `{"npc":"Goblin",...}` fails to import
   rather than being silently ignored (and possibly honoured by a later version).
2. Enum values must be the exact constant name; numbers must be whole and in range; wrong JSON
   types are rejected. Validation of values is the model's job (ADR 0002) — the codec only ever
   builds rules through the same constructors and builder the UI uses.
3. **Envelope** `{"schemaVersion":1,"rules":[…]}` with 1–50 rules, so a recipe or a set of rules
   shares as one code. Defaults are omitted on write for short codes; output is byte-stable.
4. **Share code** = `GR1.` + base64url (no padding) of zlib-compressed JSON. The prefix versions
   the encoding; `schemaVersion` versions the JSON. zlib's Adler-32 catches most corruption.
5. **Hostile-input caps before parsing**: ≤ 16 KiB code, ≤ 64 KiB inflated (checked while
   inflating — a 10 MiB zero bomb is rejected), nesting depth ≤ 8 (Gson's tree parser recurses;
   a `[[[[…` code must not overflow the client thread's stack). All failures surface as
   `InvalidRuleException` with a user-readable message.
6. `ShareCodec` takes the injected `Gson`.

## Hub rules cited

- example-plugin `AGENTS.md` — "Use `@Inject Gson` to get a Gson instead, never create your own
  from scratch." (tests construct `new Gson()`; plugin code never does.)
- *Forbidden language features* — "No use of Java (de)serialization" (`ComplianceTest` bans
  `ObjectInputStream`/`ObjectOutputStream`; we use JSON only).
- RuneLite wiki — "ID based plugins [...] will not be accepted" (closed schema, no ID fields).

## Consequences

- Every schema change touches `RuleJson` and needs a `schemaVersion` bump plus a migration for
  stored config (per `AGENTS.md`: never silently reset users' settings).
- Codes from newer versions fail on older clients with a clear message rather than partially
  importing.
