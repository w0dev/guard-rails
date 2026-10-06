package com.guardrails.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.runelite.api.MenuAction;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

/** The untargetable set is absent from {@link GuardedAction} by construction; these tests pin it. */
public class GuardedActionTest
{
	/** Options Guard Rails must never guard, by menu text. */
	private static final List<String> FORBIDDEN_OPTIONS = Arrays.asList(
		"attack", "trade", "trade with", "follow", "report", "challenge", "pickpocket", "knock-out",
		"lure", "walk here", "cast");

	/** Enum constant names that must never exist. */
	private static final List<String> FORBIDDEN_NAMES = Arrays.asList(
		"ATTACK", "CAST_ON_NPC", "CAST_ON_PLAYER", "TRADE", "FOLLOW", "REPORT", "PICKPOCKET",
		"KNOCK_OUT", "LURE", "WALK", "WALK_HERE");

	@Test
	public void noActionCanMatchNpcPlayerOrWalkEntries()
	{
		for (GuardedAction action : GuardedAction.values())
		{
			assertTrue(action + " overlaps NEVER_GUARDED",
				Collections.disjoint(action.getMenuTypes(), GuardedAction.NEVER_GUARDED));
		}
	}

	@Test
	public void neverGuardedCoversEveryNpcAndPlayerType()
	{
		Set<MenuAction> expected = EnumSet.noneOf(MenuAction.class);
		for (MenuAction type : MenuAction.values())
		{
			String n = type.name();
			boolean actorType = n.startsWith("NPC_") || n.startsWith("PLAYER_") || n.endsWith("_ON_NPC")
				|| n.endsWith("_ON_PLAYER") || n.equals("RUNELITE_PLAYER") || n.equals("WALK");
			if (actorType && !isDeprecated(type))
			{
				expected.add(type);
			}
		}
		assertTrue("missing: " + difference(expected, GuardedAction.NEVER_GUARDED),
			GuardedAction.NEVER_GUARDED.containsAll(expected));
	}

	@Test
	public void noDisplayNameIsAForbiddenOption()
	{
		for (GuardedAction action : GuardedAction.values())
		{
			assertFalse(action + " is a forbidden option",
				FORBIDDEN_OPTIONS.contains(action.getDisplayName().toLowerCase(Locale.ROOT)));
		}
	}

	@Test
	public void cannotNameAForbiddenAction()
	{
		for (String name : FORBIDDEN_NAMES)
		{
			try
			{
				GuardedAction.valueOf(name);
				fail(name + " must not be a GuardedAction");
			}
			catch (IllegalArgumentException expected)
			{
				// absent by construction
			}
		}
	}

	@Test
	public void spellActionsOnlyMatchOwnItemsOrSpellbook()
	{
		assertTrue(GuardedAction.HIGH_ALCHEMY.getMenuTypes().equals(EnumSet.of(MenuAction.WIDGET_TARGET_ON_WIDGET)));
		assertTrue(GuardedAction.LOW_ALCHEMY.getMenuTypes().equals(EnumSet.of(MenuAction.WIDGET_TARGET_ON_WIDGET)));
		assertTrue(GuardedAction.CAST_TELEPORT.getMenuTypes().equals(EnumSet.of(MenuAction.CC_OP)));
	}

	private static boolean isDeprecated(MenuAction type)
	{
		// Deprecated aliases RuneLite no longer produces; listed explicitly rather than via reflection.
		return type.name().equals("ITEM_USE_ON_NPC") || type.name().equals("ITEM_USE_ON_PLAYER");
	}

	private static Set<MenuAction> difference(Set<MenuAction> a, Set<MenuAction> b)
	{
		Set<MenuAction> d = EnumSet.noneOf(MenuAction.class);
		d.addAll(a);
		d.removeAll(b);
		return d;
	}
}
