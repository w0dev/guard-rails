package com.guardrails.model;

import com.guardrails.model.condition.AccountIs;
import com.guardrails.model.condition.AccountType;
import com.guardrails.model.condition.Area;
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
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import net.runelite.api.Skill;

/** One non-default sample per condition type, so exhaustive tests can iterate them. */
public final class Samples
{
	private Samples()
	{
	}

	public static Map<ConditionType, Condition> conditions()
	{
		Map<ConditionType, Condition> m = new EnumMap<>(ConditionType.class);
		m.put(ConditionType.EFFECT_ACTIVE, new EffectActive(Effect.DIVINE_SUPER_COMBAT, false));
		m.put(ConditionType.STAT_BOOST, new StatBoost(Skill.STRENGTH, Comparison.BELOW, -3));
		m.put(ConditionType.VITAL_BELOW, new VitalBelow(Vital.PRAYER, 25));
		m.put(ConditionType.IN_AREA, new InArea(Area.GRAND_EXCHANGE));
		m.put(ConditionType.INTERFACE_OPEN, new InterfaceOpen(GameInterface.BANK));
		m.put(ConditionType.WORLD_TYPE, new OnWorld(WorldKind.HARDCORE));
		m.put(ConditionType.ACCOUNT_TYPE, new AccountIs(EnumSet.of(AccountType.HARDCORE_IRONMAN,
			AccountType.HARDCORE_GROUP_IRONMAN)));
		m.put(ConditionType.INVENTORY_CONTAINS, new InventoryContains(ItemGroup.STAMINA, false));
		m.put(ConditionType.EQUIPMENT_CONTAINS, new EquipmentContains(ItemGroup.VALUABLE_CAPES, true));
		m.put(ConditionType.SKULLED, new Skulled(false));
		m.put(ConditionType.COMBAT_SPELL_WITHIN, new CombatSpellWithin(12));
		m.put(ConditionType.CLICKED_WITHIN, new ClickedWithin(3));
		m.put(ConditionType.ITEM_VALUE_AT_LEAST, new ItemValueAtLeast(25_000));
		return m;
	}

	public static Rule repotGuard()
	{
		return Rule.builder()
			.name("Repot guard")
			.target(Target.item(GuardedAction.DRINK, ItemGroup.DIVINE_SUPER_COMBAT))
			.condition(new EffectActive(Effect.DIVINE_SUPER_COMBAT, true))
			.mode(Mode.HOLD_TO_CONFIRM)
			.build();
	}

	/** A rule using every field with a non-default value and every condition type. */
	public static Rule everything()
	{
		Rule.Builder b = Rule.builder()
			.name("Everything")
			.enabled(false)
			.target(Target.object(GuardedAction.ENTER, ObjectGroup.INFERNO_ENTRANCE))
			.mode(Mode.MODIFIER)
			.modifier(Modifier.CTRL)
			.holdMillis(1200);
		// Only MAX_CONDITIONS fit in one rule; codec tests round-trip every type individually.
		conditions().values().stream().limit(Rule.MAX_CONDITIONS).forEach(b::condition);
		return b.build();
	}
}
