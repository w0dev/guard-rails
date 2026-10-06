# Plugin Hub precedents for Guard Rails

All repos were cloned and checked out at the exact `commit=` in `runelite/plugin-hub/plugins/<name>`, so each one is the approved version. Line numbers are repo-relative at that commit.

Naming note: geheur's `More-menu-entry-swaps` is listed in the hub as **`plugins/hotkeyable-menu-swaps`**, not under a "custom menu swaps" name. All other names exist as given.

## Comparison table

| Plugin | repo@commit | Events (enforcement) | Mechanism | Adds entries? | Consumes clicks? | Removes entries? | Target identification | PvP guard |
|---|---|---|---|---|---|---|---|---|
| no-misclick-repot | bopsec/bop-plugins@1df7805 | PostMenuSort, VarbitChanged, GameTick | Inserts a no-op **"Waiting..."** RUNELITE entry on top with `setForceLeftClick(true)`. It does **not** call `setDeprioritized`. | Yes: a RUNELITE no-op placeholder | No | No | Hardcoded potion item IDs plus option "Drink"/"Eat" | None |
| smithing-misclick-prevention | asundr/runelite-smithing-misclick-prevention@8b44153 | MenuEntryAdded, MenuShouldLeftClick, WidgetLoaded/Closed, MenuOptionClicked (notify only) | `MenuShouldLeftClick.setForceRightClick(true)` while the smithing interface (312) is open | No | No | No | Option "Smith"/"Smith set" plus a target-name regex (material + item type) and config-supplied names | None |
| misclick-guard | willzero123/misclick-guard@3f49947 | MenuEntryAdded, PostMenuSort(prio -1), MenuOpened, MenuOptionClicked, plus a MouseListener | Three modes. **Left click off**: consumes the click unless it was chosen from an open menu, and can optionally `setDeprioritized(true)` and hoist an empty-text CANCEL to the top. **Disabled**: removes the entry and consumes the click. | Only a blank CANCEL entry (fallback) | Yes | Yes (Disabled mode) | Widget group (combat/orbs/bank) plus option/target text, limited to widget MenuAction types | N/A (UI buttons) |
| quest-food-guard ("Quest Eat") | Steviefp/quest-food-guard@30365b2 | MenuEntryAdded | `menu.removeMenuEntry` for "Eat" on quest food | No | No | Yes (always, including from the right-click menu) | Hardcoded ItemIDs | None |
| stopmisclickingtiles | WhatATopic/StopMisclickingTiles@3b90809 | MenuOptionClicked, MenuEntryAdded (config UI) | Consumes "Walk here" on disabled or blocked tiles **unless the hotkey is held or toggled** | Yes: a shift-only RUNELITE "Disable/Enable Walk here" config entry | Yes | No | Option "Walk here" plus tile WorldPoint saved per region in config, and collision flags | None |
| accidental-teleport-blocker | bielie993-ui/accidental-teleport-blocker@1299d86 | MenuOptionClicked, MenuEntryAdded (config UI), StatChanged | Consumes CC_OP teleport casts unless **CTRL/SHIFT is held**. Can also block only for N seconds after a trigger spell. Can let right-click casts through (`client.isMenuOpen()`). Posts a chat message. | Yes: a shift-only RUNELITE "Enable/Disable block" config entry | Yes | No | Spell name parsed from the target text, user list per spellbook (varbit 4070) | None |
| skiller-guard | JamsRepos/skiller-guard@b417976 | PostMenuSort (skipped while the menu is open), MenuOpened, MenuOptionClicked, ClientTick, and more | Fail-closed: filters out entries on PostMenuSort and MenuOpened, **and** consumes the click as a backstop. Player, Walk, Cancel and Examine entries are never touched. | Yes: RUNELITE "[SG] ..." warning lines (right-click only, chat-only onClick) | Yes | Yes | Large hardcoded ID catalog (SafetyIds/SafetyCatalog) | Explicitly skips **player** entries. Wilderness/PvP/LMS only changes warnings. |
| alch-blocker | robrichardson13/alch-blocker@4f88ca8 | MenuOptionClicked, ScriptPostFired, PostMenuSort/MenuOpened (list UI) | Hides or dims inventory item widgets while alch is selected, and consumes the alch-on-item click. **Shift override** skips the consume. | Yes: a RUNELITE "Blacklist/Whitelist Alchemy" config entry (inserted above Cancel) | Yes | No | Item ID/name lists with wildcards, plus helper rules | None |
| no-misclicks-in-bank | Welkon1/No-Misclicks-In-Bank@6f76aa1 | MenuEntryAdded | Removes the Wield/Wear/Drink/Eat entry while the bank is open | No | No | Yes | Option prefix plus "bank widget open" | None |
| hotkeyable-menu-swaps (geheur More-menu-entry-swaps) | geheur/More-menu-entry-swaps@720769e | PostMenuSort(prio -1), MenuOpened(prio -1) | Swaps and hides entries by rewriting the array. Swaps are refused if the top entry or the swap candidate `isProtected`. | Only to clone a submenu entry it swaps to the top (copies the real entry's onClick) | No | Yes (custom hides, but never protected entries) | User-configured option/target text matching | Protects **player options** generally. No wilderness/world check. |

## Per-plugin notes

### no-misclick-repot (the "Waiting..." entry)
`src/main/java/com/bop/NoMisclickRepotPlugin.java`
- L41: `private static final String NO_OP_OPTION = "Waiting...";`
- L166-180 `onPostMenuSort`: takes `menuEntries[length-1]` (the top entry). If `shouldDeprioritize(topEntry)`, it calls `client.getMenu().setMenuEntries(insertNoOpEntry(menuEntries));`
- L281-291 `shouldDeprioritize`: true only when the option is exactly "Drink" or "Eat" and the item ID's buff is still active.
- L406-419, quoted exactly:
  ```java
  private MenuEntry[] insertNoOpEntry(MenuEntry[] menuEntries)
  {
      MenuEntry[] newEntries = Arrays.copyOf(menuEntries, menuEntries.length + 1);
      System.arraycopy(menuEntries, 0, newEntries, 0, menuEntries.length);

      newEntries[newEntries.length - 1] = client.getMenu().createMenuEntry(newEntries.length - 1)
          .setOption(NO_OP_OPTION)
          .setTarget("")
          .setType(MenuAction.RUNELITE)
          .setForceLeftClick(true)
          .onClick(entry -> { });

      return newEntries;
  }
  ```
- Verdict: this is a **pure client-side placeholder**. `MenuAction.RUNELITE` entries are handled inside RuneLite and are not sent to the server as game actions. The onClick is an empty lambda and nothing is consumed. The real Drink/Eat entry stays in the menu just below the placeholder, so the user can still right-click and pick it (the README says "Can still right click sip"). `setForceLeftClick(true)` makes sure a left-click runs the placeholder and never opens the menu.
- Not used anywhere in the plugin: `setDeprioritized`, `event.consume()`, and entry removal. The descriptor text says "Deprioritize Drink options", but the code does not use the deprioritize API.
- No `client.isMenuOpen()` guard. Whether PostMenuSort fires while the menu is open, and could therefore stack duplicate "Waiting..." entries, is **UNVERIFIED**. The alch-blocker comments at L342-347 say PostMenuSort keeps firing on an open menu and that MES and OverlayRenderer guard against this. skiller-guard L62 guards it too. Guard Rails should guard it.
- Conditions are detected from varbits/varps (e.g. `VarbitID.DIVINERANGE_POTION_TIME`, `VarPlayerID.POISON`) and boosted skill thresholds, with a per-GameTick countdown (L251-265). There is no PvP guard.

### smithing-misclick-prevention
`src/main/java/org/asundr/SmithingMenuManager.java`
- L153-164: MenuEntryAdded sets a `forceRightClick` flag when the anvil interface is open (`SMITHING_MENU_ID = 312`, L106) and option/target match.
- L166-183: `onMenuShouldLeftClick` re-checks `client.getMenuEntries()` and calls `evt.setForceRightClick(true);` (L179).
- L204-217: MenuOptionClicked only sends a notification and overlay ("Misclick on: %s!"). It does not consume.
- L317-327: targets are identified by option `Smith`/`Smith set` plus a regex on the sanitized target name and config custom names.

### misclick-guard
`src/main/java/com/misclickguard/MisclickGuardPlugin.java`, `ClickMode.java`, `GuardedAction.java`
- `ClickMode` L33-35: `NORMAL`, `LEFT_CLICK_OFF(blocksLeftClick)`, `DISABLED(blocksLeftClick, removesMenuEntry)`.
- L74-85: a MouseListener records `leftPressStartedInMenu.set(client.isMenuOpen())` on left press. This is how it tells a left-click apart from a selection in the open menu.
- L107-123: MenuEntryAdded calls `entry.setDeprioritized(true)` when the config enables it and the mode is left-click-off.
- L140-147: PostMenuSort (priority -1) runs `removeDisabledEntries()` (L297-325, `menu.setMenuEntries(retainedEntries)`) and `makeCancelDefaultForDeprioritizedEntry()` (L227-272). The second moves the CANCEL entry to the top with option `""`, or creates `menu.createMenuEntry(-1).setOption("").setTarget("").setType(MenuAction.CANCEL)`. Left-click then does nothing. MenuOpened (L134-138, L274-295) restores the "Cancel" text and position.
- L149-166: `event.consume()` if the mode is Disabled, or the mode blocks left-click and the click was not selected from an open menu.
- Targets are identified by widget group (combat, orbs, bank) plus option/target text, and only for widget MenuAction types (GuardedAction L73-85, L104-121).

### quest-food-guard
`src/main/java/com/questfoodguard/QuestFoodGuardPlugin.java` L45-55: on every MenuEntryAdded it loops through entries and calls `client.getMenu().removeMenuEntry(entry)` for "Eat" where `CONFIRM_ITEMS.contains(entry.getItemId())` (hardcoded ItemIDs, L27-39). The entry is removed from left-click **and** right-click, with no bypass. The plugin display name is "Quest Eat".

### stopmisclickingtiles
`src/main/java/com/stopmisclickingtiles/StopMisclickingTilesPlugin.java`
- L89-108: `HotkeyListener` with HOLD or TOGGLE mode (`KeybindMode`) sets `hotkeyHeld`.
- L201-247: MenuOptionClicked returns early if `hotkeyHeld` (L204). Otherwise, for "Walk here", it calls `event.consume()` when the tile is in the per-region disabled list (L221-224) or blocked by collision flags (L236-245).
- L249-280: a shift-only RUNELITE config entry, "Disable/Enable Walk here", that writes to ConfigManager.

### accidental-teleport-blocker
`src/main/java/com.AccidentalTeleportBlocker/AccidentalTeleportBlockerPlugin.java`
- L189-304 MenuOptionClicked:
  - L223: only CC_OP or CC_OP_LOW_PRIORITY.
  - L233: `if (config.allowRightClickWithoutModifier() && client.isMenuOpen()) return;`
  - L275-289: checks `client.isKeyPressed(KeyCode.KC_SHIFT / KC_CONTROL)`. If the key is not held, calls `event.consume()` and posts "ATB: Hold CTRL to use this teleport" via `client.addChatMessage`.
- L239-246: an optional time window after "trigger" spells. A cast is detected as a magic StatChanged after clicking "Cast".
- L139-187: shift-only RUNELITE config entries.

### skiller-guard
`src/main/java/com/skillerguard/MenuSafetyService.java`
- L60-67: PostMenuSort, `if (!activation.isActive() || client.isMenuOpen()) return; filterMenu();`
- L69-81: MenuOpened, `event.setMenuEntries(filtered)`.
- L83-101: MenuOptionClicked skips player entries and otherwise calls `event.consume()` when a hide rule matches. This is a backstop.
- L118-140 `filter`: keeps `isProtected` entries.
- L175-201 `isProtected`/`isPlayerEntry`: covers player entries (PLAYER_FIRST..EIGHTH, WIDGET_TARGET_ON_PLAYER, ITEM_USE_ON_PLAYER, `entry.getPlayer()!=null`) plus Walk, Cancel and Examine.
- L163-171: right-click-only RUNELITE "[SG] ..." warning entries.
- `DangerSettingsService.java` L214-220 has wilderness, PvP and LMS logic, but it only controls warnings.

### alch-blocker
`src/main/java/io/robrichardson/alchblocker/AlchBlockerPlugin.java`
- L213-232: MenuOptionClicked. If `isAlchOnItemEntry` and the item is blocked, it consumes unless `config.shiftClickAlchesBlocked() && client.isKeyPressed(KeyCode.KC_SHIFT)` (L224-227). The comment at L222-223 says the override "only declines to consume the user's own real click, never synthesises one".
- L275-292: identification uses entry type `WIDGET_TARGET_ON_WIDGET` plus the selected widget ID (high/low alch), with text as a fallback.
- Around L540-548: blocked items get `setHidden(true)` or `setOpacity` on the inventory widget.
- L329-386: a shift-only RUNELITE list-edit entry. The L346-348 comment explains why PostMenuSort needs the `isMenuOpen()` guard.

### no-misclicks-in-bank
`src/main/java/com/example/NoWieldInBankPlugin.java` L34-88: MenuEntryAdded. If the bank widget is open (`InterfaceID.BANKMAIN`, L90-93) and the option starts with wield/wear/drink/eat and is enabled, it truncates the last entry (`Arrays.copyOf(entries, entries.length - 1)`, L86-87).

### hotkeyable-menu-swaps (geheur More-menu-entry-swaps)
`src/main/java/com/hotkeyablemenuswaps/HotkeyableMenuSwapsPlugin.java`

`isProtected` (L1589-1601), quoted in full:
```java
private boolean isProtected(MenuEntry entry)
{
    MenuAction type = entry.getType();
    if (type == WIDGET_TARGET_ON_PLAYER || type == WIDGET_TARGET_ON_NPC) {
        if (WidgetUtil.componentToInterface(client.getSelectedWidget().getId()) == MAGIC_SPELLBOOK) {
            return true;
        }
    }
    if (mayNotBeLeftClick(entry)) {
        return true;
    }
    return false;
}
```
`mayNotBeLeftClick` (L1603-1618), quoted in full:
```java
private boolean mayNotBeLeftClick(MenuEntry entry)
{
    MenuAction type = entry.getType();
    if (type.getId() >= PLAYER_FIRST_OPTION.getId() && type.getId() <= PLAYER_EIGHTH_OPTION.getId()) {
        return true;
    }
    if (type == MenuAction.NPC_FOURTH_OPTION || type == MenuAction.NPC_FIFTH_OPTION) {
        if (entry.getNpc() != null) {
            String[] actions = entry.getNpc().getTransformedComposition().getActions();
            if (actions.length >= 4 && "Lure".equals(actions[3]) || actions.length >= 5 && "Knock-Out".equals(actions[4])) {
                return true;
            }
        }
    }
    return false;
}
```
How they are used:
- L1397-1399: if the current top entry `mayNotBeLeftClick`, the plugin does no custom swap at all.
- L1423-1425: the swap is aborted if `isProtected(topEntry) || isProtected(entryToSwap)`.
- L1653: custom hides never remove an `isProtected` entry.

What they protect against:
- (a) Any **player** option (Attack, Trade, Follow, Report, etc., PLAYER_FIRST..EIGHTH) becoming, or being displaced from, left-click.
- (b) **Spell cast on a player or NPC** (WIDGET_TARGET_ON_PLAYER/NPC while a magic spellbook spell is selected) being swapped or hidden.
- (c) The NPC **"Lure" (op 4) and "Knock-Out" (op 5)** options, i.e. the blackjacking/pickpocket-lure flow (Pollnivneach/thieving), becoming left-click.

There is **no wilderness or world-type check** in the plugin (grep for Wilderness/WorldType/PVP returns nothing). Both methods arrived in commit `e31af8a "Add custom swapper."`.

Caveat for mirroring: `client.getSelectedWidget()` can be null. L1593 would throw an NPE in that case, but only when the type is WIDGET_TARGET_ON_*, which implies a selected widget. Add a null check in Guard Rails anyway.

## Implications for Guard Rails

| Our mode | Closest approved precedent | Match quality / flags |
|---|---|---|
| **1. Force right-click via `MenuShouldLeftClick.setForceRightClick`** | **smithing-misclick-prevention** (SmithingMenuManager L166-183) | Direct precedent with the same API. Note that smithing scopes it narrowly (anvil interface open plus specific targets). |
| **2. `setDeprioritized(true)`** | **misclick-guard** (L107-123), but it is called in **MenuEntryAdded**, not PostMenuSort | Precedent for the API, not for the event. No approved plugin we checked calls `setDeprioritized` in PostMenuSort (**UNVERIFIED** whether doing so even takes effect after sorting, because deprioritization is applied during the sort). Recommendation: set it in MenuEntryAdded as misclick-guard does. **Surprise:** no-misclick-repot, whose description says "Deprioritize", does **not** use `setDeprioritized`. misclick-guard also pairs deprioritize with consuming non-menu left-clicks and a blank-CANCEL top entry so the left-click does nothing. |
| **3. Modifier-key gate** | **accidental-teleport-blocker** (L272-303: consume unless CTRL/SHIFT is held, chat message), **stopmisclickingtiles** (hotkey HOLD/TOGGLE bypass, consume), **alch-blocker** (shift override skips consume) | Well precedented, but **every precedent enforces by `event.consume()` in MenuOptionClicked**. None hides or reorders entries based on the modifier. If our gate is menu-based (for example, a force-right-click unless the key is held), it is a combination of mode 1 and a key check, with no exact precedent. If it is consume-based, it matches all three precedents. |
| **4. Harmless top entry with `setForceLeftClick` that arms the real action after press-and-hold** | **no-misclick-repot** (L406-419: RUNELITE "Waiting..." entry, `setForceLeftClick(true)`, `onClick(entry -> { })`). Also misclick-guard's blank CANCEL on top (L227-272). | The placeholder part has a precedent: a top RUNELITE no-op entry with setForceLeftClick, real entry left in place, nothing consumed or removed. The **arming after press-and-hold has NO precedent.** No approved plugin we checked uses a hold timer, and none has a placeholder whose click changes later behavior. repot's onClick is empty and its guard lifts only on game state (buff timer). Our onClick or hold logic must not trigger the real action programmatically. alch-blocker's comment ("never synthesises one") shows reviewers care about this. Keep it so the user's own next real click performs the action, e.g. arming removes the placeholder on the next PostMenuSort. Also add an `isMenuOpen()` guard, which repot lacks. |

Additional notes:
- **PvP/wilderness:** none of the precedents has a wilderness or world-type safety check on enforcement. The only "protect" logic is skiller-guard and hotkeyable-menu-swaps never touching **player** entries, and hotkeyable-menu-swaps never making player, spell-on-player/NPC or Lure/Knock-Out left-click. Mirroring `isProtected`/`mayNotBeLeftClick` means: (i) never make a protected entry the top or left-click entry through our modes, especially mode 4's arming and any reordering, and (ii) never hide them. If we want a wilderness check, it would be our own addition with no precedent to copy.
- **Removal plus consume** is common: quest-food-guard, no-misclicks-in-bank, misclick-guard Disabled and skiller-guard remove entries, and five plugins consume. Our four modes are softer than these.
- RUNELITE-type added entries appear in 6 of 10 plugins, mostly as shift-held config toggles. They are a normal pattern.
