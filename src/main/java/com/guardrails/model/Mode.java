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
 * How a guard is enforced. Every mode leaves the guarded option reachable from the right-click
 * menu; none removes, hides or consumes anything. Precedents are recorded in
 * {@code docs/RULES-COMPLIANCE.md} and at each enforcement site.
 */
@Getter
@RequiredArgsConstructor
public enum Mode
{
	/** Left-click opens the menu instead of performing the top option. */
	RIGHT_CLICK_ONLY("Right-click only"),
	/** The guarded option is sorted below Walk here / Use, so it is never the left click. */
	DEPRIORITISE("Deprioritise"),
	/** Right-click only, unless the chosen modifier key is held. */
	MODIFIER("Unless modifier held"),
	/** A client-side placeholder takes the left click until a press-and-hold arms the option. */
	HOLD_TO_CONFIRM("Hold to confirm");

	private final String displayName;

	@Override
	public String toString()
	{
		return displayName;
	}
}
