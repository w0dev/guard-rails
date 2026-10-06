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

import com.guardrails.model.condition.Condition;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * WHEN every condition holds, GUARD the target, using MODE. Immutable and always valid: the only
 * way to obtain one is {@link Builder#build()}, which enforces every constraint below.
 */
@Getter
@ToString
@EqualsAndHashCode
public final class Rule
{
	public static final int MAX_NAME_LENGTH = 40;
	public static final int MAX_CONDITIONS = 8;
	public static final int MIN_HOLD_MILLIS = 200;
	public static final int MAX_HOLD_MILLIS = 1500;
	public static final int DEFAULT_HOLD_MILLIS = 600;

	private final String name;
	private final boolean enabled;
	private final Target target;
	private final List<Condition> conditions;
	private final Mode mode;
	private final Modifier modifier;
	private final int holdMillis;
	/**
	 * Keep this rule active in the Wilderness and on PvP worlds. Only allowed for actions on the
	 * player's own carried items; everything else is always off there.
	 */
	private final boolean allowInWilderness;

	private Rule(Builder b)
	{
		this.name = validName(b.name);
		this.enabled = b.enabled;
		if (b.target == null)
		{
			throw new InvalidRuleException("A rule needs a target");
		}
		this.target = b.target;
		if (b.conditions.size() > MAX_CONDITIONS)
		{
			throw new InvalidRuleException("At most " + MAX_CONDITIONS + " conditions per rule");
		}
		for (Condition c : b.conditions)
		{
			if (c == null)
			{
				throw new InvalidRuleException("Missing condition");
			}
		}
		this.conditions = Collections.unmodifiableList(new ArrayList<>(b.conditions));
		if (b.mode == null)
		{
			throw new InvalidRuleException("A rule needs a mode");
		}
		this.mode = b.mode;
		if (b.modifier == null)
		{
			throw new InvalidRuleException("Missing modifier key");
		}
		this.modifier = b.modifier;
		if (b.holdMillis < MIN_HOLD_MILLIS || b.holdMillis > MAX_HOLD_MILLIS)
		{
			throw new InvalidRuleException("Hold time must be " + MIN_HOLD_MILLIS + "–" + MAX_HOLD_MILLIS + " ms");
		}
		this.holdMillis = b.holdMillis;
		if (b.allowInWilderness && !isCarriedItemAction(b.target))
		{
			throw new InvalidRuleException("Only actions on your own carried items can stay on in the Wilderness");
		}
		this.allowInWilderness = b.allowInWilderness;
	}

	public static Builder builder()
	{
		return new Builder();
	}

	public Builder toBuilder()
	{
		Builder b = new Builder()
			.name(name)
			.enabled(enabled)
			.target(target)
			.mode(mode)
			.modifier(modifier)
			.holdMillis(holdMillis)
			.allowInWilderness(allowInWilderness);
		conditions.forEach(b::condition);
		return b;
	}

	private static boolean isCarriedItemAction(Target target)
	{
		// Withdraw acts on the bank, not on an item the player is carrying.
		return target.getKind() == TargetKind.ITEM && target.getAction() != GuardedAction.WITHDRAW;
	}

	private static String validName(String raw)
	{
		String name = raw == null ? "" : raw.trim();
		if (name.isEmpty())
		{
			throw new InvalidRuleException("A rule needs a name");
		}
		if (name.length() > MAX_NAME_LENGTH)
		{
			throw new InvalidRuleException("Rule names are at most " + MAX_NAME_LENGTH + " characters");
		}
		for (int i = 0; i < name.length(); i++)
		{
			char c = name.charAt(i);
			// Names are echoed into chat feedback; '<' '>' would be parsed as chat formatting tags.
			if (c == '<' || c == '>' || Character.isISOControl(c))
			{
				throw new InvalidRuleException("Rule names cannot contain < > or control characters");
			}
		}
		return name;
	}

	public static final class Builder
	{
		private String name;
		private boolean enabled = true;
		private Target target;
		private final List<Condition> conditions = new ArrayList<>();
		private Mode mode;
		private Modifier modifier = Modifier.SHIFT;
		private int holdMillis = DEFAULT_HOLD_MILLIS;
		private boolean allowInWilderness;

		private Builder()
		{
		}

		public Builder name(String name)
		{
			this.name = name;
			return this;
		}

		public Builder enabled(boolean enabled)
		{
			this.enabled = enabled;
			return this;
		}

		public Builder target(Target target)
		{
			this.target = target;
			return this;
		}

		public Builder condition(Condition condition)
		{
			this.conditions.add(condition);
			return this;
		}

		public Builder mode(Mode mode)
		{
			this.mode = mode;
			return this;
		}

		public Builder modifier(Modifier modifier)
		{
			this.modifier = modifier;
			return this;
		}

		public Builder holdMillis(int holdMillis)
		{
			this.holdMillis = holdMillis;
			return this;
		}

		public Builder allowInWilderness(boolean allowInWilderness)
		{
			this.allowInWilderness = allowInWilderness;
			return this;
		}

		public Rule build()
		{
			return new Rule(this);
		}
	}
}
