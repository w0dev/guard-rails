package com.guardrails.model;

import com.guardrails.model.condition.AccountType;
import com.guardrails.model.condition.Area;
import com.guardrails.model.condition.Effect;
import com.guardrails.model.condition.GameContext;
import com.guardrails.model.condition.GameInterface;
import com.guardrails.model.condition.WorldKind;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import net.runelite.api.Skill;

/** Mutable test double for {@link GameContext}. */
public class FakeGameContext implements GameContext
{
	public final Set<Effect> effects = EnumSet.noneOf(Effect.class);
	public final Map<Skill, Integer> real = new EnumMap<>(Skill.class);
	public final Map<Skill, Integer> boosted = new EnumMap<>(Skill.class);
	public final Set<Area> areas = EnumSet.noneOf(Area.class);
	public final Set<GameInterface> interfaces = EnumSet.noneOf(GameInterface.class);
	public final Set<WorldKind> worlds = EnumSet.noneOf(WorldKind.class);
	public final Set<ItemGroup> inventory = EnumSet.noneOf(ItemGroup.class);
	public final Set<ItemGroup> equipment = EnumSet.noneOf(ItemGroup.class);
	public AccountType accountType = AccountType.NORMAL;
	public boolean skulled;
	public long millisSinceCombatSpell = Long.MAX_VALUE;

	public FakeGameContext level(Skill skill, int realLevel, int boostedLevel)
	{
		real.put(skill, realLevel);
		boosted.put(skill, boostedLevel);
		return this;
	}

	@Override
	public boolean isEffectActive(Effect effect)
	{
		return effects.contains(effect);
	}

	@Override
	public int getRealLevel(Skill skill)
	{
		return real.getOrDefault(skill, 1);
	}

	@Override
	public int getBoostedLevel(Skill skill)
	{
		return boosted.getOrDefault(skill, getRealLevel(skill));
	}

	@Override
	public boolean isInArea(Area area)
	{
		return areas.contains(area);
	}

	@Override
	public boolean isInterfaceOpen(GameInterface gameInterface)
	{
		return interfaces.contains(gameInterface);
	}

	@Override
	public boolean isOnWorld(WorldKind worldKind)
	{
		return worlds.contains(worldKind);
	}

	@Override
	public AccountType getAccountType()
	{
		return accountType;
	}

	@Override
	public boolean inventoryContains(ItemGroup group)
	{
		return inventory.contains(group);
	}

	@Override
	public boolean equipmentContains(ItemGroup group)
	{
		return equipment.contains(group);
	}

	@Override
	public boolean isSkulled()
	{
		return skulled;
	}

	@Override
	public long getMillisSinceCombatSpell()
	{
		return millisSinceCombatSpell;
	}
}
