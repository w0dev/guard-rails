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
package com.guardrails.model;

import java.util.Objects;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * What a rule guards: one {@link GuardedAction} on one curated group. Construct with
 * {@link #item}, {@link #object} or {@link #spell}; each rejects an action of the wrong kind,
 * so e.g. "Enter" on an item group cannot exist.
 */
@Getter
@EqualsAndHashCode
public final class Target
{
	private final GuardedAction action;
	private final ItemGroup items;
	private final ObjectGroup objects;
	private final TeleportSpell spell;

	private Target(GuardedAction action, ItemGroup items, ObjectGroup objects, TeleportSpell spell)
	{
		this.action = action;
		this.items = items;
		this.objects = objects;
		this.spell = spell;
	}

	public static Target item(GuardedAction action, ItemGroup items)
	{
		requireKind(action, TargetKind.ITEM);
		return new Target(action, require(items, "item group"), null, null);
	}

	public static Target object(GuardedAction action, ObjectGroup objects)
	{
		requireKind(action, TargetKind.OBJECT);
		return new Target(action, null, require(objects, "object group"), null);
	}

	public static Target spell(TeleportSpell spell)
	{
		return new Target(GuardedAction.CAST_TELEPORT, null, null, require(spell, "spell"));
	}

	public TargetKind getKind()
	{
		return action.getKind();
	}

	/** e.g. "Drink · Divine super combat potions". */
	public String describe()
	{
		switch (getKind())
		{
			case ITEM:
				return action.getDisplayName() + " · " + items.getDisplayName();
			case OBJECT:
				return action.getDisplayName() + " · " + objects.getDisplayName();
			default:
				return action.getDisplayName() + " · " + spell.getDisplayName();
		}
	}

	@Override
	public String toString()
	{
		return describe();
	}

	private static void requireKind(GuardedAction action, TargetKind kind)
	{
		require(action, "action");
		if (action.getKind() != kind)
		{
			throw new InvalidRuleException(action.getDisplayName() + " cannot target " + kind.name().toLowerCase());
		}
	}

	private static <T> T require(T value, String what)
	{
		if (value == null)
		{
			throw new InvalidRuleException("Missing " + what);
		}
		return Objects.requireNonNull(value);
	}
}
