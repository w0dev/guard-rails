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

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Discriminator for condition JSON and the rule editor's condition dropdown. */
@Getter
@RequiredArgsConstructor
public enum ConditionType
{
	EFFECT_ACTIVE("Effect active"),
	STAT_BOOST("Stat boost"),
	VITAL_BELOW("Hitpoints/prayer below"),
	IN_AREA("In area"),
	INTERFACE_OPEN("Interface open"),
	WORLD_TYPE("World type"),
	ACCOUNT_TYPE("Account type"),
	INVENTORY_CONTAINS("Inventory contains"),
	EQUIPMENT_CONTAINS("Equipment contains"),
	SKULLED("Skulled"),
	COMBAT_SPELL_WITHIN("Combat spell cast recently"),
	CLICKED_WITHIN("Clicked recently"),
	ITEM_VALUE_AT_LEAST("Item value at least");

	private final String displayName;

	@Override
	public String toString()
	{
		return displayName;
	}
}
