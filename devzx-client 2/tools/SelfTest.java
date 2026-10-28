import dev.devzx.client.hud.Anchor;
import dev.devzx.client.module.Category;
import dev.devzx.client.module.Module;
import dev.devzx.client.module.ModuleManager;
import dev.devzx.client.module.setting.*;
import dev.devzx.client.theme.Theme;
import dev.devzx.client.util.ClickTracker;
import dev.devzx.client.util.Compass;
import dev.devzx.client.util.TextUtil;

/** Plain-Java checks for the logic that does not touch Minecraft. Run: see docs/STATUS.md */
public class SelfTest {
	static int failures, checks;

	static void eq(Object expected, Object actual, String what) {
		checks++;
		if (!java.util.Objects.equals(expected, actual)) {
			failures++;
			System.out.println("FAIL " + what + ": expected <" + expected + "> got <" + actual + ">");
		}
	}

	public static void main(String[] args) {
		// ClickTracker: rolling one-second window, edge detection, separate buttons
		ClickTracker t = new ClickTracker();
		t.update(true, false, 0);   t.update(false, false, 50);
		t.update(true, false, 100); t.update(true, false, 150);   // held: still one click
		t.update(false, false, 180);
		t.update(true, true, 200);  t.update(false, false, 250);
		eq(3, t.leftCps(500), "left cps at 500");
		eq(1, t.rightCps(500), "right cps at 500");
		eq(2, t.leftCps(1000), "left cps at 1000 (t=0 expired)");
		eq(1, t.leftCps(1150), "left cps at 1150");
		eq(0, t.leftCps(1300), "left cps at 1300");
		eq(0, t.rightCps(1300), "right cps at 1300");
		eq(3L, t.totalLeft(), "total left");
		eq(1L, t.totalRight(), "total right");

		// Compass: yaw 0 = south, 90 = west, -90 = east, 180/-180 = north
		eq("S", Compass.shortName(Compass.index(0f)), "yaw 0");
		eq("W", Compass.shortName(Compass.index(90f)), "yaw 90");
		eq("E", Compass.shortName(Compass.index(-90f)), "yaw -90");
		eq("N", Compass.shortName(Compass.index(180f)), "yaw 180");
		eq("N", Compass.shortName(Compass.index(-180f)), "yaw -180");
		eq("SW", Compass.shortName(Compass.index(45f)), "yaw 45");
		eq("SE", Compass.shortName(Compass.index(-45f)), "yaw -45");
		eq("S", Compass.shortName(Compass.index(359.9f)), "yaw 359.9");
		eq("-Z", Compass.axis(Compass.index(180f)), "north axis");
		eq("", Compass.axis(Compass.index(45f)), "diagonal axis");
		eq(-170f, Compass.wrap(190f), "wrap 190");
		eq(-180f, Compass.wrap(-180f), "wrap -180");

		// TextUtil
		eq("123.5", TextUtil.fixed(123.456, 1), "fixed 1");
		eq("64.0", TextUtil.fixed(64, 1), "fixed whole");
		eq("0.0", TextUtil.fixed(-0.04, 1), "fixed no negative zero");
		eq("-5", TextUtil.fixed(-5.25, 0), "fixed 0 decimals");
		eq("0.05", TextUtil.fixed(0.05, 2), "fixed pad");
		eq("-1234.568", TextUtil.fixed(-1234.5678, 3), "fixed 3");
		eq("0:00", TextUtil.duration(0), "duration 0");
		eq("1:05", TextUtil.duration(65), "duration 65");
		eq("1:01:01", TextUtil.duration(3661), "duration 3661");
		eq("0:00", TextUtil.duration(-4), "duration negative");
		eq("II", TextUtil.roman(2), "roman 2");
		eq("11", TextUtil.roman(11), "roman 11");
		eq("50 m", TextUtil.distance(50), "distance m");
		eq("1.50 km", TextUtil.distance(1500), "distance km");

		// Theme
		eq(0xFF808080, Theme.lerp(0xFF000000, 0xFFFFFFFF, 0.5f), "lerp mid");
		eq(0xFF000000, Theme.lerp(0xFF000000, 0xFFFFFFFF, -3f), "lerp clamp low");
		eq(0x80FF7A00, Theme.withAlpha(Theme.PRIMARY, 0x80), "withAlpha");

		// Settings
		NumberSetting n = new NumberSetting("scale", "", 1, 0.5, 3);
		n.set(10.0);  eq(3.0, n.get(), "number clamps high");
		n.set(Double.NaN); eq(3.0, n.get(), "number ignores NaN");
		eq(false, n.parse("abc"), "number rejects text");
		eq(true, n.parse("2.5"), "number parses");  eq(2.5, n.get(), "number value");
		eq("0.50..3", n.hint(), "number hint");
		BooleanSetting b = new BooleanSetting("b", "", false);
		eq(true, b.parse("ON"), "bool on"); eq(true, b.get(), "bool value");
		eq(false, b.parse("maybe"), "bool rejects");
		ColorSetting c = new ColorSetting("c", "", Theme.PRIMARY);
		eq("#FF7A00", c.display(), "color default");
		eq(true, c.parse("#ff9d2e"), "color parse"); eq("#FF9D2E", c.display(), "color display");
		eq(0xFFFF9D2E, c.argb(), "color argb");
		eq(false, c.parse("zzz"), "color rejects"); eq(false, c.parse("12345"), "color rejects short");
		eq(true, c.parse("0x101010"), "color 0x prefix"); eq("#101010", c.display(), "color 0x value");
		EnumSetting<Anchor> e = new EnumSetting<>("a", "", Anchor.class, Anchor.TOP_LEFT);
		eq(true, e.parse("bottom_right"), "enum parse"); eq(Anchor.BOTTOM_RIGHT, e.get(), "enum value");
		eq(false, e.parse("nowhere"), "enum rejects"); eq(Anchor.BOTTOM_RIGHT, e.get(), "enum unchanged");
		eq(true, e.hint().startsWith("top_left|top_center"), "enum hint");

		// Module framework
		ModuleManager mm = new ModuleManager();
		Module m1 = new Module("One", "d", Category.HUD) {};
		mm.register(m1);
		boolean threw = false;
		try { mm.register(new Module("one", "d", Category.HUD) {}); } catch (IllegalStateException ex) { threw = true; }
		eq(true, threw, "duplicate name rejected");
		eq(m1, mm.get("ONE"), "case-insensitive lookup");
		int v = Module.stateVersion();
		m1.toggle();
		eq(true, m1.isEnabled(), "toggle on"); eq(true, Module.stateVersion() > v, "stateVersion bumps");

		System.out.println(checks + " checks, " + failures + " failures");
		if (failures > 0) System.exit(1);
	}
}
