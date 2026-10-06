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

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import lombok.Getter;
import net.runelite.api.MenuAction;

/**
 * The closed set of options a rule may guard. This enum <em>is</em> the allow-list.
 *
 * <p>There is deliberately no constant for Attack, any cast on an NPC or player, Trade, Follow,
 * Report, any other player option, Pickpocket, Knock-Out, Lure or Walk here. A rule can only be
 * built from a constant of this enum, and the share-code parser only accepts these constant
 * names, so those options cannot be referenced from the UI, a recipe or an imported code.
 *
 * <p>Each constant also fixes the menu entry types it may ever match. None of them may include a
 * type from {@link #NEVER_GUARDED}; {@code GuardedActionTest} asserts this for every constant.
 */
@Getter
public enum GuardedAction
{
	EAT(TargetKind.ITEM, "Eat", Types.ITEM_OP),
	DRINK(TargetKind.ITEM, "Drink", Types.ITEM_OP),
	DROP(TargetKind.ITEM, "Drop", Types.ITEM_OP),
	DESTROY(TargetKind.ITEM, "Destroy", Types.ITEM_OP),
	WIELD(TargetKind.ITEM, "Wield", Types.ITEM_OP),
	WEAR(TargetKind.ITEM, "Wear", Types.ITEM_OP),
	USE(TargetKind.ITEM, "Use", Types.ITEM_USE),
	HIGH_ALCHEMY(TargetKind.ITEM, "High Level Alchemy", Types.SPELL_ON_ITEM),
	LOW_ALCHEMY(TargetKind.ITEM, "Low Level Alchemy", Types.SPELL_ON_ITEM),
	DEPOSIT(TargetKind.ITEM, "Deposit", Types.ITEM_OP),
	WITHDRAW(TargetKind.ITEM, "Withdraw", Types.ITEM_OP),

	ENTER(TargetKind.OBJECT, "Enter", Types.OBJECT_OP),
	CLIMB(TargetKind.OBJECT, "Climb", Types.OBJECT_OP),
	PULL(TargetKind.OBJECT, "Pull", Types.OBJECT_OP),
	TELEPORT(TargetKind.OBJECT, "Teleport", Types.OBJECT_OP),
	BOARD(TargetKind.OBJECT, "Board", Types.OBJECT_OP),
	JUMP_IN(TargetKind.OBJECT, "Jump-in", Types.OBJECT_OP),

	CAST_TELEPORT(TargetKind.SPELL, "Cast teleport", Types.SPELLBOOK_OP);

	/**
	 * Menu entry types that no guarded action may match: every NPC and player interaction
	 * (including spells and items used on them) and walking. RuneLite deprecated the old
	 * ITEM_USE_ON_NPC/PLAYER types in favour of WIDGET_TARGET_ON_NPC/PLAYER, which are listed.
	 */
	public static final Set<MenuAction> NEVER_GUARDED = Collections.unmodifiableSet(EnumSet.of(
		MenuAction.NPC_FIRST_OPTION, MenuAction.NPC_SECOND_OPTION, MenuAction.NPC_THIRD_OPTION,
		MenuAction.NPC_FOURTH_OPTION, MenuAction.NPC_FIFTH_OPTION,
		MenuAction.WIDGET_TARGET_ON_NPC,
		MenuAction.PLAYER_FIRST_OPTION, MenuAction.PLAYER_SECOND_OPTION, MenuAction.PLAYER_THIRD_OPTION,
		MenuAction.PLAYER_FOURTH_OPTION, MenuAction.PLAYER_FIFTH_OPTION, MenuAction.PLAYER_SIXTH_OPTION,
		MenuAction.PLAYER_SEVENTH_OPTION, MenuAction.PLAYER_EIGHTH_OPTION,
		MenuAction.WIDGET_TARGET_ON_PLAYER, MenuAction.RUNELITE_PLAYER,
		MenuAction.WALK, MenuAction.SET_HEADING));

	private final TargetKind kind;
	private final String displayName;
	private final Set<MenuAction> menuTypes;

	GuardedAction(TargetKind kind, String displayName, Set<MenuAction> menuTypes)
	{
		this.kind = kind;
		this.displayName = displayName;
		this.menuTypes = menuTypes;
	}

	@Override
	public String toString()
	{
		return displayName;
	}

	private static final class Types
	{
		/** Inventory, bank and equipment item options. */
		static final Set<MenuAction> ITEM_OP = immutable(MenuAction.CC_OP, MenuAction.CC_OP_LOW_PRIORITY);
		/** "Use" selects the item; it may surface as an op or a widget-target selection. */
		static final Set<MenuAction> ITEM_USE = immutable(MenuAction.CC_OP, MenuAction.CC_OP_LOW_PRIORITY,
			MenuAction.WIDGET_TARGET);
		/** A selected spell cast on one of the player's own items (alchemy). */
		static final Set<MenuAction> SPELL_ON_ITEM = immutable(MenuAction.WIDGET_TARGET_ON_WIDGET);
		static final Set<MenuAction> OBJECT_OP = immutable(MenuAction.GAME_OBJECT_FIRST_OPTION,
			MenuAction.GAME_OBJECT_SECOND_OPTION, MenuAction.GAME_OBJECT_THIRD_OPTION,
			MenuAction.GAME_OBJECT_FOURTH_OPTION, MenuAction.GAME_OBJECT_FIFTH_OPTION);
		/** Clicking a spell in the spellbook. */
		static final Set<MenuAction> SPELLBOOK_OP = immutable(MenuAction.CC_OP);

		private static Set<MenuAction> immutable(MenuAction first, MenuAction... rest)
		{
			return Collections.unmodifiableSet(EnumSet.of(first, rest));
		}
	}
}
