package com.guardrails.model;

import com.guardrails.model.condition.Skulled;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class RuleTest
{
	private static Rule.Builder valid()
	{
		return Rule.builder()
			.name("Test")
			.target(Target.item(GuardedAction.DROP, ItemGroup.ANY_ITEM))
			.mode(Mode.RIGHT_CLICK_ONLY);
	}

	@Test
	public void defaults()
	{
		Rule r = valid().build();
		assertTrue(r.isEnabled());
		assertEquals(Modifier.SHIFT, r.getModifier());
		assertEquals(Rule.DEFAULT_HOLD_MILLIS, r.getHoldMillis());
		assertFalse(r.isAllowInWilderness());
		assertTrue(r.getConditions().isEmpty());
	}

	@Test
	public void toBuilderRoundTrips()
	{
		Rule r = Samples.everything();
		assertEquals(r, r.toBuilder().build());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void conditionsAreImmutable()
	{
		valid().build().getConditions().add(new Skulled(true));
	}

	@Test(expected = InvalidRuleException.class)
	public void requiresTarget()
	{
		valid().target(null).build();
	}

	@Test(expected = InvalidRuleException.class)
	public void requiresMode()
	{
		valid().mode(null).build();
	}

	@Test(expected = InvalidRuleException.class)
	public void requiresName()
	{
		valid().name("   ").build();
	}

	@Test(expected = InvalidRuleException.class)
	public void rejectsLongName()
	{
		valid().name("x".repeat(Rule.MAX_NAME_LENGTH + 1)).build();
	}

	@Test(expected = InvalidRuleException.class)
	public void rejectsChatTagsInName()
	{
		valid().name("<col=ff0000>Hi").build();
	}

	@Test(expected = InvalidRuleException.class)
	public void rejectsControlCharsInName()
	{
		valid().name("a\nb").build();
	}

	@Test
	public void trimsName()
	{
		assertEquals("Test", valid().name("  Test ").build().getName());
	}

	@Test
	public void holdMillisBounds()
	{
		valid().holdMillis(Rule.MIN_HOLD_MILLIS).build();
		valid().holdMillis(Rule.MAX_HOLD_MILLIS).build();
		assertInvalid(valid().holdMillis(Rule.MIN_HOLD_MILLIS - 1));
		assertInvalid(valid().holdMillis(Rule.MAX_HOLD_MILLIS + 1));
	}

	@Test
	public void conditionLimit()
	{
		Rule.Builder b = valid();
		for (int i = 0; i < Rule.MAX_CONDITIONS; i++)
		{
			b.condition(new Skulled(true));
		}
		b.build();
		assertInvalid(b.condition(new Skulled(true)));
	}

	@Test(expected = InvalidRuleException.class)
	public void rejectsNullCondition()
	{
		valid().condition(null).build();
	}

	@Test
	public void wildernessOptInOnlyForCarriedItems()
	{
		valid().allowInWilderness(true).build();
		assertInvalid(valid().target(Target.item(GuardedAction.WITHDRAW, ItemGroup.ANY_ITEM)).allowInWilderness(true));
		assertInvalid(valid().target(Target.object(GuardedAction.PULL, ObjectGroup.WILDERNESS_LEVERS))
			.allowInWilderness(true));
		assertInvalid(valid().target(Target.spell(TeleportSpell.ANY_TELEPORT)).allowInWilderness(true));
	}

	@Test
	public void targetRejectsWrongKind()
	{
		assertInvalidTarget(() -> Target.item(GuardedAction.ENTER, ItemGroup.ANY_ITEM));
		assertInvalidTarget(() -> Target.object(GuardedAction.DRINK, ObjectGroup.INFERNO_ENTRANCE));
		assertInvalidTarget(() -> Target.object(GuardedAction.CAST_TELEPORT, ObjectGroup.INFERNO_ENTRANCE));
		assertInvalidTarget(() -> Target.item(null, ItemGroup.ANY_ITEM));
		assertInvalidTarget(() -> Target.item(GuardedAction.DROP, null));
		assertInvalidTarget(() -> Target.spell(null));
	}

	@Test
	public void describe()
	{
		assertEquals("Drink · Divine super combat potions", Samples.repotGuard().getTarget().describe());
		assertEquals("Cast teleport · Any teleport spell", Target.spell(TeleportSpell.ANY_TELEPORT).describe());
	}

	private static void assertInvalid(Rule.Builder b)
	{
		try
		{
			b.build();
		}
		catch (InvalidRuleException expected)
		{
			return;
		}
		throw new AssertionError("expected InvalidRuleException");
	}

	private static void assertInvalidTarget(Runnable r)
	{
		try
		{
			r.run();
		}
		catch (InvalidRuleException expected)
		{
			return;
		}
		throw new AssertionError("expected InvalidRuleException");
	}
}
