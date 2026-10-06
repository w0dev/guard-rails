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
package com.guardrails.model.condition;

import lombok.Value;
import net.runelite.api.Skill;

/** Compares (boosted level − real level) of a skill against an amount; negative means drained. */
@Value
public class StatBoost implements Condition
{
	public static final int MAX_AMOUNT = 99;

	Skill skill;
	Comparison comparison;
	int amount;

	public StatBoost(Skill skill, Comparison comparison, int amount)
	{
		this.skill = Conditions.required(skill, "skill");
		this.comparison = Conditions.required(comparison, "comparison");
		this.amount = Conditions.inRange(amount, -MAX_AMOUNT, MAX_AMOUNT, "Boost");
	}

	@Override
	public ConditionType getType()
	{
		return ConditionType.STAT_BOOST;
	}

	@Override
	public boolean holds(GameContext context, Click click)
	{
		int boost = context.getBoostedLevel(skill) - context.getRealLevel(skill);
		return comparison == Comparison.AT_LEAST ? boost >= amount : boost < amount;
	}

	@Override
	public String describe()
	{
		return skill.getName() + " boost " + comparison.getDisplayName() + " " + amount;
	}
}
