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

/** Holds when the clicked item's unit value is at least the given gp. Unknown values never hold. */
@Value
public class ItemValueAtLeast implements Condition
{
	public static final long MAX_GP = Integer.MAX_VALUE;

	long gp;

	public ItemValueAtLeast(long gp)
	{
		this.gp = Conditions.inRange(gp, 1, MAX_GP, "Value");
	}

	@Override
	public ConditionType getType()
	{
		return ConditionType.ITEM_VALUE_AT_LEAST;
	}

	@Override
	public boolean holds(GameContext context, Click click)
	{
		return click.getItemValue() >= gp;
	}

	@Override
	public boolean dependsOnClick()
	{
		return true;
	}

	@Override
	public String describe()
	{
		return "Item worth at least " + String.format("%,d", gp) + " gp";
	}
}
