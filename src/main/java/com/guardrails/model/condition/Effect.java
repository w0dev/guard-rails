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

/** Timed effects a rule can test. Mapped to varbits/varps when the game context is built. */
@Getter
@RequiredArgsConstructor
public enum Effect
{
	DIVINE_SUPER_ATTACK("Divine super attack"),
	DIVINE_SUPER_STRENGTH("Divine super strength"),
	DIVINE_SUPER_DEFENCE("Divine super defence"),
	DIVINE_RANGING("Divine ranging"),
	DIVINE_MAGIC("Divine magic"),
	DIVINE_SUPER_COMBAT("Divine super combat"),
	DIVINE_BASTION("Divine bastion"),
	DIVINE_BATTLEMAGE("Divine battlemage"),
	PRAYER_REGENERATION("Prayer regeneration"),
	ANTIFIRE("Antifire"),
	SUPER_ANTIFIRE("Super antifire"),
	ANTIPOISON("Antipoison"),
	ANTIVENOM("Antivenom"),
	STAMINA("Stamina"),
	OVERLOAD("Overload"),
	GOADING("Goading"),
	SMELLING_SALTS("Smelling salts");

	private final String displayName;

	@Override
	public String toString()
	{
		return displayName;
	}
}
