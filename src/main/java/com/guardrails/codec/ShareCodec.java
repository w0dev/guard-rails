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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.guardrails.model.InvalidRuleException;
import com.guardrails.model.Rule;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Rules ⇄ JSON text (for config storage) and rules ⇄ share codes ({@code GR1.} + base64url of
 * zlib-compressed JSON). Decoding treats the input as hostile: length, decompressed size and JSON
 * nesting depth are all capped before parsing, and the schema is closed (see {@link RuleJson}).
 */
@Singleton
public class ShareCodec
{
	public static final String PREFIX = "GR1.";
	static final int MAX_CODE_CHARS = 16 * 1024;
	static final int MAX_JSON_BYTES = 64 * 1024;
	static final int MAX_JSON_DEPTH = 8;

	private final Gson gson;

	@Inject
	public ShareCodec(Gson gson)
	{
		this.gson = gson;
	}

	public String toJson(List<Rule> rules)
	{
		return gson.toJson(RuleJson.toJson(rules));
	}

	public List<Rule> fromJson(String json)
	{
		if (json.length() > MAX_JSON_BYTES)
		{
			throw new InvalidRuleException("Rules are too large");
		}
		checkDepth(json);
		JsonElement element;
		try
		{
			element = gson.fromJson(json, JsonElement.class);
		}
		catch (RuntimeException e)
		{
			throw new InvalidRuleException("Not valid rule JSON");
		}
		return RuleJson.fromJson(element);
	}

	public String encode(List<Rule> rules)
	{
		byte[] json = toJson(rules).getBytes(StandardCharsets.UTF_8);
		Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
		try
		{
			deflater.setInput(json);
			deflater.finish();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buf = new byte[1024];
			while (!deflater.finished())
			{
				out.write(buf, 0, deflater.deflate(buf));
			}
			return PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(out.toByteArray());
		}
		finally
		{
			deflater.end();
		}
	}

	public List<Rule> decode(String code)
	{
		String compact = code == null ? "" : code.replaceAll("\\s+", "");
		if (!compact.startsWith(PREFIX))
		{
			throw new InvalidRuleException("Not a Guard Rails code (should start with " + PREFIX + ")");
		}
		if (compact.length() > MAX_CODE_CHARS)
		{
			throw new InvalidRuleException("Code is too long");
		}
		byte[] compressed;
		try
		{
			compressed = Base64.getUrlDecoder().decode(compact.substring(PREFIX.length()));
		}
		catch (IllegalArgumentException e)
		{
			throw new InvalidRuleException("Code is damaged (bad characters)");
		}
		return fromJson(new String(inflate(compressed), StandardCharsets.UTF_8));
	}

	private static byte[] inflate(byte[] compressed)
	{
		Inflater inflater = new Inflater();
		try
		{
			inflater.setInput(compressed);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buf = new byte[1024];
			while (!inflater.finished())
			{
				int n = inflater.inflate(buf);
				if (n == 0 && (inflater.needsInput() || inflater.needsDictionary()))
				{
					throw new InvalidRuleException("Code is damaged (truncated)");
				}
				out.write(buf, 0, n);
				if (out.size() > MAX_JSON_BYTES)
				{
					throw new InvalidRuleException("Code is too large");
				}
			}
			return out.toByteArray();
		}
		catch (DataFormatException e)
		{
			throw new InvalidRuleException("Code is damaged");
		}
		finally
		{
			inflater.end();
		}
	}

	/** Rejects deeply nested JSON before Gson's recursive parser can overflow the stack. */
	static void checkDepth(String json)
	{
		int depth = 0;
		boolean inString = false;
		for (int i = 0; i < json.length(); i++)
		{
			char c = json.charAt(i);
			if (inString)
			{
				if (c == '\\')
				{
					i++;
				}
				else if (c == '"')
				{
					inString = false;
				}
			}
			else if (c == '"')
			{
				inString = true;
			}
			else if (c == '{' || c == '[')
			{
				if (++depth > MAX_JSON_DEPTH)
				{
					throw new InvalidRuleException("Code is nested too deeply");
				}
			}
			else if (c == '}' || c == ']')
			{
				depth--;
			}
		}
	}
}
