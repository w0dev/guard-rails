# RuneLite API surface: verified notes

Source: shallow clone of https://github.com/runelite/runelite at `research/runelite/`
- master HEAD: `ddd0669ef4d7494fd420cb507e3c1fa62bab8023` (Sun Oct 4 2026), `gradle.properties`: `project.build.version=1.13.2-SNAPSHOT`
- Latest release tag: **`runelite-parent-1.13.1`** (commit `6945ec38`, Sep 30 2026). Before that: 1.13.0, 1.12.39.
- `git diff runelite-parent-1.13.1 HEAD` on MenuEntry/MenuAction/Menu/events/WorldType: **no changes**. Everything below applies to 1.13.1 too.

Paths are relative to `runelite-api/src/main/java/net/runelite/api/` (API) or `runelite-client/src/main/java/net/runelite/client/` (client) unless given in full.

**Important caveat:** the code that posts `MenuShouldLeftClick`, `PostMenuSort`, `MenuOptionClicked`, `MenuOpened` and `MenuEntryAdded`, sorts the menu, and runs `MenuEntry.onClick` callbacks lives in the closed-source mixins of `net.runelite:injected-client` (`runelite-client/build.gradle.kts:50`: `runtimeOnly("net.runelite:injected-client:${project.version}")`). It is **not in this repo**. A grep for `new MenuOptionClicked`, `MenuShouldLeftClick` or `PostMenuSort` in the client turns up only subscribers and tests. Statements about dispatch order below come from the API javadocs (which the RuneLite devs maintain) and from how built-in plugins use the events.

---

## MenuEntry (`MenuEntry.java`)
```java
public interface MenuEntry
String getOption();                MenuEntry setOption(String option);
String getTarget();                MenuEntry setTarget(String target);
int getIdentifier();               MenuEntry setIdentifier(int identifier);
MenuAction getType();              MenuEntry setType(MenuAction type);
int getParam0();                   MenuEntry setParam0(int param0);
int getParam1();                   MenuEntry setParam1(int param1);
boolean isForceLeftClick();        MenuEntry setForceLeftClick(boolean forceLeftClick);
int getWorldViewId();              MenuEntry setWorldViewId(int worldViewId);
boolean isDeprioritized();         MenuEntry setDeprioritized(boolean deprioritized);
MenuEntry onClick(Consumer<MenuEntry> callback);
Consumer<MenuEntry> onClick();
boolean isItemOp();
int getItemOp();                   // "@return 1-5"
int getItemId();                   MenuEntry setItemId(int itemId);
@Nullable Widget getWidget();
@Nullable NPC getNpc();
@Nullable Player getPlayer();
@Nullable Actor getActor();
@Nullable Menu getSubMenu();
@Nonnull Menu createSubMenu();
void deleteSubMenu();
```
Javadoc notes:
- setForceLeftClick: "If this is true and you have single mouse button on and this entry is the top entry the right click menu will not be opened when you left click ... This is used for shift click". So it only affects the single-mouse-button mode. No built-in plugin calls it on master (grep finds nothing).
- setDeprioritized: "Deprioritized menus are sorted in the menu to be below the other menu entries." The opcode offset is `MenuAction.MENU_ACTION_DEPRIORITIZE_OFFSET = 2000`, which the vanilla client applies to sort an entry below everything else, including Walk here.
- isItemOp: "'Use' and 'Examine' are not considered item ops."
- getWidget: "...with an associated widget. Such as eg, CC_OP."
- Setters return `MenuEntry`, so calls chain.

## Menu and the Client menu methods
`Menu.java`:
```java
public interface Menu
MenuEntry createMenuEntry(int idx);   // "Accepts negative indexes eg. -1 inserts at the end."
MenuEntry[] getMenuEntries();
void setMenuEntries(MenuEntry[] entries); // "typically be used in the context of the MenuOpened event, since setting the menu entries will be overwritten the next frame"
void removeMenuEntry(MenuEntry entry);
int getMenuX(); int getMenuY(); int getMenuWidth(); int getMenuHeight(); // only valid if menu open
```
`Client.java`:
```java
@Nonnull Menu getMenu();                                  // :594
@Deprecated MenuEntry createMenuEntry(int idx);           // :604  -> use Menu#createMenuEntry
@Deprecated MenuEntry[] getMenuEntries();                 // :615  -> use Menu#getMenuEntries
@Deprecated void setMenuEntries(MenuEntry[] entries);     // :628  -> use Menu#setMenuEntries
boolean isMenuOpen();                                     // :647
boolean isKeyPressed(@MagicConstant(valuesFromClass = KeyCode.class) int keycode); // :1992
```
The menu array is ordered so that the **last index is the top entry**, which is the left-click default (MenuOpened javadoc; MenuManager comment "higher-index entries appearing toward the top"). The client rebuilds menu entries every client tick while the menu is closed. It does not rebuild them while the menu is open (MenuEntrySwapperPlugin:1704 comment: "The menu is not rebuilt when it is open").

## MenuAction (`MenuAction.java`), quoted constants
```
@Deprecated ITEM_USE_ON_GAME_OBJECT(1), WIDGET_TARGET_ON_GAME_OBJECT(2),
GAME_OBJECT_FIRST_OPTION(3), GAME_OBJECT_SECOND_OPTION(4), GAME_OBJECT_THIRD_OPTION(5),
GAME_OBJECT_FOURTH_OPTION(6), GAME_OBJECT_FIFTH_OPTION(1001),
@Deprecated ITEM_USE_ON_NPC(7), WIDGET_TARGET_ON_NPC(8),
NPC_FIRST_OPTION(9) .. NPC_FIFTH_OPTION(13),
@Deprecated ITEM_USE_ON_PLAYER(14), WIDGET_TARGET_ON_PLAYER(15),
@Deprecated ITEM_USE_ON_GROUND_ITEM(16), WIDGET_TARGET_ON_GROUND_ITEM(17),
GROUND_ITEM_FIRST_OPTION(18) .. GROUND_ITEM_FIFTH_OPTION(22),
WALK(23),
WIDGET_TYPE_1(24), WIDGET_TARGET(25), WIDGET_CLOSE(26), WIDGET_TYPE_4(28), WIDGET_TYPE_5(29), WIDGET_CONTINUE(30),
@Deprecated ITEM_USE_ON_ITEM(31), @Deprecated WIDGET_USE_ON_ITEM(32),
@Deprecated ITEM_FIRST_OPTION(33) .. @Deprecated ITEM_FIFTH_OPTION(37), @Deprecated ITEM_USE(38),
WIDGET_FIRST_OPTION(39) .. WIDGET_FIFTH_OPTION(43),
PLAYER_FIRST_OPTION(44), PLAYER_SECOND_OPTION(45), PLAYER_THIRD_OPTION(46), PLAYER_FOURTH_OPTION(47),
PLAYER_FIFTH_OPTION(48), PLAYER_SIXTH_OPTION(49), PLAYER_SEVENTH_OPTION(50), PLAYER_EIGHTH_OPTION(51),
CC_OP(57),                      // "normal priority child component actions"
WIDGET_TARGET_ON_WIDGET(58),
SET_HEADING(60),
WORLD_ENTITY_FIRST_OPTION(63) .. WORLD_ENTITY_FIFTH_OPTION(67),
RUNELITE_WIDGET(998),           // "RuneLite menu that is a widge[t]"
RUNELITE_HIGH_PRIORITY(999),
EXAMINE_OBJECT(1002), EXAMINE_NPC(1003), EXAMINE_ITEM_GROUND(1004), @Deprecated EXAMINE_ITEM(1005),
CANCEL(1006), CC_OP_LOW_PRIORITY(1007), EXAMINE_WORLD_ENTITY(1013),
RUNELITE(1500),                 // "Menu action injected by runelite for its menu items."
RUNELITE_OVERLAY(1501), RUNELITE_OVERLAY_CONFIG(1502),
RUNELITE_PLAYER(1503),          // "target a player and have its identifier set to a player index"
RUNELITE_INFOBOX(1504),
RUNELITE_LOW_PRIORITY(1506),    // "Like RUNELITE, except clicking always forces the menu open."
UNKNOWN(-1);
public static final int MENU_ACTION_DEPRIORITIZE_OFFSET = 2000;
@Deprecated public static final MenuAction PLAYER_EIGTH_OPTION = PLAYER_EIGHTH_OPTION;
public int getId();  public static MenuAction of(int id);
```
- The spell-on-target types are named `WIDGET_TARGET_ON_PLAYER` (15) and `WIDGET_TARGET_ON_NPC` (8). The old `SPELL_CAST_ON_*` names are **NOT FOUND**. Item-use-on and spell-on both go through `WIDGET_TARGET_ON_*` now. The `ITEM_USE_ON_*` constants are deprecated.
- **Inventory item ops are `CC_OP` / `CC_OP_LOW_PRIORITY`.** `ITEM_*_OPTION` are deprecated. Use `MenuEntry.isItemOp()`, `getItemOp()` and `getItemId()` instead.
- There is no separate "Attack" type. Attack on an NPC is usually `NPC_SECOND_OPTION` (see CorpPlugin:260). Attack on a player is whichever PLAYER_*_OPTION slot the server assigned it. Check `getOption()`.

## RUNELITE-type entries: client-only?
- What the open source shows: `MenuManager.onMenuEntryAdded` (client `menus/MenuManager.java:143-146`) builds custom entries with `.setType(MenuAction.RUNELITE).setParam1(widgetId).onClick(currentMenu.callback)`. Overlay and infobox entries use RUNELITE_OVERLAY / RUNELITE_INFOBOX, and their handlers are ordinary `MenuOptionClicked` subscribers (e.g. `ui/overlay/infobox/InfoBoxOverlay.java:211-225`). Nothing in the open client sends a packet for these.
- What is **NOT verifiable in the repo**: the dispatcher that runs `onClick` and then hands the click to the vanilla `menuAction`. It lives in the closed injected-client. Opcodes 998/999/1500–1506 are RuneLite inventions, and the vanilla OSRS menuAction switch has no case for them, so they produce no server packet. That is the design intent and matches long-standing behaviour, **but I did not confirm it in source**. To confirm it, someone would need to decompile `net.runelite:injected-client:1.13.1`. I did not download that jar.
- Practical consequence: a plugin that turns a dangerous entry into a RUNELITE no-op (or swaps in a RUNELITE "confirm" entry) gets a client-side click. `MenuOptionClicked` still fires for it, with `getMenuAction()==RUNELITE`. Calling `MenuOptionClicked.consume()` on a real entry is the documented way to stop vanilla handling ("will not be passed on for handling by vanilla client code").

## Events (`events/`)
```java
@Data public class MenuShouldLeftClick { private boolean forceRightClick; } // "If set to true, the menu will open on left click."
// => isForceRightClick() / setForceRightClick(boolean)
public class PostMenuSort {} // "Posted after the menu is sorted, but before clicks are processed. This is only fired if the menu isn't open, and shouldn't be used as a general purpose client tick event."
public class MenuOptionClicked {
  @Getter MenuEntry getMenuEntry(); @Getter boolean isConsumed(); void consume();
  int getParam0(); int getParam1(); String getMenuOption(); String getMenuTarget();
  MenuAction getMenuAction(); int getId(); boolean isItemOp(); int getItemOp(); int getItemId();
  @Nullable Widget getWidget(); @Deprecated int getActionParam(); @Deprecated int getWidgetId(); }
  // fires for every left-click action too; "when there is no action performed when left-clicking ... triggers with the 'Cancel' action"
@Data public class MenuOpened { MenuEntry[] menuEntries; MenuEntry getFirstEntry(); } // last array element is the top entry
public class MenuEntryAdded { @Getter MenuEntry getMenuEntry(); String getOption(); String getTarget();
  int getType() /* int id, not MenuAction */; int getIdentifier(); int getActionParam0(); int getActionParam1(); int getItemId(); }
@Data public class VarbitChanged { @Varp int varpId=-1; @Varbit int varbitId=-1; long value;
  int getValue(); long getLongValue(); @Deprecated int getIndex(); } // varbitId == -1 for varp changes
@Data public class GameTick {}   // once per server tick (~0.6s), after all packets processed
public class ClientTick {}       // every client tick (20ms)
```
**Ordering** (ClientTick javadoc, verbatim list):
```
1. packet processing  2. interface tick  3. client tick (this event)  4. clientscript execution
5. menu sorting (see PostMenuSort)  6. menu click detection and drag handling  7. PostClientTick event
```
- `MenuEntryAdded` fires each time an entry is added while the client rebuilds the menu. In practice that is roughly every client tick for every entry, while the menu is closed. The client's own deprioritization and Walk-here swaps run in `onClientTick` (MenuEntrySwapperPlugin:1630-1697, which calls `setDeprioritized(true)` before the sort). The reordering swaps run in `onPostMenuSort` (:1702).
- `PostMenuSort` fires once per client tick (50/s) after the sort, and only while the menu is closed. It comes before click detection, so changes made there decide what a click does.
- `MenuShouldLeftClick` is posted by the injected client at click time (step 6), when it decides whether a left click opens the menu. The poster is NOT FOUND in the repo (closed mixins). The only consumer is BankPlugin (`plugins/bank/BankPlugin.java:218-239`). It sets a flag in `onMenuEntryAdded` and then calls `event.setForceRightClick(true)`, which suggests the event fires per left-click attempt, not per frame.
- Rendering is separate from these steps. `Hooks.frame()` posts `BeforeRender`, and `Hooks.tick()` posts `GameTick` once per server tick, at the start of the client tick that follows it (`callback/Hooks.java:234-246`). `Hooks.tickEnd()` posts `PostClientTick`.

## WorldType (`WorldType.java`), full list
```
MEMBERS(1), PVP(1<<2), BOUNTY(1<<5), PVP_ARENA(1<<6), SKILL_TOTAL(1<<7), QUEST_SPEEDRUNNING(1<<8),
HIGH_RISK(1<<10), LAST_MAN_STANDING(1<<14), BETA_WORLD(1<<16), LEGACY_ONLY(1<<22), EOC_ONLY(1<<23),
NOSAVE_MODE(1<<25), TOURNAMENT_WORLD(1<<26), FRESH_START_WORLD(1<<27), DEADMAN(1<<29), SEASONAL(1<<30)
static EnumSet<WorldType> fromMask(int); static int toMask(EnumSet<WorldType>);
static boolean isPvpWorld(Collection<WorldType>)  // PVP_WORLD_TYPES = {DEADMAN, PVP}
```
`Client`: `EnumSet<WorldType> getWorldType();` (:1757). There is **no hardcore, ironman or roguelike constant**. `isPvpWorld` does not count HIGH_RISK, BOUNTY or PVP_ARENA, so handle those yourself.

## Account type
- `Varbits.ACCOUNT_TYPE = 1777`. The whole `Varbits` class is `@Deprecated`. Javadoc values: `0 = normal, 1 = ironman, 2 = ultimate ironman, 3 = hardcore ironman, 4 = group ironman, 5 = hardcore group ironman, 6 = unranked group ironman`.
- gameval equivalent: **`VarbitID.IRONMAN = 1777`** (`gameval/VarbitID.java:1378`). The client uses it, e.g. `client.getVarbitValue(VarbitID.IRONMAN)` in DailyTasksPlugin:212 (`!= 2 /* UIM */`) and HiscorePlugin:277.
- `Client.getAccountType()` is `@Deprecated` ("see Varbits#ACCOUNT_TYPE"). The `vars/AccountType` enum has NORMAL, IRONMAN, ULTIMATE_IRONMAN, HARDCORE_IRONMAN, GROUP_IRONMAN and HARDCORE_GROUP_IRONMAN. It has no entry for unranked GIM.
- `int getVarbitValue(@Varbit int varbit)` (Client:763); `int getVarpValue(@Varp int varpId)` (:781).

## Input: KeyManager / MouseManager (client `input/`)
```java
public class KeyManager { void registerKeyListener(KeyListener); void unregisterKeyListener(KeyListener); }
public interface KeyListener extends java.awt.event.KeyListener { default boolean isEnabledOnLoginScreen(){return false;} default void focusLost(){} }
public class MouseManager { void registerMouseListener(MouseListener); void registerMouseListener(int position, MouseListener);
  void unregisterMouseListener(MouseListener); void registerMouseWheelListener(MouseWheelListener); void registerMouseWheelListener(int, MouseWheelListener); void unregisterMouseWheelListener(MouseWheelListener); }
public interface MouseListener { MouseEvent mouseClicked(MouseEvent); MouseEvent mousePressed(MouseEvent); MouseEvent mouseReleased(MouseEvent);
  MouseEvent mouseEntered(MouseEvent); MouseEvent mouseExited(MouseEvent); MouseEvent mouseDragged(MouseEvent); MouseEvent mouseMoved(MouseEvent); }
// also net.runelite.client.input.MouseAdapter (convenience base)
```
- These are **listeners only**. They are fed from `Hooks.mousePressed` and `keyPressed` (callback/Hooks.java:322-376) before the game handles the event. They cannot synthesize input.
- A listener **can consume** an event: `processKeyPressed` stops when `keyEvent.isConsumed()`, and `processMousePressed` returns the returned/consumed event to the client (KeyManager:73-96, MouseManager:88-105). A consumed event never reaches the game. That makes press-and-hold gating possible: record the mousePressed and mouseReleased timestamps (`MouseEvent.getWhen()` or `System.currentTimeMillis()`). Event-bus subscribers run on the client thread, but the listeners run on the AWT/game input path, so synchronize any shared state.

## ConfigManager RS-profile methods (client `config/ConfigManager.java`)
```java
public String getRSProfileKey()                                                  // :244
public String getRSProfileConfiguration(String groupName, String key)            // :817 (null if no rsProfileKey yet)
public <T> T getRSProfileConfiguration(String groupName, String key, Type clazz) // :845
public <T> void setRSProfileConfiguration(String groupName, String key, T value) // :931 (creates profile from local player if needed)
public void unsetRSProfileConfiguration(String groupName, String key)            // :1012
public List<String> getRSProfileConfigurationKeys(String group, String profile, String keyPrefix) // :778
public List<RuneScapeProfile> getRSProfiles()                                    // :1461
```

## ItemManager / compositions
`game/ItemManager.java`:
```java
public long getItemPrice(int itemID)                         // :316
public long getItemPriceWithSource(int itemID, boolean useWikiPrice) // :328
public List<ItemPrice> search(String itemName)               // :434 tradeable items only, case-insensitive substring over price list
@Nonnull public ItemComposition getItemComposition(int itemId) // :457  -> client.getItemDefinition(itemId)
public int canonicalize(int itemID)                          // :465 un-noted, un-placeholdered, worn->inv mapping
public ItemStats getItemStats(int itemId)                    // :394
```
- There is **no exact name-to-id lookup** (NOT FOUND). `search` only covers tradeables and does substring matching. To get an id from a name you have to compare `getItemComposition(id).getName()` yourself. Compositions must be read on the client thread.
- `ItemComposition`: `String getName()` (may get " (Members)" appended on F2P), `String getMembersName()`, `int getId()`, `int getNote()`, `int getLinkedNoteId()`, `int getPlaceholderTemplateId()`.
- `Client`: `@Nonnull ItemComposition getItemDefinition(int id)` (:394), `ObjectComposition getObjectDefinition(int objectId)` (:1007), `NPCComposition getNpcDefinition(int npcId)` (:1016).
- `ObjectComposition`: `int getId()`, `String getName()`, `String[] getActions()`, `int[] getImpostorIds()`, `ObjectComposition getImpostor()` ("throws NullPointerException if getImpostorIds() is null"). The pattern used in MenuEntrySwapperPlugin:1660-1665:
  ```java
  ObjectComposition oc = client.getObjectDefinition(objectId);
  if (oc.getImpostorIds() != null) { oc = oc.getImpostor(); objectId = oc.getId(); }
  ```
  For NPCs use `npc.getTransformedComposition()`.

## gameval package (`gameval/`)
Files: `AnimationID, DBTableID, InterfaceID, InventoryID, ItemID, NpcID, ObjectID, ObjectID1, SpotanimID, SpriteID, VarClientID, VarPlayerID, VarbitID`.
- The old `net.runelite.api.ItemID`, `ObjectID` and `InventoryID` (enum) are `@Deprecated`. `gameval.InventoryID` replaces them with int constants: `INV = 93`, `WORN = 94`, `BANK = 95`. Use `ItemContainer getItemContainer(int id)` (Client:1309). The enum overload at :1299 also exists.
- Interfaces: group ids `InterfaceID.BANKMAIN = 12`, `BANKSIDE = 15`, `BANK_DEPOSITBOX = 192`, `GE_OFFERS = 465`, `GE_COLLECT = 402`, `GE_PRICECHECKER = 464`, `GE_OFFERS_SIDE` (exists, used in WidgetItemOverlay:109). Component ids are nested classes: `InterfaceID.Bankmain.UNIVERSE = 0x000c_0001`, `InterfaceID.GeOffers.UNIVERSE = 0x01d1_0000`.
- Is the bank or GE open? Built-ins use one of two checks:
  - `Widget w = client.getWidget(InterfaceID.Bankmain.UNIVERSE); open = w != null && !w.isHidden();` (BankTagsPlugin:245, ItemStatPlugin:142-143 for GeOffers)
  - track `WidgetLoaded` / `WidgetClosed` where `event.getGroupId() == InterfaceID.BANKMAIN` (AntiDragPlugin:192, TabInterface:302).
  - Signature: `Widget getWidget(@Component int componentId)` (Client:543).

## Wilderness / PvP
- `Varbits.IN_WILDERNESS = 5963` (deprecated; "0 = Outside wilderness, 1 = In wilderness"). gameval: **`VarbitID.INSIDE_WILDERNESS = 5963`**.
- PvP-area varbit: `VarbitID.PVP_AREA_CLIENT = 8121` (old `Varbits.PVP_SPEC_ORB`). PlayerIndicatorsService:101 uses `INSIDE_WILDERNESS == 1 || PVP_AREA_CLIENT == 1`.
- World types: `PVP`, `HIGH_RISK`, `DEADMAN`, `PVP_ARENA`, `BOUNTY`, `LAST_MAN_STANDING`, plus `WorldType.isPvpWorld()`, which covers only PVP and DEADMAN.

## Potion / buff timer vars (from `plugins/timersandbuffs/TimersAndBuffsPlugin.java`)
VarbitID values:
`STAMINA_ACTIVE=25, STAMINA_DURATION=24, ANTIFIRE_POTION=3981, SUPER_ANTIFIRE_POTION=6101, PRAYER_REGENERATION_POTION_TIMER=11361, NZONE_OVERLOAD_POTION_EFFECTS=3955, RAIDS_OVERLOAD_TIMER=5418, DEADMAN_OVERLOAD_POTION_EFFECTS=10954, DIVINEATTACK_POTION_TIME=8429, DIVINESTRENGTH_POTION_TIME=8430, DIVINEDEFENCE_POTION_TIME=8431, DIVINERANGE_POTION_TIME=8432, DIVINEMAGIC_POTION_TIME=8433, DIVINECOMBAT_POTION_TIME=13663, DIVINEBASTION_POTION_TIME=13664, DIVINEBATTLEMAGE_POTION_TIME=13665, MOONLIGHT_POTION_TIME=10029`.
Other VarbitIDs the plugin uses: `GOADING_POTION_TIMER, SURGE_POTION_TIMER, STATRENEWAL_POTION_TIMER, IMBUED_HEART_TIMER, MAGIC_IMBUE_ACTIVE, VENGEANCE_*, TELEBLOCK_CYCLES, ARCEUUS_*_ACTIVE/COOLDOWN, TOA_MIDRAIDLOOT_*, WGS_SMOULDERING_*`.
- **Antipoison and antivenom have no varbit.** They use the varp `VarPlayerID.POISON = 102`. In TimersAndBuffsPlugin:124 and :342-361, a negative value means immunity: `VENOM_VALUE_CUTOFF = -38 // Antivenom < -38 <= Antipoison < 0`. Each step lasts 30 ticks.

## Skull
`Player`: `@MagicConstant(valuesFromClass = SkullIcon.class) int getSkullIcon();` ("the id skull icon, or -1 if unskulled"). `SkullIcon` is now a class of int constants, **not an enum**: `NONE=-1, SKULL=0, SKULL_FIGHT_PIT=1, SKULL_HIGH_RISK=2, FORINTHRY_SURGE=3, SKULL_DEADMAN=7, LOOT_KEYS_ONE..FIVE=8..12, FORINTHRY_SURGE_DEADMAN=14, FORINTHRY_SURGE_KEYS_ONE..FIVE=15..19`. Note that FORINTHRY_SURGE (3) is not a PK skull.

## Built-in setDeprioritized / setForceRightClick call sites (paths under `runelite-client/src/main/java/net/runelite/client/plugins/`)
- `corp/CorpPlugin.java:266`: `menuEntry.setDeprioritized(true);` in `onMenuEntryAdded`, for the Dark energy core "Attack" (`NPC_SECOND_OPTION`). This is the closest existing analogue to a "don't left-click attack this" guard.
- `grounditems/GroundItemsPlugin.java:521`: `lastEntry.setDeprioritized(true);` for hidden ground items (onMenuEntryAdded).
- `menuentryswapper/MenuEntrySwapperPlugin.java:1671, 1686, 1695`: `menuEntry.setDeprioritized(true);` in `onClientTick`, for object, NPC and ground-item entries when shift-click Walk here or a custom option of -1 applies.
- `interfacestyles/InterfaceStylesPlugin.java:173, 189`: `setDeprioritized(false)` and a copy of the flag onto submenu entries.
- `bank/BankPlugin.java:236`: `event.setForceRightClick(true);` (the only MenuShouldLeftClick consumer).
- `setForceLeftClick`: no built-in callers. NOT FOUND in use.
