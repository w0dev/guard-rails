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

import com.guardrails.model.InvalidRuleException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Value;

/** Holds when the account type is any of the given types. */
@Value
public class AccountIs implements Condition
{
	Set<AccountType> accountTypes;

	public AccountIs(Set<AccountType> accountTypes)
	{
		Conditions.required(accountTypes, "account types");
		if (accountTypes.isEmpty() || accountTypes.contains(null))
		{
			throw new InvalidRuleException("Pick at least one account type");
		}
		this.accountTypes = Collections.unmodifiableSet(EnumSet.copyOf(accountTypes));
	}

	@Override
	public ConditionType getType()
	{
		return ConditionType.ACCOUNT_TYPE;
	}

	@Override
	public boolean holds(GameContext context, Click click)
	{
		return accountTypes.contains(context.getAccountType());
	}

	@Override
	public String describe()
	{
		return "Account is " + accountTypes.stream().map(AccountType::getDisplayName).collect(Collectors.joining(" or "));
	}
}
