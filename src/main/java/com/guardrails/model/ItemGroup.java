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

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Curated, named item groups. Users pick a group; they never enter item IDs. Membership is
 * defined in code from gameval {@code ItemID} constants (or, for {@link #UNTRADEABLE} and
 * {@link #ANY_ITEM}, a property of the item) when groups are resolved.
 */
@Getter
@RequiredArgsConstructor
public enum ItemGroup
{
	ANY_ITEM("Any item"),
	DIVINE_SUPER_COMBAT("Divine super combat potions"),
	DIVINE_SUPER_ATTACK("Divine super attack potions"),
	DIVINE_SUPER_STRENGTH("Divine super strength potions"),
	DIVINE_SUPER_DEFENCE("Divine super defence potions"),
	DIVINE_RANGING("Divine ranging potions"),
	DIVINE_MAGIC("Divine magic potions"),
	DIVINE_BASTION("Divine bastion potions"),
	DIVINE_BATTLEMAGE("Divine battlemage potions"),
	PRAYER_REGENERATION("Prayer regeneration potions"),
	ANTIFIRE("Antifire potions"),
	SUPER_ANTIFIRE("Super antifire potions"),
	ANTIVENOM("Antivenom potions"),
	STAMINA("Stamina potions"),
	OVERLOAD("Overloads"),
	GOADING("Goading potions"),
	SMELLING_SALTS("Smelling salts"),
	QUEST_FOODS("Quest foods"),
	UNTRADEABLE("Untradeable items"),
	VALUABLE_CAPES("Hard-earned capes");

	private final String displayName;

	@Override
	public String toString()
	{
		return displayName;
	}
}
