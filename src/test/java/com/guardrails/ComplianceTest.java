package com.guardrails;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * Static guard rails for our own source tree. Each banned token maps to the Plugin Hub rule
 * (example-plugin AGENTS.md / RuneLite wiki "Rejected or Rolled-Back Features") it protects.
 * This is a tripwire, not a proof: reviewers still read the code.
 */
public class ComplianceTest
{
	private static final Path MAIN_SRC = Paths.get("src", "main");

	private static final Map<String, String> BANNED = new LinkedHashMap<>();

	static
	{
		// Forbidden language features
		BANNED.put("java.lang.reflect", "reflection is forbidden");
		BANNED.put("getDeclaredField", "reflection is forbidden");
		BANNED.put("getDeclaredMethod", "reflection is forbidden");
		BANNED.put("setAccessible(", "reflection is forbidden");
		BANNED.put("Class.forName", "runtime class loading is forbidden");
		BANNED.put("ClassLoader", "runtime class loading is forbidden");
		BANNED.put("sun.misc.Unsafe", "native memory access is forbidden");
		BANNED.put("com.sun.jna", "JNA is forbidden");
		BANNED.put("System.loadLibrary", "JNI is forbidden");
		BANNED.put("ProcessBuilder", "external processes are forbidden");
		BANNED.put("Runtime.getRuntime", "external processes are forbidden");
		BANNED.put("ObjectInputStream", "Java serialization is forbidden");
		BANNED.put("ObjectOutputStream", "Java serialization is forbidden");
		BANNED.put("Thread.sleep", "AGENTS.md: never use Thread.sleep()");
		// Deprecated widget APIs
		BANNED.put("WidgetInfo", "AGENTS.md: use gameval InterfaceID, not WidgetInfo");
		BANNED.put("WidgetID", "AGENTS.md: use gameval InterfaceID, not WidgetID");
		// No network in v1
		BANNED.put("okhttp3", "Guard Rails v1 makes no network requests");
		BANNED.put("java.net.", "Guard Rails v1 makes no network requests");
		BANNED.put("java.awt.Desktop", "AGENTS.md: use LinkBrowser");
		// Input injection
		BANNED.put("java.awt.Robot", "no injecting input events");
		BANNED.put("dispatchEvent(", "no injecting input events");
		BANNED.put("new KeyEvent(", "no injecting input events");
		BANNED.put("new MouseEvent(", "no injecting input events");
		// Menus: Guard Rails never removes or hides an entry
		BANNED.put("setMenuEntries(", "never remove menu entries (conditional menu entry removal)");
		BANNED.put("removeMenuEntry(", "never remove menu entries (conditional menu entry removal)");
		BANNED.put("menuAction(", "never replay or synthesise a menu action (Client.menuAction is forbidden)");
		BANNED.put("JFileChooser", "AGENTS.md: use Filepath.Chooser");
	}

	@Test
	public void sourceUsesNoBannedApis() throws IOException
	{
		List<String> violations = new ArrayList<>();
		for (Path file : sourceFiles())
		{
			List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
			for (int i = 0; i < lines.size(); i++)
			{
				String line = lines.get(i);
				for (Map.Entry<String, String> ban : BANNED.entrySet())
				{
					if (line.contains(ban.getKey()))
					{
						violations.add(file + ":" + (i + 1) + " uses " + ban.getKey() + " — " + ban.getValue());
					}
				}
			}
		}
		assertTrue(String.join("\n", violations), violations.isEmpty());
	}

	@Test
	public void sourceIsJavaOnly() throws IOException
	{
		try (Stream<Path> files = Files.walk(MAIN_SRC))
		{
			List<Path> foreign = files
				.filter(Files::isRegularFile)
				.filter(p -> p.toString().endsWith(".kt") || p.toString().endsWith(".scala") || p.toString().endsWith(".groovy"))
				.collect(Collectors.toList());
			assertTrue("Java only: " + foreign, foreign.isEmpty());
		}
	}

	@Test
	public void noServiceLoaderFile()
	{
		assertTrue("AGENTS.md: do not include META-INF/services plugin file",
			!Files.exists(MAIN_SRC.resolve("resources/META-INF/services/net.runelite.client.plugins.Plugin")));
	}

	@Test
	public void pluginPropertiesMatchHubRequirements() throws IOException
	{
		Properties props = new Properties();
		try (InputStream in = Files.newInputStream(Paths.get("runelite-plugin.properties")))
		{
			props.load(in);
		}
		assertEquals("Guard Rails", props.getProperty("displayName"));
		assertEquals("standard", props.getProperty("build"));
		assertEquals(GuardRailsPlugin.class.getName(), props.getProperty("plugins"));
		assertTrue(props.getProperty("tags").contains("misclick"));
	}

	@Test
	public void iconFitsHubLimits() throws IOException
	{
		BufferedImage icon = ImageIO.read(Paths.get("icon.png").toFile());
		assertTrue("icon.png must be a real PNG", icon != null);
		assertTrue("icon.png max 48 wide, was " + icon.getWidth(), icon.getWidth() <= 48);
		assertTrue("icon.png max 72 tall, was " + icon.getHeight(), icon.getHeight() <= 72);
		assertTrue("icon.png max 256 KiB", Files.size(Paths.get("icon.png")) <= 256 * 1024);
	}

	private static List<Path> sourceFiles() throws IOException
	{
		try (Stream<Path> files = Files.walk(MAIN_SRC))
		{
			return files.filter(p -> p.toString().endsWith(".java")).collect(Collectors.toList());
		}
	}
}
