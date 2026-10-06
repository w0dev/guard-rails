/*
 * Copyright (c) 2026, Blake Smreker
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.guardrails.codec;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.guardrails.model.GuardedAction;
import com.guardrails.model.InvalidRuleException;
import com.guardrails.model.ItemGroup;
import com.guardrails.model.Mode;
import com.guardrails.model.Modifier;
import com.guardrails.model.ObjectGroup;
import com.guardrails.model.Rule;
import com.guardrails.model.Target;
import com.guardrails.model.TeleportSpell;
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
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.runelite.api.Skill;

/**
 * Explicit, hand-written mapping between rules and the Gson tree model. No reflection-based
 * binding: every field that can appear in a rule's JSON is named in this file, and anything else
 * is rejected by {@link StrictObject#finish()}.
 *
 * <pre>
 * {"schemaVersion":1,"rules":[{"name":"Repot guard",
 *   "target":{"action":"DRINK","items":"DIVINE_SUPER_COMBAT"},
 *   "when":[{"type":"EFFECT_ACTIVE","effect":"DIVINE_SUPER_COMBAT"}],
 *   "mode":"HOLD_TO_CONFIRM"}]}
 * </pre>
 */
public final class RuleJson
{
	public static final int SCHEMA_VERSION = 1;
	public static final int MAX_RULES = 50;

	private RuleJson()
	{
	}

	public static JsonObject toJson(List<Rule> rules)
	{
		JsonArray array = new JsonArray();
		rules.forEach(r -> array.add(toJson(r)));
		JsonObject root = new JsonObject();
		root.addProperty("schemaVersion", SCHEMA_VERSION);
		root.add("rules", array);
		return root;
	}

	public static List<Rule> fromJson(JsonElement element)
	{
		StrictObject root = StrictObject.of(element, "code");
		int version = root.integer("schemaVersion");
		if (version != SCHEMA_VERSION)
		{
			throw new InvalidRuleException("Unsupported schema version " + version);
		}
		JsonArray array = root.array("rules");
		root.finish();
		if (array.size() == 0 || array.size() > MAX_RULES)
		{
			throw new InvalidRuleException("A code holds 1–" + MAX_RULES + " rules");
		}
		List<Rule> rules = new ArrayList<>(array.size());
		for (JsonElement e : array)
		{
			rules.add(ruleFromJson(e));
		}
		return rules;
	}

	static JsonObject toJson(Rule rule)
	{
		JsonObject o = new JsonObject();
		o.addProperty("name", rule.getName());
		if (!rule.isEnabled())
		{
			o.addProperty("enabled", false);
		}
		o.add("target", toJson(rule.getTarget()));
		if (!rule.getConditions().isEmpty())
		{
			JsonArray when = new JsonArray();
			rule.getConditions().forEach(c -> when.add(toJson(c)));
			o.add("when", when);
		}
		o.addProperty("mode", rule.getMode().name());
		if (rule.getModifier() != Modifier.SHIFT)
		{
			o.addProperty("modifier", rule.getModifier().name());
		}
		if (rule.getHoldMillis() != Rule.DEFAULT_HOLD_MILLIS)
		{
			o.addProperty("holdMillis", rule.getHoldMillis());
		}
		if (rule.isAllowInWilderness())
		{
			o.addProperty("allowInWilderness", true);
		}
		return o;
	}

	static Rule ruleFromJson(JsonElement element)
	{
		StrictObject o = StrictObject.of(element, "rule");
		Rule.Builder b = Rule.builder()
			.name(o.string("name"))
			.enabled(o.bool("enabled", true))
			.target(targetFromJson(o))
			.mode(o.enumValue("mode", Mode.values()))
			.modifier(o.enumValue("modifier", Modifier.values(), Modifier.SHIFT))
			.holdMillis(o.integer("holdMillis", Rule.DEFAULT_HOLD_MILLIS))
			.allowInWilderness(o.bool("allowInWilderness", false));
		if (o.has("when"))
		{
			JsonArray when = o.array("when");
			if (when.size() > Rule.MAX_CONDITIONS)
			{
				throw new InvalidRuleException("At most " + Rule.MAX_CONDITIONS + " conditions per rule");
			}
			when.forEach(c -> b.condition(conditionFromJson(c)));
		}
		o.finish();
		return b.build();
	}

	private static JsonObject toJson(Target target)
	{
		JsonObject o = new JsonObject();
		o.addProperty("action", target.getAction().name());
		switch (target.getKind())
		{
			case ITEM:
				o.addProperty("items", target.getItems().name());
				break;
			case OBJECT:
				o.addProperty("objects", target.getObjects().name());
				break;
			case SPELL:
				o.addProperty("spell", target.getSpell().name());
				break;
		}
		return o;
	}

	private static Target targetFromJson(StrictObject rule)
	{
		StrictObject o = rule.object("target");
		GuardedAction action = o.enumValue("action", GuardedAction.values());
		Target target;
		switch (action.getKind())
		{
			case ITEM:
				target = Target.item(action, o.enumValue("items", ItemGroup.values()));
				break;
			case OBJECT:
				target = Target.object(action, o.enumValue("objects", ObjectGroup.values()));
				break;
			default:
				target = Target.spell(o.enumValue("spell", TeleportSpell.values()));
				break;
		}
		o.finish();
		return target;
	}

	static JsonObject toJson(Condition condition)
	{
		JsonObject o = new JsonObject();
		o.addProperty("type", condition.getType().name());
		switch (condition.getType())
		{
			case EFFECT_ACTIVE:
			{
				EffectActive c = (EffectActive) condition;
				o.addProperty("effect", c.getEffect().name());
				if (!c.isActive())
				{
					o.addProperty("active", false);
				}
				break;
			}
			case STAT_BOOST:
			{
				StatBoost c = (StatBoost) condition;
				o.addProperty("skill", c.getSkill().name());
				o.addProperty("comparison", c.getComparison().name());
				o.addProperty("amount", c.getAmount());
				break;
			}
			case VITAL_BELOW:
			{
				VitalBelow c = (VitalBelow) condition;
				o.addProperty("vital", c.getVital().name());
				o.addProperty("percent", c.getPercent());
				break;
			}
			case IN_AREA:
				o.addProperty("area", ((InArea) condition).getArea().name());
				break;
			case INTERFACE_OPEN:
				o.addProperty("interface", ((InterfaceOpen) condition).getGameInterface().name());
				break;
			case WORLD_TYPE:
				o.addProperty("world", ((OnWorld) condition).getWorld().name());
				break;
			case ACCOUNT_TYPE:
			{
				JsonArray types = new JsonArray();
				((AccountIs) condition).getAccountTypes().forEach(t -> types.add(t.name()));
				o.add("accounts", types);
				break;
			}
			case INVENTORY_CONTAINS:
			{
				InventoryContains c = (InventoryContains) condition;
				o.addProperty("group", c.getGroup().name());
				if (!c.isPresent())
				{
					o.addProperty("present", false);
				}
				break;
			}
			case EQUIPMENT_CONTAINS:
			{
				EquipmentContains c = (EquipmentContains) condition;
				o.addProperty("group", c.getGroup().name());
				if (!c.isPresent())
				{
					o.addProperty("present", false);
				}
				break;
			}
			case SKULLED:
				if (!((Skulled) condition).isSkulled())
				{
					o.addProperty("skulled", false);
				}
				break;
			case COMBAT_SPELL_WITHIN:
				o.addProperty("seconds", ((CombatSpellWithin) condition).getSeconds());
				break;
			case CLICKED_WITHIN:
				o.addProperty("seconds", ((ClickedWithin) condition).getSeconds());
				break;
			case ITEM_VALUE_AT_LEAST:
				o.addProperty("gp", ((ItemValueAtLeast) condition).getGp());
				break;
		}
		return o;
	}

	static Condition conditionFromJson(JsonElement element)
	{
		StrictObject o = StrictObject.of(element, "condition");
		ConditionType type = o.enumValue("type", ConditionType.values());
		Condition c;
		switch (type)
		{
			case EFFECT_ACTIVE:
				c = new EffectActive(o.enumValue("effect", Effect.values()), o.bool("active", true));
				break;
			case STAT_BOOST:
				c = new StatBoost(o.enumValue("skill", Skill.values()),
					o.enumValue("comparison", Comparison.values()), o.integer("amount"));
				break;
			case VITAL_BELOW:
				c = new VitalBelow(o.enumValue("vital", Vital.values()), o.integer("percent"));
				break;
			case IN_AREA:
				c = new InArea(o.enumValue("area", Area.values()));
				break;
			case INTERFACE_OPEN:
				c = new InterfaceOpen(o.enumValue("interface", GameInterface.values()));
				break;
			case WORLD_TYPE:
				c = new OnWorld(o.enumValue("world", WorldKind.values()));
				break;
			case ACCOUNT_TYPE:
			{
				Set<AccountType> types = EnumSet.noneOf(AccountType.class);
				for (JsonElement t : o.array("accounts"))
				{
					types.add(o.enumElement("accounts", t, AccountType.values()));
				}
				c = new AccountIs(types);
				break;
			}
			case INVENTORY_CONTAINS:
				c = new InventoryContains(o.enumValue("group", ItemGroup.values()), o.bool("present", true));
				break;
			case EQUIPMENT_CONTAINS:
				c = new EquipmentContains(o.enumValue("group", ItemGroup.values()), o.bool("present", true));
				break;
			case SKULLED:
				c = new Skulled(o.bool("skulled", true));
				break;
			case COMBAT_SPELL_WITHIN:
				c = new CombatSpellWithin(o.integer("seconds"));
				break;
			case CLICKED_WITHIN:
				c = new ClickedWithin(o.integer("seconds"));
				break;
			case ITEM_VALUE_AT_LEAST:
				c = new ItemValueAtLeast(o.longValue("gp"));
				break;
			default:
				throw new InvalidRuleException("Unsupported condition " + type);
		}
		o.finish();
		return c;
	}
}
