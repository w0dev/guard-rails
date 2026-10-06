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
package com.guardrails.codec;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.guardrails.model.InvalidRuleException;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Reads one JSON object field by field and, in {@link #finish()}, rejects any field that was not
 * read. This is what makes the schema closed: an imported code cannot smuggle in an extra field
 * (such as an item or NPC ID) that some later version might honour.
 */
final class StrictObject
{
	private final JsonObject object;
	private final String where;
	private final Set<String> read = new HashSet<>();

	private StrictObject(JsonObject object, String where)
	{
		this.object = object;
		this.where = where;
	}

	static StrictObject of(JsonElement element, String where)
	{
		if (element == null || !element.isJsonObject())
		{
			throw new InvalidRuleException("Expected an object for " + where);
		}
		return new StrictObject(element.getAsJsonObject(), where);
	}

	boolean has(String key)
	{
		return object.has(key);
	}

	String string(String key)
	{
		return primitive(key, true).getAsString();
	}

	boolean bool(String key, boolean fallback)
	{
		JsonPrimitive p = optional(key);
		if (p == null)
		{
			return fallback;
		}
		if (!p.isBoolean())
		{
			throw invalid(key, "must be true or false");
		}
		return p.getAsBoolean();
	}

	int integer(String key, int fallback)
	{
		return has(key) ? (int) whole(key, Integer.MIN_VALUE, Integer.MAX_VALUE) : fallback;
	}

	int integer(String key)
	{
		return (int) whole(key, Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	long longValue(String key)
	{
		return whole(key, Long.MIN_VALUE, Long.MAX_VALUE);
	}

	/** Accepts only the exact name of one of {@code values}. */
	<E extends Enum<E>> E enumValue(String key, E[] values)
	{
		return lookup(key, string(key), values);
	}

	<E extends Enum<E>> E enumValue(String key, E[] values, E fallback)
	{
		return has(key) ? enumValue(key, values) : fallback;
	}

	JsonArray array(String key)
	{
		read.add(key);
		JsonElement e = object.get(key);
		if (e == null || !e.isJsonArray())
		{
			throw invalid(key, "must be a list");
		}
		return e.getAsJsonArray();
	}

	StrictObject object(String key)
	{
		read.add(key);
		if (!has(key))
		{
			throw new InvalidRuleException("Missing '" + key + "' in " + where);
		}
		return of(object.get(key), key);
	}

	<E extends Enum<E>> E enumElement(String key, JsonElement element, E[] values)
	{
		if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString())
		{
			throw invalid(key, "must be a list of names");
		}
		return lookup(key, element.getAsString(), values);
	}

	void finish()
	{
		for (Map.Entry<String, JsonElement> e : object.entrySet())
		{
			if (!read.contains(e.getKey()))
			{
				throw new InvalidRuleException("Unknown field '" + clip(e.getKey()) + "' in " + where);
			}
		}
	}

	private long whole(String key, long min, long max)
	{
		JsonPrimitive p = primitive(key, false);
		if (!p.isNumber())
		{
			throw invalid(key, "must be a number");
		}
		BigDecimal value = p.getAsBigDecimal();
		if (value.signum() != 0 && value.stripTrailingZeros().scale() > 0)
		{
			throw invalid(key, "must be a whole number");
		}
		if (value.compareTo(BigDecimal.valueOf(min)) < 0 || value.compareTo(BigDecimal.valueOf(max)) > 0)
		{
			throw invalid(key, "is out of range");
		}
		return value.longValueExact();
	}

	private <E extends Enum<E>> E lookup(String key, String name, E[] values)
	{
		for (E v : values)
		{
			if (v.name().equals(name))
			{
				return v;
			}
		}
		throw invalid(key, "has unknown value '" + clip(name) + "'");
	}

	private JsonPrimitive primitive(String key, boolean mustBeString)
	{
		JsonPrimitive p = optional(key);
		if (p == null)
		{
			throw new InvalidRuleException("Missing '" + key + "' in " + where);
		}
		if (mustBeString && !p.isString())
		{
			throw invalid(key, "must be text");
		}
		return p;
	}

	private JsonPrimitive optional(String key)
	{
		read.add(key);
		JsonElement e = object.get(key);
		if (e == null)
		{
			return null;
		}
		if (!e.isJsonPrimitive())
		{
			throw invalid(key, "has the wrong type");
		}
		return e.getAsJsonPrimitive();
	}

	private InvalidRuleException invalid(String key, String problem)
	{
		return new InvalidRuleException("'" + key + "' in " + where + " " + problem);
	}

	private static String clip(String s)
	{
		return s.length() > 32 ? s.substring(0, 32) + "…" : s;
	}
}
