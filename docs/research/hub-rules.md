# Guard Rails: Plugin Hub rules research

Researched 2026-10-05. Sources were read locally from clones in this directory, plus the `gh` API.

| Source | Revision read |
|---|---|
| `runelite/runelite.wiki` (whole wiki HEAD) | `0b82e7930d1b14bcfb827175ef61d26edd416b30`, 2026-10-05 21:47 -0500 |
| `runelite/plugin-hub` master | `f65ec8e6f3274d995719137047e4c0408e4bf2de`, 2026-10-06 03:14 UTC |
| `runelite/plugin-hub-tooling` (the packager CI runs) | `35fc06011f7089a15833092f413e8d0eca46eaaf`, 2026-10-01 |
| `runelite/runelite` master | `ddd0669ef4d7494fd420cb507e3c1fa62bab8023`, 2026-10-04 |

---

## 1. Wiki: "Rejected or Rolled Back Features"

File: `Rejected-or-Rolled-Back-Features.md`. Last changed in wiki commit `0b82e79` (2026-10-05, Lake, "Update forbidden language features").

### Menu Entry Swapping section, verbatim

The heading is `#### Menu Entry Swapping`. The full section reads:

```
#### Menu Entry Swapping
* `Conditional menu entry removing`: This can be overpowered in some cases (hiding attack options on NPCs/players based on some conditions, like it being friend or it being specific type of NPC).
  * Note, as of [Jagex's latest Third Party Client Guidelines](https://secure.runescape.com/m=news/third-party-client-guidelines?oldschool=1), we are now able to offer left-click and shift-click swaps for NPCs. See [our associated release post for info](https://runelite.net/blog/show/2022-06-17-1.8.24-Release)
```

This entry has been in the page since at least `33eefbf` (2018-07-03, Tomas Slusny).

### Player-supplied IDs, verbatim

This bullet sits under `#### Not currently being considered`. It was added in `aad710a` (2024-02-24, Felanbird, "Rejected-Features: add ID based plugins").

```
* ID based plugins: Plugins that use player provided IDs for the entirety of their functionality can cause moderation issues and outcomes that break Jagex's plugin rules. Due to this fact we will not be accepting any new ID based plugins. Plugins that use a specific set of IDs but do not allow user input will still be accepted. (e.g. plugins like Vardorvis Projectiles which only allows you to change a specific projectile to a set list of projectiles.)
```

### Other entries that touch clicks or input, verbatim

Under `#### Removed in the past`:

```
* Barbarian Assault Plugin: Left-click calling removed (see [#859](https://github.com/runelite/runelite/pull/859))
* Plugins that remove the inventory pane background or make it click-through. This was abused to trivialize click-intensive skilling methods.
* Plugins that manipulate the client's window, including, but not limited to, minimizing or changing which window is currently focused.
```

Under `#### Not currently being considered`:

```
* Things that can be abused for "AFK" agility training (see [#6979](https://github.com/runelite/runelite/issues/6979))
* Touchscreen/Controller plugins: Any plugin-hub implementations of this would likely trigger Jagex's macro detection, and thus won't be accepted.  
* Plugins that simulate content for any purpose. (e.g. Jagex previously requested Quest Helper's Leviathan simulation be removed.)
```

The page has no entry about "deprioritise", "force right-click" or "misclick" by those words.

### Forbidden language features and sensitive APIs

Section `#### Forbidden language features` (summary, with the parts that matter to us quoted):
- Plugins must be written in Java. Kotlin, Scala and other JVM languages are not allowed.
- Forbidden: reflection, JNI/JNA, running subprocesses, and downloading code at runtime.
- Forbidden functions: `Desktop.open`/`LinkBrowser.open`, `Desktop.browse` (`LinkBrowser.browse` is OK), `Thread.interrupt`/`Thread.sleep`, `java.io` file APIs (use `net.runelite.client.util.Filepath`), `java.lang.Runtime`, `java.awt.KeyboardFocusManager`, and any network client other than OkHttp.
- Forbidden patterns: creating your own `Gson` (inject it), creating your own `OkHttp` (inject it), and using `net.runelite` as your package.
- `Client.menuAction` is forbidden except for one listed case. The verbatim rule is under `#### Sensitive APIs`:
  ```
  * `Client.menuAction`
      * There is currently only 1 acceptable use case for this function - fetching collection log data: `client.menuAction(-1, InterfaceID.Collection.SEARCH_TOGGLE, MenuAction.CC_OP, 1, -1, "Search", null)`
  ```
- Verbatim: "**As a rule of thumb, if we cannot review every single line of source code that your plugin will execute, we will not accept it.**"
- Verbatim: "Also note that some older plugins are grandfathered into these rules. Just because an existing plugin does something in a certain way does not guarantee it is allowed."
- `#### Restricted APIs` (these need manual review): `Client.hopToWorld`, `Filepath.Unchecked`, `ScriptEventBuilder.setCanSendPackets`.

**What this means for Guard Rails.**
- When the hold-to-confirm entry is clicked, it must do nothing, or only consume the click. It must never call `client.menuAction` to replay the real action.
- Rules must not take raw IDs as input. See §6 for the reviewer precedent.

---

## 2. Wiki: "Plugin Hub Review"

The page is `Plugin-Hub-Review.md`, titled "Plugin Hub". It was added in `d88fc7c` on 2026-06-19 and is only 32 lines long. `Information-about-the-Plugin-Hub.md` (last changed `199eabe`, 2026-06-19) links to it.

### What it requires

The page is about process. It sets no technical requirements: nothing on menus, IDs, icons, size, naming, duplicates or AI disclosure. The relevant text, verbatim:

> Plugin Hub plugins are reviewed both during initial submission, and also any subsequent update, by human reviewers affiliated with RuneLite, as well as AI agents.

> RuneLite's review process focuses on two aspects: security and game rule compliance.

> We verify that plugins aren't malicious, such as stealing account credentials or installing malware. This is achieved primarily by having rules on the code we accept, such as restricting reflection and native code, limiting dependencies, and by utilizing automatic code scanning.

> We review plugin submissions for rule breaking behavior. This is done best-effort as the rules communicated to us by Jagex can often be subjective or can change due to emerging gameplay or new content.

The page lists these non-goals (paraphrased). Reviewers do not check:
- that a plugin works or is useful;
- its performance;
- whether it breaks other plugins;
- whether the information it shows is accurate.

They may "proactively disable" a plugin that destabilises the client.

`### Reviewers` section, verbatim:

> Beginning in April 2026, RuneLite operates an automated review bot that can automatically approve plugin updates. Most simple plugins can be approved by the bot automatically.

> The automated review bot was made as a result of recent technological advancements in code generation with AI. Since late 2025/early 2026, the majority of new plugin code is no longer written by humans or submitted by humans. [...]

> All other review is done by humans; either directly reviewing the submitted code, or by configuring the review bot with the specific permissions and restrictions that the plugin is allowed to have.

### Topics with no wiki requirement

**AI disclosure.** No page on the wiki requires it; I grepped the whole wiki. The review page only says AI is part of the review process. PR #13428 included a "Generated with Claude Code" line, and reviewers did not comment on it. **UNVERIFIED** whether any unwritten AI policy exists, for example in Discord.

**Duplicates and repacks.** No wiki text covers these. The policy lives in the plugin-hub README (§4, "Contribute to existing plugins") and in reviewer comments (§5, §6).

**Data-sharing warning.** From `Information-about-the-Plugin-Hub.md`, verbatim:

> Plugins which communicate with third party servers are required to have a warning either on the plugin, or on the configuration option enabling the setting, explaining what data is being sent.

---

## 3. Wiki: "Using Jagex Accounts" (for the README)

File: `Using-Jagex-Accounts.md`, last changed `ab869aa` (2024-11-04). Steps, paraphrased closely:

1. Make sure the RuneLite *launcher* is version 2.6.3 or newer.
2. Open the launcher's configure mode:
   - Windows: run `RuneLite (configure)` from the Start menu.
   - Other platforms: pass `--configure` to the launcher. On a Mac: `/Applications/RuneLite.app/Contents/MacOS/RuneLite --configure`.
3. In the **Client arguments** box, add `--insecure-write-credentials`, then click **Save**.
4. Launch RuneLite once through the **Jagex Launcher**. It writes your credentials to `.runelite/credentials.properties`. That file lets anyone log in to your account without a password. **Never share it.**
5. Run the dev client (for example the Gradle `run` task in your IDE). It uses the saved credentials.
6. Cleanup: delete `credentials.properties` when you are done. To invalidate the credentials, use "End sessions" in your runescape.com account settings.

---

## 4. `runelite/plugin-hub` README and packager limits

### README requirements

Verbatim where it gives exact values.

- **Repository**: "**Make sure that your repository is set to public**". The packager also requires that "repositories must be hosted on GitHub.com" and that the repository URL "must be a clone url ~ it should end with .git".
- **Example `runelite-plugin.properties`**:
  ```
  displayName=Helmet check
  author=dekvall
  description=Alerts you when you have nothing equipped in your head slot
  tags=hint,gear,head
  plugins=com.helmetcheck.HelmetCheckPlugin
  version=
  build=standard
  ```
  "`version` is optional, if missing the commit will be used."
- **Icon**: "Optionally, you can add an icon to be displayed alongside with your plugin. Place a file with the name `icon.png` no larger than 48x72 px at the root of the repository."
- **License**: BSD 2-Clause "Simplified", committed as `LICENSE`.
- **Plugin manifest file**: one new file at `plugins/<name>`:
  ```
  repository=
  commit=
  ```
  "copy the full 40-character hash".
- **Keep one PR**: "we prefer all changes be kept in a single pull request to avoid spamming notifications with further newly-opened PRs."
- **Contribute instead of duplicating**: "We recommend contributing to existing plugins if the author(s) are accepting contributions, and the feature you want to add fits well into the plugin, to avoid fragmentation of plugin ecosystem."
- **Reviewing**: "We will review your plugin to ensure it isn't malicious, doesn't break Jagex's rules, or isn't one of our previously Rejected/Rolledback features. __If it is difficult for us to ensure the plugin isn't against the rules we will not merge it__."
- **Resources**: load them with `getResourceAsStream`, not `getResource`, because plugins run from a jar.
- **Third-party dependencies**: these need Gradle dependency verification and a manual check by a maintainer. That "adds significantly to the amount of time", so avoid them.
- **Build type**: "`build` property, which may be set to either `standard` or `gradle`. In `standard` mode, your `build.gradle` and `settings.gradle` get replaced when built during plugin submission. This allows for expedited review".
- **Java**: the README recommends Java 11, and CI builds with `java-version: 11`.

### Packager limits

From `plugin-hub-tooling/package/src/main/java/net/runelite/pluginhub/packager/Plugin.java`. Messages are quoted verbatim.

| Check | Limit or rule |
|---|---|
| Output jar | Default `jarSizeLimitMiB = 10`. Error: "the output jar is {}MiB, which is above our limit of {}MiB". A warning appears at 80% of the limit. |
| Source archive | `MAX_SRC_SIZE_MIB = 10`, or the jar limit if that is larger. Error: "the source archive is {}MiB, which is above our limit of {}MiB". |
| `icon.png` file size | Error: "icon.png is {}KiB, which is above our limit of 256KiB". |
| `icon.png` resolution | Rejected if width × height > 50×100. Error: "icon.png is too high-resolution. It should be 48x72 px". |
| Internal name (the plugin file name) | Must match `^[a-z0-9-]+$`. |
| `commit` | Must match `^[a-fA-F0-9]{40}$`. |
| Allowed keys in the plugin file | `repository`, `commit`, `jarSizeLimitMiB`, `warning`, `authors`. Error for anything else: "unexpected key in commit descriptor". |
| Required properties | `displayName` (must not be "Example"), `author`, `description` (must not be the template text), `plugins`, `build` ("build must be one of [gradle, standard]"). |
| Unknown properties | Error: "unknown {} key(s) in runelite-plugin.properties". |
| Gradle files | "All gradle files must wrap at 120 characters or less". |
| `LICENSE` | Required: "Missing LICENSE file". |
| Build time | "build did not complete within 5 minutes". |
| Jar manifest | "plugins cannot specify Class-Path". |
| Deprecated APIs | Terminally deprecated APIs are rejected: "plugin uses terminally deprecated APIs". |
| Standard build | Only `src/main/java`, `src/main/resources` and `lombok.config` are used. |

PRs labelled `waiting for author` are closed automatically after 7 days (`.github/workflows/stale.yml`).

---

## 5. PR runelite/plugin-hub#13428

- **Title**: "Add vividflash-qol-aio"
- **Author**: `vividflash`
- **Dates**: opened 2026-07-07T22:37Z; **closed without merging** 2026-07-08T01:28Z by `riktenx`.
- **Repository**: https://github.com/vividflash/vividflash-qol-aio, commit `04446abd5b38314afc593f6179413eda7a6de3e3`.
- **Labels**: `plugin added`. `waiting for author` was added twice (by coopermor and ldavid432) and removed each time by the bot.

**Does it match "a QoL repack bundling guards"? Yes.** The PR body, verbatim:

> Adds **VividFlash QoL AIO** — a single plugin bundling a set of small quality-of-life and misclick-guard tweaks (No Use Player, Easy Unnote, withdraw limits, Require Tool, Pickpocket/Foundry/Motherlode safety guards, Take-before-Attack menu swap, potion misclick guards, clue held-count reminder, category sound muting, and more), so users can enable just the ones they want from one place instead of running many tiny plugins. Every feature is off-by-default or config-gated.

The body ended with "🤖 Generated with [Claude Code](https://claude.com/claude-code)".

The PR has no review comments and no reviews (both API endpoints returned empty). Every non-bot issue comment, verbatim:

1. **coopermor** (2026-07-07):
   > If you are using code from other plugins you need to retain their license information and not attribute all the files to yourself.
2. **vividflash**:
   > Thanks, will fix tomorrow and repush. Should have been in there in the list, mb. Probably same issue with other two.
3. **riktenx** (member; closed the PR with this comment, 2026-07-08):
   > thanks for the submission but we don't accept plugin repacks of this nature. if a user wants all of these plugins they can just install the original versions of them directly
4. **vividflash** (after the close):
   > Fair enough. Turning it private and creating new stripped down public version containing only new features, that existing plugins missed as a complementary plugin to the existing options with additional new features. Question: How is Konar Milestone Reminder different from slayer-boosting? [...]
5. **raiyni** (member):
   > They are two different code bases

### Why it was rejected

It was a **repack**: one plugin re-bundling the features of existing hub plugins, such as No Use Player and Easy Unnote. Users can install the originals instead. A secondary issue was that it copied code from other plugins without keeping their licence and attribution.

Nothing in the thread objects to the menu or guard behaviour itself. Reviewers raised no Jagex-rules concern.

**Implications for Guard Rails:**
- Do not position it as a bundle of existing guard plugins, and do not copy their code.
- Present it as one coherent rules engine, and say in the PR how it differs from Custom Menu Swaps, Misclick Guard and the others.
- raiyni's reply ("two different code bases") suggests overlapping features are tolerated when the code is original. That is my inference from one comment, not stated policy.

---

## 6. Name collisions and reviewer precedent on misclick and menu plugins

### Is "Guard Rails" taken?

- No file in `plugins/` (2,868 entries) is named `guard-rails`, `guardrails` or similar.
- `gh search code "displayName=Guard Rails"` and `"Guard Rails" filename:runelite-plugin.properties` returned nothing.
- PR searches for "guard-rails", "guard rails" and "guardrails" only hit unrelated PRs that use the word in prose (Lending Tracker #14088 and #14300; Choicer #10322, #10329, #10426).
- **The name `guard-rails` appears to be free.** Display names inside the repositories of all ~2,868 plugins were not exhaustively checked (**UNVERIFIED**).

Possibly confusable names already in `plugins/`: `misclick-guard`, `skiller-guard`, `quest-food-guard`, `gauntlet-guard`, `herb-harvest-guard`, `firemaking-log-guard`, `mixology-order-guard`, `pickpocket-guardian`, `click-confirm`, `deprioritize-leave`.

### Reviewer comments on menu removal and swapping

**#12757 "Add Reactive Menu Swapper plugin"**, closed and rejected. https://github.com/runelite/plugin-hub/pull/12757
**riktenx** (2026-06-27), verbatim. This is the key precedent for any configurable rules engine:

> thanks for the submission but this plugin is essentially too powerful
>
> this amount of flexibility in menu swapping will enable it to be abused in many places, including but not limited to nylos at tob, or blackjacking. in its current form (and any version of this i can conceive of really) it is essentially impossible to verify that it's compliant with jagex rules and thus we cannot accept it
>
> per https://github.com/runelite/runelite/wiki/Rejected-or-Rolled-Back-Features
>
> > Conditional menu entry removing: [...]

**#10543 "Add PVP Entity Hider plugin"**, closed. https://github.com/runelite/plugin-hub/pull/10543
**raiyni** (2026-02-09), quoting Jagex's guidelines verbatim:

> How does this not go against several PVP related rules in the 3pc? [...] eg. `Removes or deprioritises attack, cast (or similar) options from the minimenu in PVP` or `Makes it easier to target 3D entities with a spell by removing some options`

**#5799 "Add 'No Player Use' plugin"**, merged 2024-04-12. https://github.com/runelite/plugin-hub/pull/5799
**cdfisher**:

> This seems like it would possibly go against the third-party client guidelines, specifically
> > Reorders or removes player-based options, such as 'Trade with'

**geheur** replied:

> Pretty sure "Custom Menu Swaps" can already do this @cdfisher

The PR was merged anyway.

**#7215 "Create click-move-options"** (removed Walk Here and Cancel), closed. https://github.com/runelite/plugin-hub/pull/7215
- **SaltisRS**: "Isn't most of this functionality already available via Menu Entry Swapper and their various addon plugins?"
- **LlemonDuck**: "closing as the above comments have not been resolved yet. feel free to resubmit when they are"

### Accepted guard plugins where reviewers endorsed removing entries

Guard plugins that remove non-combat item entries have been accepted. In several cases reviewers told the author to use the menu-removal API:

- **#8437 pet-spell-blocker**, merged. https://github.com/runelite/plugin-hub/pull/8437
  - **LlemonDuck**: "Also you can use `client.removeMenuEntry`"
  - **geheur**: "it's `client.getMenu().removemenuentry()`."
- **#8522 quest-food-guard**, merged.
  - **LlemonDuck**: "use `client.getMenu().removeMenuEntry` instead of reconstructing your own menu list"
- **#13262 No-Misclicks-In-Bank** (removes wield/wear/drink/eat in the bank), merged. https://github.com/runelite/plugin-hub/pull/13262
  - **riktenx** raised only naming and build issues: "surely you want to exercise some forethought with the name here and maybe call it something more generic? "bank option hider" considering you have also separately submitted "no drink in bank"". Also "doesn't build" and "delete all .class files from your repo".
- **No reviewer comments**: #15844 misclick-guard (merged 2026-09-01), #10102 deprioritize-leave, #11459 no-misclick-repot, #5239 click-confirm (Felanbird's only comment was about opening duplicate PRs), #15287 skiller-guard.
- **#13231 Maggot King Helper**: a contributor describes deprioritising the instance exit door on left-click. No reviewer objected. The PR was closed in favour of the existing plugin, not on rules grounds.

### Reviewer comments on user-supplied IDs

This is directly relevant to letting users define rules.

- **#16556 Vale Totems Declutter**, merged 2026-09-15. https://github.com/runelite/plugin-hub/pull/16556
  **coopermor**:
  > We don't allow new plugins that take user supplied IDs. Hardcode the vale totems region in your plugin and only allow the config input to be object names and not IDs.
- **#13385 Better Object Hider**, closed. https://github.com/runelite/plugin-hub/pull/13385
  **riktenx**, on right-click-to-hide stored by ID or tile:
  > that's just user-input ID with extra steps. we've had a plugin exactly like that before and we had to disable it for moderation issues.
  >
  > essentially the only ID-based hiding you can do is hardcoded. for example there is a plugin that just hides stalactites in caves, etc.
- **#13415 Better Object Hider (resubmitted)**, merged 2026-07-07. https://github.com/runelite/plugin-hub/pull/13415
  - The author reworked it to key on **object name + scope/location**, with **no IDs** entered, stored or targeted, and **objects without a name cannot be targeted**. The author described this as following "guidance in `#development`".
  - The only further reviewer request (**riktenx**): "you will need to prevent the central pillar in the bloat room from being hidden as this is something we've historically disallowed"
- **Other ID-based plugins closed by quoting the wiki rule**:
  - #16967 HD Tile Markers (riktenx, 2026-09-25: "note that better npc highlight is grandfathered here")
  - #12754 object id marker (Felanbird)
  - #13203 ground object id highlighter (Bram91)
  - #10348 object-highlight-list (coopermor)
  - #14923 ground-marker-variables (ldavid432: "The user configurable varbit based functionality is not allowed.")

### Searches with no relevant hits

I found no reviewer comment mentioning `forceRightClick`, "Waiting..." entries, or "hold to confirm". The searches were:
- PR searches for `forceRightClick`, "hold to confirm", "Waiting..." and "click confirm": nothing relevant.
- "Waiting..." matched only unrelated PR bodies.
- Issues were not searched separately (**UNVERIFIED** for issues).

---

## 7. Hardcore or roguelike world type

The `runelite-api/src/main/java/net/runelite/api/WorldType.java` enum constants on master (blob `b4ca3b3d`) are, verbatim:

```
MEMBERS(1),
PVP(1 << 2),
BOUNTY(1 << 5),
PVP_ARENA(1 << 6),
SKILL_TOTAL(1 << 7),
QUEST_SPEEDRUNNING(1 << 8),
HIGH_RISK(1 << 10),
LAST_MAN_STANDING(1 << 14),
BETA_WORLD(1 << 16),
LEGACY_ONLY(1 << 22),
EOC_ONLY(1 << 23),
NOSAVE_MODE(1 << 25),
TOURNAMENT_WORLD(1 << 26),
FRESH_START_WORLD(1 << 27),
DEADMAN(1 << 29),
SEASONAL(1 << 30);
```

- **No hardcore or roguelike flag exists.**
- The most recent change to the file is `d1285170d2` (2024-05-20, "api: add legacy only/eoc only world type"). Earlier changes: `0b277e2ee0` (2023-05-31, beta worlds), `77102eface` (2022-10-04, FSW), `0c037cbac3` (2022-09-14, quest speedrunning).
- `gh search` of runelite/runelite PRs and issues for "hardcore", "hardcore world" and "roguelike" found only unrelated ironman-icon and hiscore items.
- The API's only hardcore references are `IconID.HARDCORE_IRONMAN` and `IconID.HARDCORE_GROUP_IRONMAN`, which are account types.
- **UNVERIFIED**: whether Jagex has announced such a world type, or whether a varbit or other signal exists outside `WorldType`.

---

## 8. Plugin files in `plugins/` (master `f65ec8e`)

Each plugin file was read verbatim. displayName and description come from `runelite-plugin.properties` at the pinned commit.

| Plugin file | repository | commit | displayName / description |
|---|---|---|---|
| `no-misclick-repot` | https://github.com/bopsec/bop-plugins.git | `1df78055099e9f3b4e7f3df024131db78590c355` | "No Misclick Repot". Moves the "Drink" option if the potions' effects are still active. |
| `smithing-misclick-prevention` | https://github.com/asundr/runelite-smithing-misclick-prevention.git | `8b4415392f1235815efa41b00321e68561e6cebf` | "Smithing MisClick Prevention". Prevents accidentally clicking on unintended items in the smithing menu. |
| `misclick-guard` | https://github.com/willzero123/misclick-guard.git | `3f499475890120fcd204f883fc972bc7275e6f9b` | "Misclick Guard". Protects selected interface actions from accidental clicks. The repo contains `AGENTS.md` and `CLAUDE.md`. |
| `quest-food-guard` | https://github.com/Steviefp/quest-food-guard.git | `30365b2d92884a546da6646df4a8bfd90828ddfb` | "Quest Food Guard". Prevents accidental eating of quest-related food items. |
| `stopmisclickingtiles` | https://github.com/WhatATopic/StopMisclickingTiles.git | `3b9080974f7142e80aedb490cac2d6a74015cfc2` | "StopMisclickingTiles". Shift + Right-click a tile to disable the "walk here" action on it. |
| `accidental-teleport-blocker` | https://github.com/bielie993-ui/accidental-teleport-blocker.git | `1299d86f16eb7b9bbea69b70c8a120e3cc8d8333` | "Accidental Teleport Blocker". Blocks teleports in the spellbook unless you CTRL + Click (Configurable). |
| `skiller-guard` | https://github.com/JamsRepos/skiller-guard.git | `b417976edf5ecbc4b49d0ba298989bc32e795fca` | "Jam's Skiller Guard". Protects skillers from accidental combat and Prayer XP: hides risky clicks, locks lamps, and warns if XP still slips through. |
| `alch-blocker` | https://github.com/robrichardson13/alch-blocker.git | `4f88ca8b3321def6c16baf7fb6b50553602f183f` | "Alch Blocker". Allows you to block items from being alched. Plugin file also has `authors=robrichardson13,zdunbrack`. |
| `no-misclicks-in-bank` | https://github.com/Welkon1/No-Misclicks-In-Bank.git | `6f76aa102a04525bdce2424b57b4ad899ba82e8b` | "NoMisclicksInBank". Removes the wield, wear, drink, and eat options from items whilst in your bank. |
| **`hotkeyable-menu-swaps`** (this is Custom Menu Swaps) | https://github.com/geheur/More-menu-entry-swaps.git | `720769e96dfcc1d976784ece846a4ab0403ef96d` | "Custom Menu Swaps", author geheur. Custom text-based swaps and assign different hotkeys for certain different menu entry swaps. Main class `com.hotkeyablemenuswaps.HotkeyableMenuSwapsPlugin`, `build=standard`. |

All ten exist. The only one not found under the expected name is **Custom Menu Swaps**: its plugin file is `hotkeyable-menu-swaps`, not `custom-menu-swaps`.

Related plugins in `plugins/` that we didn't look into: `click-confirm`, `deprioritize-leave`, `teleport-blocker`, `no-misclick-veng`, `stop-misclicking-orbs`, `shift-drop-block`, `pet-spell-blocker`, `confirm-spellbook`, `prevent-tp-at-mortimer`, `menu-swapper-extended`, `improved-context-menu`, `firemaking-log-guard`, `herb-harvest-guard`, `hcim-safety-check`.

---

## Bottom line for Guard Rails

1. **Avoid the #12757 rejection.** riktenx rejected a flexible, user-configurable menu swapper as "essentially too powerful" and "impossible to verify". That is the biggest risk for a general rules engine. Mitigations:
   - Restrict which menu actions and targets rules can apply to.
   - Hard-exclude NPC and player combat options, spell-on-actor options, Walk Here, and anything that matters in PvP.
   - Make the restrictions obvious in the code so a reviewer can verify them.
2. **No user-typed IDs.** Key rules on item and object *names* and option text, as #16556 and #13415 did, and skip targets without a name. Hard-coded ID lists are fine.
3. **Not a repack.** Write original code, give it a coherent purpose, and say in the PR how it differs from the existing guard plugins (#13428).
4. **Hold-to-confirm must not use `Client.menuAction`.** That call is forbidden apart from the one collection-log case.
5. **Packaging:**
   - Plugin file `plugins/guard-rails`, using `build=standard`.
   - `icon.png` at most 48x72 px and 256 KiB.
   - Jar and source archive each at most 10 MiB.
   - Include a `LICENSE` (BSD-2).
   - Wrap Gradle files at 120 characters.
   - Write the plugin in Java 11 only.
