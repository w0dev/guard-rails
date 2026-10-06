package com.guardrails.model;

import com.guardrails.model.condition.AccountIs;
import com.guardrails.model.condition.AccountType;
import com.guardrails.model.condition.Area;
import com.guardrails.model.condition.Click;
import com.guardrails.model.condition.ClickedWithin;
import com.guardrails.model.condition.CombatSpellWithin;
import com.guardrails.model.condition.Comparison;
import com.guardrails.model.condition.Condition;
import com.guardrails.model.condition.ConditionType;
import com.guardrails.model.condition.Effect;
import com.guardrails.model.condition.EffectActive;
import com.guardrails.model.condition.EquipmentContains;
import com.guardrails.model.condition.GameInterface;
import com.guardrails.model.condition.InArea;
import com.guardrails.model.condition.InterfaceOpen;
import com.guardrails.model.condition.InventoryContains;
import com.guardrails.model.condition.ItemValueAtLeast;
import com.guardrails.model.condition.OnWorld;
import com.guardrails.model.condition.Skulled;
import com.guardrails.model.condition.StatBoost;
import com.guardrails.model.condition.Vital;
import com.guardrails.model.condition.VitalBelow;
import com.guardrails.model.condition.WorldKind;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import net.runelite.api.Skill;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class ConditionTest
{
	private FakeGameContext ctx;

	@Before
	public void setUp()
	{
		ctx = new FakeGameContext();
	}

	@Test
	public void everyTypeHasASampleWithMatchingType()
	{
		Map<ConditionType, Condition> samples = Samples.conditions();
		assertEquals(EnumSet.allOf(ConditionType.class), samples.keySet());
		samples.forEach((type, c) ->
		{
			assertEquals(type, c.getType());
			assertFalse(c.describe().isEmpty());
		});
	}

	@Test
	public void onlyClickConditionsDependOnTheClick()
	{
		Samples.conditions().forEach((type, c) -> assertEquals(type.name(),
			type == ConditionType.CLICKED_WITHIN || type == ConditionType.ITEM_VALUE_AT_LEAST, c.dependsOnClick()));
	}

	@Test
	public void effectActive()
	{
		Condition active = new EffectActive(Effect.STAMINA, true);
		Condition inactive = new EffectActive(Effect.STAMINA, false);
		assertFalse(active.holds(ctx, Click.NONE));
		assertTrue(inactive.holds(ctx, Click.NONE));
		ctx.effects.add(Effect.STAMINA);
		assertTrue(active.holds(ctx, Click.NONE));
		assertFalse(inactive.holds(ctx, Click.NONE));
	}

	@Test
	public void statBoost()
	{
		Condition boosted = new StatBoost(Skill.ATTACK, Comparison.AT_LEAST, 5);
		Condition drained = new StatBoost(Skill.ATTACK, Comparison.BELOW, 0);
		ctx.level(Skill.ATTACK, 99, 104);
		assertTrue(boosted.holds(ctx, Click.NONE));
		assertFalse(drained.holds(ctx, Click.NONE));
		ctx.level(Skill.ATTACK, 99, 103);
		assertFalse(boosted.holds(ctx, Click.NONE));
		ctx.level(Skill.ATTACK, 99, 98);
		assertTrue(drained.holds(ctx, Click.NONE));
	}

	@Test
	public void vitalBelow()
	{
		Condition hp = new VitalBelow(Vital.HITPOINTS, 50);
		ctx.level(Skill.HITPOINTS, 100, 49);
		assertTrue(hp.holds(ctx, Click.NONE));
		ctx.level(Skill.HITPOINTS, 100, 50);
		assertFalse("exactly 50% is not below 50%", hp.holds(ctx, Click.NONE));
		ctx.level(Skill.HITPOINTS, 99, 50);
		assertFalse("50/99 is above 50%", hp.holds(ctx, Click.NONE));
		Condition prayer = new VitalBelow(Vital.PRAYER, 10);
		ctx.level(Skill.PRAYER, 70, 6);
		assertTrue(prayer.holds(ctx, Click.NONE));
		ctx.level(Skill.PRAYER, 70, 7);
		assertFalse(prayer.holds(ctx, Click.NONE));
	}

	@Test
	public void vitalBelowWithNoLevelNeverHolds()
	{
		ctx.level(Skill.HITPOINTS, 0, 0);
		assertFalse(new VitalBelow(Vital.HITPOINTS, 100).holds(ctx, Click.NONE));
	}

	@Test
	public void setMembershipConditions()
	{
		assertFalse(new InArea(Area.GRAND_EXCHANGE).holds(ctx, Click.NONE));
		ctx.areas.add(Area.GRAND_EXCHANGE);
		assertTrue(new InArea(Area.GRAND_EXCHANGE).holds(ctx, Click.NONE));

		assertFalse(new InterfaceOpen(GameInterface.BANK).holds(ctx, Click.NONE));
		ctx.interfaces.add(GameInterface.BANK);
		assertTrue(new InterfaceOpen(GameInterface.BANK).holds(ctx, Click.NONE));

		assertFalse(new OnWorld(WorldKind.HARDCORE).holds(ctx, Click.NONE));
		ctx.worlds.add(WorldKind.HARDCORE);
		assertTrue(new OnWorld(WorldKind.HARDCORE).holds(ctx, Click.NONE));
	}

	@Test
	public void accountIs()
	{
		Condition hc = new AccountIs(EnumSet.of(AccountType.HARDCORE_IRONMAN, AccountType.HARDCORE_GROUP_IRONMAN));
		assertFalse(hc.holds(ctx, Click.NONE));
		ctx.accountType = AccountType.HARDCORE_GROUP_IRONMAN;
		assertTrue(hc.holds(ctx, Click.NONE));
	}

	@Test(expected = InvalidRuleException.class)
	public void accountIsRejectsEmpty()
	{
		new AccountIs(Collections.emptySet());
	}

	@Test
	public void containers()
	{
		assertTrue(new InventoryContains(ItemGroup.STAMINA, false).holds(ctx, Click.NONE));
		ctx.inventory.add(ItemGroup.STAMINA);
		assertTrue(new InventoryContains(ItemGroup.STAMINA, true).holds(ctx, Click.NONE));
		assertFalse(new InventoryContains(ItemGroup.STAMINA, false).holds(ctx, Click.NONE));
		assertFalse("inventory is not equipment", new EquipmentContains(ItemGroup.STAMINA, true).holds(ctx, Click.NONE));
	}

	@Test
	public void skulled()
	{
		assertTrue(new Skulled(false).holds(ctx, Click.NONE));
		ctx.skulled = true;
		assertTrue(new Skulled(true).holds(ctx, Click.NONE));
	}

	@Test
	public void combatSpellWithin()
	{
		Condition c = new CombatSpellWithin(10);
		assertFalse("never cast", c.holds(ctx, Click.NONE));
		ctx.millisSinceCombatSpell = 10_000;
		assertTrue(c.holds(ctx, Click.NONE));
		ctx.millisSinceCombatSpell = 10_001;
		assertFalse(c.holds(ctx, Click.NONE));
	}

	@Test
	public void clickDependent()
	{
		assertFalse(new ClickedWithin(2).holds(ctx, Click.NONE));
		assertTrue(new ClickedWithin(2).holds(ctx, new Click(1, 1, 2000)));
		assertFalse(new ItemValueAtLeast(100).holds(ctx, Click.NONE));
		assertTrue(new ItemValueAtLeast(100).holds(ctx, new Click(1, 100, Long.MAX_VALUE)));
		assertFalse(new ItemValueAtLeast(100).holds(ctx, new Click(1, 99, Long.MAX_VALUE)));
	}

	@Test
	public void constructorsValidate()
	{
		expectInvalid(() -> new EffectActive(null, true));
		expectInvalid(() -> new StatBoost(Skill.ATTACK, Comparison.AT_LEAST, 100));
		expectInvalid(() -> new StatBoost(null, Comparison.AT_LEAST, 1));
		expectInvalid(() -> new VitalBelow(Vital.HITPOINTS, 0));
		expectInvalid(() -> new VitalBelow(Vital.HITPOINTS, 101));
		expectInvalid(() -> new InArea(null));
		expectInvalid(() -> new CombatSpellWithin(0));
		expectInvalid(() -> new CombatSpellWithin(CombatSpellWithin.MAX_SECONDS + 1));
		expectInvalid(() -> new ClickedWithin(ClickedWithin.MAX_SECONDS + 1));
		expectInvalid(() -> new ItemValueAtLeast(0));
		expectInvalid(() -> new ItemValueAtLeast(ItemValueAtLeast.MAX_GP + 1));
		expectInvalid(() -> new AccountIs(null));
	}

	private static void expectInvalid(Runnable r)
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
