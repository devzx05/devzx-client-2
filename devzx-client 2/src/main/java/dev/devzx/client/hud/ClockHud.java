package dev.devzx.client.hud;

import dev.devzx.client.module.setting.BooleanSetting;
import dev.devzx.client.module.setting.EnumSetting;
import net.minecraft.client.Minecraft;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Real-world (system) clock. Re-formatted at most once per second. */
public final class ClockHud extends TextHud {
	public enum Format { H24, H12 }

	private static final DateTimeFormatter F24 = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);
	private static final DateTimeFormatter F24S = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH);
	private static final DateTimeFormatter F12 = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);
	private static final DateTimeFormatter F12S = DateTimeFormatter.ofPattern("h:mm:ss a", Locale.ENGLISH);

	private final EnumSetting<Format> format;
	private final BooleanSetting seconds;
	private long lastWallSecond = Long.MIN_VALUE;
	private int lastMode = -1;

	public ClockHud() {
		super("Clock", "Real-world time", Anchor.TOP_LEFT, 4, 4);
		format = addSetting(new EnumSetting<>("format", "12 or 24 hour clock", Format.class, Format.H24));
		seconds = addSetting(new BooleanSetting("seconds", "Show seconds", false));
	}

	@Override
	public void update(Minecraft mc, long nowMs) {
		setLineCount(1);
		long wallSecond = System.currentTimeMillis() / 1000L;
		int mode = (format.get() == Format.H12 ? 1 : 0) | (seconds.get() ? 2 : 0);
		if (wallSecond == lastWallSecond && mode == lastMode) return;
		lastWallSecond = wallSecond;
		lastMode = mode;
		DateTimeFormatter f = switch (mode) {
			case 0 -> F24;
			case 1 -> F12;
			case 2 -> F24S;
			default -> F12S;
		};
		setLine(mc.font, 0, "Time", LocalTime.now().format(f));
	}
}
