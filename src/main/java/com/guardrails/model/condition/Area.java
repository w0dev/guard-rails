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

/** Curated named areas. Region and WorldArea definitions live with the game context. */
@Getter
@RequiredArgsConstructor
public enum Area
{
	GRAND_EXCHANGE("Grand Exchange"),
	WILDERNESS("Wilderness"),
	TZHAAR_FIGHT_CAVES_ENTRANCE("Fight Caves entrance"),
	INFERNO_ENTRANCE("Inferno entrance"),
	NIGHTMARE_ZONE_LOBBY("Nightmare Zone lobby"),
	CHAMBERS_OF_XERIC_LOBBY("Chambers of Xeric lobby"),
	THEATRE_OF_BLOOD_LOBBY("Theatre of Blood lobby"),
	TOMBS_OF_AMASCUT_LOBBY("Tombs of Amascut lobby"),
	FORTIS_COLOSSEUM_LOBBY("Fortis Colosseum lobby");

	private final String displayName;

	@Override
	public String toString()
	{
		return displayName;
	}
}
