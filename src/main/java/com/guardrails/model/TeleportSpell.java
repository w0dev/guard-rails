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
 * Teleport spells a rule may guard, named as they appear in the spellbook.
 * {@link #ANY_TELEPORT} matches every constant below it.
 */
@Getter
@RequiredArgsConstructor
public enum TeleportSpell
{
	ANY_TELEPORT("Any teleport spell"),

	// Standard
	LUMBRIDGE_HOME_TELEPORT("Lumbridge Home Teleport"),
	VARROCK_TELEPORT("Varrock Teleport"),
	LUMBRIDGE_TELEPORT("Lumbridge Teleport"),
	FALADOR_TELEPORT("Falador Teleport"),
	CAMELOT_TELEPORT("Camelot Teleport"),
	KOUREND_CASTLE_TELEPORT("Kourend Castle Teleport"),
	ARDOUGNE_TELEPORT("Ardougne Teleport"),
	CIVITAS_ILLA_FORTIS_TELEPORT("Civitas illa Fortis Teleport"),
	WATCHTOWER_TELEPORT("Watchtower Teleport"),
	TROLLHEIM_TELEPORT("Trollheim Teleport"),
	APE_ATOLL_TELEPORT("Ape Atoll Teleport"),

	// Ancient
	EDGEVILLE_HOME_TELEPORT("Edgeville Home Teleport"),
	PADDEWWA_TELEPORT("Paddewwa Teleport"),
	SENNTISTEN_TELEPORT("Senntisten Teleport"),
	KHARYRLL_TELEPORT("Kharyrll Teleport"),
	LASSAR_TELEPORT("Lassar Teleport"),
	DAREEYAK_TELEPORT("Dareeyak Teleport"),
	CARRALLANGAR_TELEPORT("Carrallangar Teleport"),
	ANNAKARL_TELEPORT("Annakarl Teleport"),
	GHORROCK_TELEPORT("Ghorrock Teleport"),

	// Lunar
	LUNAR_HOME_TELEPORT("Lunar Home Teleport"),
	MOONCLAN_TELEPORT("Moonclan Teleport"),
	OURANIA_TELEPORT("Ourania Teleport"),
	WATERBIRTH_TELEPORT("Waterbirth Teleport"),
	BARBARIAN_TELEPORT("Barbarian Teleport"),
	KHAZARD_TELEPORT("Khazard Teleport"),
	FISHING_GUILD_TELEPORT("Fishing Guild Teleport"),
	CATHERBY_TELEPORT("Catherby Teleport"),
	ICE_PLATEAU_TELEPORT("Ice Plateau Teleport"),

	// Arceuus
	ARCEUUS_HOME_TELEPORT("Arceuus Home Teleport"),
	ARCEUUS_LIBRARY_TELEPORT("Arceuus Library Teleport"),
	DRAYNOR_MANOR_TELEPORT("Draynor Manor Teleport"),
	BATTLEFRONT_TELEPORT("Battlefront Teleport"),
	MIND_ALTAR_TELEPORT("Mind Altar Teleport"),
	SALVE_GRAVEYARD_TELEPORT("Salve Graveyard Teleport"),
	FENKENSTRAINS_CASTLE_TELEPORT("Fenkenstrain's Castle Teleport"),
	WEST_ARDOUGNE_TELEPORT("West Ardougne Teleport"),
	HARMONY_ISLAND_TELEPORT("Harmony Island Teleport"),
	CEMETERY_TELEPORT("Cemetery Teleport"),
	BARROWS_TELEPORT("Barrows Teleport"),
	APE_ATOLL_TELEPORT_ARCEUUS("Ape Atoll Teleport (Arceuus)");

	private final String displayName;

	@Override
	public String toString()
	{
		return displayName;
	}
}
