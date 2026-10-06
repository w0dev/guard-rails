package com.guardrails.codec;

import com.google.gson.Gson;
import com.guardrails.model.GuardedAction;
import com.guardrails.model.InvalidRuleException;
import com.guardrails.model.ItemGroup;
import com.guardrails.model.Mode;
import com.guardrails.model.Rule;
import com.guardrails.model.Samples;
import com.guardrails.model.Target;
import com.guardrails.model.condition.Condition;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.zip.Deflater;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

public class ShareCodecTest
{
	private final ShareCodec codec = new ShareCodec(new Gson());

	private static final String REPOT_JSON = "{\"schemaVersion\":1,\"rules\":[{\"name\":\"Repot guard\","
		+ "\"target\":{\"action\":\"DRINK\",\"items\":\"DIVINE_SUPER_COMBAT\"},"
		+ "\"when\":[{\"type\":\"EFFECT_ACTIVE\",\"effect\":\"DIVINE_SUPER_COMBAT\"}],"
		+ "\"mode\":\"HOLD_TO_CONFIRM\"}]}";

	@Test
	public void jsonIsCompactAndStable()
	{
		assertEquals(REPOT_JSON, codec.toJson(Collections.singletonList(Samples.repotGuard())));
	}

	@Test
	public void roundTripsEveryConditionType()
	{
		for (Condition c : Samples.conditions().values())
		{
			Rule rule = Samples.repotGuard().toBuilder().condition(c).build();
			assertEquals(c.getType().name(), Collections.singletonList(rule),
				codec.decode(codec.encode(Collections.singletonList(rule))));
		}
	}

	@Test
	public void roundTripsEveryRuleField()
	{
		List<Rule> rules = Arrays.asList(Samples.everything(), Samples.repotGuard(),
			Samples.repotGuard().toBuilder().name("Wildy alch").allowInWilderness(true).build());
		assertEquals(rules, codec.fromJson(codec.toJson(rules)));
		assertEquals(rules, codec.decode(codec.encode(rules)));
	}

	@Test
	public void codeHasVersionPrefixAndIsUrlSafe()
	{
		String code = codec.encode(Collections.singletonList(Samples.repotGuard()));
		assertTrue(code.startsWith("GR1."));
		assertTrue(code, code.substring(4).matches("[A-Za-z0-9_-]+"));
	}

	@Test
	public void toleratesWhitespaceFromPasting()
	{
		String code = codec.encode(Collections.singletonList(Samples.repotGuard()));
		String pasted = "  " + code.substring(0, 10) + "\n" + code.substring(10) + " \t";
		assertEquals(Collections.singletonList(Samples.repotGuard()), codec.decode(pasted));
	}

	// --- "cannot construct a rule targeting Attack" from outside the type system -------------

	@Test
	public void rejectsAttackAndOtherUntargetableActions()
	{
		for (String action : Arrays.asList("ATTACK", "Attack", "CAST_ON_NPC", "CAST_ON_PLAYER", "TRADE",
			"FOLLOW", "REPORT", "PICKPOCKET", "KNOCK_OUT", "LURE", "WALK_HERE", "drink", ""))
		{
			assertRejectedJson(REPOT_JSON.replace("\"DRINK\"", "\"" + action + "\""));
		}
	}

	@Test
	public void rejectsSmuggledIdsAndUnknownFields()
	{
		// a raw ID instead of a group name
		assertRejectedJson(REPOT_JSON.replace("\"DIVINE_SUPER_COMBAT\"},", "4151},"));
		// extra ID fields at every level
		assertRejectedJson(REPOT_JSON.replace("\"items\":", "\"itemId\":4151,\"items\":"));
		assertRejectedJson(REPOT_JSON.replace("\"action\":", "\"npc\":\"Goblin\",\"action\":"));
		assertRejectedJson(REPOT_JSON.replace("\"name\":", "\"menuType\":9,\"name\":"));
		assertRejectedJson(REPOT_JSON.replace("\"type\":", "\"varbit\":13663,\"type\":"));
		assertRejectedJson(REPOT_JSON.replace("\"schemaVersion\":1,", "\"schemaVersion\":1,\"x\":1,"));
		// an item action pointed at an object group, or with two groups
		assertRejectedJson(REPOT_JSON.replace("\"items\":\"DIVINE_SUPER_COMBAT\"", "\"objects\":\"INFERNO_ENTRANCE\""));
		assertRejectedJson(REPOT_JSON.replace("\"items\":", "\"objects\":\"INFERNO_ENTRANCE\",\"items\":"));
	}

	@Test
	public void rejectsWrongTypesAndValues()
	{
		assertRejectedJson(REPOT_JSON.replace("\"schemaVersion\":1", "\"schemaVersion\":2"));
		assertRejectedJson(REPOT_JSON.replace("\"schemaVersion\":1", "\"schemaVersion\":\"1\""));
		assertRejectedJson(REPOT_JSON.replace("\"HOLD_TO_CONFIRM\"", "\"REMOVE\""));
		assertRejectedJson(REPOT_JSON.replace("\"mode\":\"HOLD_TO_CONFIRM\"", "\"mode\":\"HOLD_TO_CONFIRM\",\"holdMillis\":50"));
		assertRejectedJson(REPOT_JSON.replace("\"mode\":\"HOLD_TO_CONFIRM\"", "\"mode\":\"HOLD_TO_CONFIRM\",\"holdMillis\":600.5"));
		assertRejectedJson(REPOT_JSON.replace("\"mode\":\"HOLD_TO_CONFIRM\"", "\"mode\":\"HOLD_TO_CONFIRM\",\"enabled\":\"yes\""));
		assertRejectedJson(REPOT_JSON.replace("\"Repot guard\"", "\"<img=1>\""));
		assertRejectedJson(REPOT_JSON.replace("\"Repot guard\"", "null"));
		assertRejectedJson(REPOT_JSON.replace("\"rules\":[", "\"rules\":[[],"));
		assertRejectedJson("{\"schemaVersion\":1,\"rules\":[]}");
		assertRejectedJson("[]");
		assertRejectedJson("");
		assertRejectedJson("{");
		assertRejectedJson(REPOT_JSON + "trailing");
	}

	@Test
	public void rejectsWildernessOptInForObjects()
	{
		String json = REPOT_JSON
			.replace("{\"action\":\"DRINK\",\"items\":\"DIVINE_SUPER_COMBAT\"}", "{\"action\":\"PULL\",\"objects\":\"WILDERNESS_LEVERS\"}")
			.replace("\"mode\":", "\"allowInWilderness\":true,\"mode\":");
		assertRejectedJson(json);
	}

	@Test
	public void rejectsTooManyRulesAndConditions()
	{
		List<Rule> many = new ArrayList<>(Collections.nCopies(RuleJson.MAX_RULES + 1, Samples.repotGuard()));
		assertRejectedJson(codec.toJson(many));

		StringBuilder when = new StringBuilder();
		for (int i = 0; i <= Rule.MAX_CONDITIONS; i++)
		{
			when.append(i == 0 ? "" : ",").append("{\"type\":\"SKULLED\"}");
		}
		assertRejectedJson(REPOT_JSON.replace("{\"type\":\"EFFECT_ACTIVE\",\"effect\":\"DIVINE_SUPER_COMBAT\"}", when));
	}

	@Test
	public void rejectsDeepNesting()
	{
		StringBuilder deep = new StringBuilder();
		for (int i = 0; i < 100_000; i++)
		{
			deep.append('[');
		}
		assertRejected(() -> ShareCodec.checkDepth(deep.toString()));
		// brackets inside strings don't count
		ShareCodec.checkDepth("{\"name\":\"[[[[[[[[[[[[[[\\\"[[[[[\"}");
	}

	@Test
	public void rejectsMalformedCodes()
	{
		String good = codec.encode(Collections.singletonList(Samples.repotGuard()));
		assertRejectedCode(null);
		assertRejectedCode("");
		assertRejectedCode("GR2." + good.substring(4));
		assertRejectedCode(good.substring(4));
		assertRejectedCode("GR1.!!!!");
		assertRejectedCode(good.substring(0, good.length() - 6));
		assertRejectedCode("GR1." + "A".repeat(ShareCodec.MAX_CODE_CHARS));
		char[] flipped = good.toCharArray();
		flipped[12] = flipped[12] == 'A' ? 'B' : 'A';
		assertRejectedCode(new String(flipped));
	}

	@Test
	public void rejectsDecompressionBomb()
	{
		byte[] zeros = new byte[10 * 1024 * 1024];
		Deflater d = new Deflater(Deflater.BEST_COMPRESSION);
		d.setInput(zeros);
		d.finish();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buf = new byte[4096];
		while (!d.finished())
		{
			out.write(buf, 0, d.deflate(buf));
		}
		d.end();
		String bomb = "GR1." + Base64.getUrlEncoder().withoutPadding().encodeToString(out.toByteArray());
		assertTrue("bomb fits under the code length cap", bomb.length() <= ShareCodec.MAX_CODE_CHARS);
		assertRejectedCode(bomb);
	}

	@Test
	public void plainTextCodeIsRejectedWithHelpfulMessage()
	{
		try
		{
			codec.decode(REPOT_JSON);
			fail();
		}
		catch (InvalidRuleException e)
		{
			assertTrue(e.getMessage().contains("GR1."));
		}
	}

	@Test
	public void ruleBuiltInCodeDecodesIdenticallyToBuilder()
	{
		Rule built = Rule.builder()
			.name("GE no-drop")
			.target(Target.item(GuardedAction.DROP, ItemGroup.ANY_ITEM))
			.mode(Mode.RIGHT_CLICK_ONLY)
			.build();
		String json = "{\"schemaVersion\":1,\"rules\":[{\"name\":\"GE no-drop\","
			+ "\"target\":{\"action\":\"DROP\",\"items\":\"ANY_ITEM\"},\"mode\":\"RIGHT_CLICK_ONLY\"}]}";
		assertEquals(Collections.singletonList(built), codec.fromJson(json));
		assertEquals(json, new String(codec.toJson(Collections.singletonList(built)).getBytes(StandardCharsets.UTF_8),
			StandardCharsets.UTF_8));
	}

	private void assertRejectedJson(CharSequence json)
	{
		assertRejected(() -> codec.fromJson(json.toString()));
	}

	private void assertRejectedCode(String code)
	{
		assertRejected(() -> codec.decode(code));
	}

	private static void assertRejected(Runnable r)
	{
		try
		{
			r.run();
		}
		catch (InvalidRuleException expected)
		{
			return;
		}
		throw new AssertionError("expected InvalidRuleException");
	}
}
