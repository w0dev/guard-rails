# Rules compliance

Guard Rails touches the one grey zone in menu plugins — conditional menu changes — so this file
records exactly which rule applies, what we do, and which approved plugin set the precedent.
It is updated in every commit that adds an enforcement site.

Sources (read 2026-10-05): RuneLite wiki `0b82e79`, `runelite/plugin-hub` `f65ec8e`,
`runelite/example-plugin` `46e14f2`. Supporting research: [`research/`](research/).

## The rules, verbatim

RuneLite wiki, *Rejected or Rolled Back Features*, section "Menu Entry Swapping":

> * `Conditional menu entry removing`: This can be overpowered in some cases (hiding attack options on NPCs/players based on some conditions, like it being friend or it being specific type of NPC).
>   * Note, as of [Jagex's latest Third Party Client Guidelines](https://secure.runescape.com/m=news/third-party-client-guidelines?oldschool=1), we are now able to offer left-click and shift-click swaps for NPCs. See [our associated release post for info](https://runelite.net/blog/show/2022-06-17-1.8.24-Release)

Same page, section "Not currently being considered":

> * ID based plugins: Plugins that use player provided IDs for the entirety of their functionality can cause moderation issues and outcomes that break Jagex's plugin rules. Due to this fact we will not be accepting any new ID based plugins. Plugins that use a specific set of IDs but do not allow user input will still be accepted. (e.g. plugins like Vardorvis Projectiles which only allows you to change a specific projectile to a set list of projectiles.)

example-plugin `AGENTS.md`, Menu / PvP / Input restrictions:

> - No adding new menu entries that cause actions to be sent to the server
> - No conditional menu entry removal based on NPC type, friend status, etc. (can be overpowered)
> - No removing or deprioritising attack/cast options in PvP
> - No injecting input events, including mouse and keyboard events

## Reviewer precedent we must answer

- **plugin-hub#12757 "Reactive Menu Swapper" — rejected** (riktenx, 2026-06-27): a flexible
  user-configured menu swapper was "essentially too powerful", abusable at "nylos at tob, or
  blackjacking", and "essentially impossible to verify". Our answer: Guard Rails cannot make any
  option *more* convenient — it only ever makes an item/object/teleport option *harder* to
  left-click — and the set of guardable options is a closed, reviewable enum with no combat,
  NPC, player, Walk-here or PvP-relevant entries.
- **plugin-hub#13428 "vividflash-qol-aio" — closed** (riktenx, 2026-07-08): "we don't accept
  plugin repacks of this nature". Our answer: see *Engine, not repack* below.

## What Guard Rails does and never does

| | |
|---|---|
| Never | remove, hide, reorder-away or filter a menu entry (`setMenuEntries`/`removeMenuEntry` are banned by `ComplianceTest`) |
| Never | target Attack, cast-on-NPC/player, Trade, Follow, Report, any player option, Pickpocket, Knock-Out, Lure |
| Never | add a menu entry that sends anything to the server; never call `Client.menuAction`; never inject or replay input |
| Never | accept a free-form item/object/NPC ID from the user, in the UI or in share codes |
| Default off | in the Wilderness and on PvP worlds |
| Only | force right-click, deprioritise, modifier-gate, or add a client-side no-op top entry — the real option always remains in the right-click menu |

## Mechanism → precedent (preliminary; finalised with the engine in step 4)

| Mode | Mechanism | Approved precedent |
|---|---|---|
| Right-click only | `MenuShouldLeftClick.setForceRightClick(true)` | smithing-misclick-prevention (`SmithingMenuManager.java:179`); core Bank plugin (`BankPlugin.java:236`) |
| Deprioritise | `MenuEntry.setDeprioritized(true)` | core Ground Items, Menu Entry Swapper, Corp (deprioritises Attack on the Dark energy core); misclick-guard |
| Modifier | guard applies unless Shift/Ctrl held (read via `KeyManager`) | accidental-teleport-blocker, alch-blocker, stopmisclickingtiles (all bypass on a held key) |
| Hold-to-confirm | client-side `MenuAction.RUNELITE` no-op entry on top | no-misclick-repot's "Waiting..." entry (`NoMisclickRepotPlugin.java:406-419`). **The hold-to-arm timer has no direct precedent** — flagged for a `#plugin-hub` sanity check. |

## Engine, not repack

#13428 bundled copies of other plugins' features (with their code) behind toggles. Guard Rails
ships one original mechanism — a rule evaluator — and the existing guards' behaviours fall out as
data (recipes are serialised rules, not code paths). Concretely: one `Condition` interface, one
closed `Target` enum, four `Mode` strategies, and nothing per-recipe in Java. Removing every
recipe leaves a fully functional plugin; adding one requires no code.
