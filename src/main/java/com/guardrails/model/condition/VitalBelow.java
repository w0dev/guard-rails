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

/** Holds while current hitpoints or prayer is below a percentage of the real level. */
@Value
public class VitalBelow implements Condition
{
	Vital vital;
	int percent;

	public VitalBelow(Vital vital, int percent)
	{
		this.vital = Conditions.required(vital, "hitpoints or prayer");
		this.percent = Conditions.inRange(percent, 1, 100, "Percent");
	}

	@Override
	public ConditionType getType()
	{
		return ConditionType.VITAL_BELOW;
	}

	@Override
	public boolean holds(GameContext context, Click click)
	{
		Skill skill = vital == Vital.HITPOINTS ? Skill.HITPOINTS : Skill.PRAYER;
		int max = context.getRealLevel(skill);
		return max > 0 && context.getBoostedLevel(skill) * 100 < percent * max;
	}

	@Override
	public String describe()
	{
		return vital.getDisplayName() + " below " + percent + "%";
	}
}
