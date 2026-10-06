# ADR 0002 — Closed target model: the allow-list is the type

- Status: accepted
- Date: 2026-10-06

## Context

The rejection most likely to sink Guard Rails is plugin-hub#12757 (Reactive Menu Swapper):

> this amount of flexibility in menu swapping will enable it to be abused in many places [...] it
> is essentially impossible to verify that it's compliant with jagex rules

A reviewer has to be able to see, in one file, everything a rule can touch. Filtering
"bad" options at runtime does not give them that; a closed type does.

## Decision

1. **`GuardedAction` is the allow-list.** 18 constants — item actions (Eat, Drink, Drop,
   Destroy, Wield, Wear, Use, High/Low Alchemy, Deposit, Withdraw), object actions (Enter,
   Climb, Pull, Teleport, Board, Jump-in) and Cast teleport. There is no constant for Attack,
   cast-on-NPC/player, Trade, Follow, Report, any player option, Pickpocket, Knock-Out, Lure or
   Walk here, so a rule cannot express them.
2. **Each action fixes the menu entry types it may match** (`CC_OP`/`CC_OP_LOW_PRIORITY` for
   item ops, `WIDGET_TARGET_ON_WIDGET` for alchemy on an own item, `GAME_OBJECT_*` for scenery,
   `CC_OP` for the spellbook). `GuardedAction.NEVER_GUARDED` lists every NPC, player,
   spell/item-on-actor and walk type; `GuardedActionTest` asserts no action overlaps it and that
   it covers every non-deprecated `NPC_*`/`PLAYER_*`/`*_ON_NPC`/`*_ON_PLAYER` constant, so a new
   RuneLite menu type in that family fails the build until classified. The engine (step 4) will
   check entry type membership before anything else.
3. **`Target` = one action on one curated group** (`ItemGroup`, `ObjectGroup`, `TeleportSpell`
   enums). Factories reject an action of the wrong kind. Group membership is defined in code
   from gameval `ItemID`/`ObjectID` constants in step 5 (agreed 2026-10-06; `ItemManager` has no
   exact name→ID lookup, see `docs/research/runelite-api.md`). Users only ever pick names.
4. **`Rule` is immutable and valid by construction**: the builder enforces name (1–40 chars,
   no `<`/`>`/control chars — names are echoed into chat, where `<…>` is markup), ≤ 8
   conditions, hold time 200–1500 ms, and Wilderness opt-in only for actions on carried items
   (not Withdraw, objects or spells).
5. **`Condition.holds(GameContext, Click)`** instead of the planned `holds(GameContext)`. Two
   conditions need facts about the clicked entry (item value for the alch guard, time since this
   target was last clicked for the double-click guard). `dependsOnClick()` marks them so the
   engine caches every other condition once per tick and evaluates only these per entry.
6. `GameContext` is an interface in the model; step 3 implements it as a per-tick snapshot.
   Effects that have no varbit (antipoison/antivenom use varp `POISON`) are the snapshot's
   problem, not the model's.

## Hub rules cited

- RuneLite wiki, *Rejected or Rolled Back Features* — "ID based plugins: Plugins that use player
  provided IDs for the entirety of their functionality [...] Plugins that use a specific set of
  IDs but do not allow user input will still be accepted."
- example-plugin `AGENTS.md` — "No removing or deprioritising attack/cast options in PvP";
  "No conditional menu entry removal based on NPC type, friend status, etc."
- Reviewer precedent plugin-hub#12757 (above).

## Consequences

- Adding a guardable action is a code change with a test and a reviewer, never user data.
- Some legitimate wishes (e.g. guarding an NPC dialogue option) are impossible by design.
- `Use` is matched as `CC_OP`/`CC_OP_LOW_PRIORITY`/`WIDGET_TARGET` pending confirmation in the
  dev client (step 4); the sets only ever narrow.
