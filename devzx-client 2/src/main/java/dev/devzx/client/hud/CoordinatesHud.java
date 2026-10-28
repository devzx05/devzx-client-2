package dev.devzx.client.hud;

import dev.devzx.client.module.setting.BooleanSetting;
import dev.devzx.client.module.setting.NumberSetting;
import dev.devzx.client.util.TextUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public final class CoordinatesHud extends TextHud {
	private final BooleanSetting showX;
	private final BooleanSetting showY;
	private final BooleanSetting showZ;
	private final BooleanSetting multiline;
	private final NumberSetting decimals;

	private long lastX = Long.MIN_VALUE;
	private long lastY;
	private long lastZ;
	private int lastFlags = -1;

	public CoordinatesHud() {
		super("Coordinates", "Player X / Y / Z position", Anchor.MIDDLE_LEFT, 4, 0);
		showX = addSetting(new BooleanSetting("showX", "Show X", true));
		showY = addSetting(new BooleanSetting("showY", "Show Y", true));
		showZ = addSetting(new BooleanSetting("showZ", "Show Z", true));
		multiline = addSetting(new BooleanSetting("multiline", "One axis per line", false));
		decimals = addSetting(new NumberSetting("decimals", "Decimal places", 1, 0, 3));
		setEnabled(true);
	}

	@Override
	public void update(Minecraft mc, long nowMs) {
		LocalPlayer p = mc.player;
		if (p == null) return;
		int d = decimals.get().intValue();
		int flags = (showX.get() ? 1 : 0) | (showY.get() ? 2 : 0) | (showZ.get() ? 4 : 0) | (multiline.get() ? 8 : 0) | (d << 4);
		long mult = d == 0 ? 1 : d == 1 ? 10 : d == 2 ? 100 : 1000;
		long kx = Math.round(p.getX() * mult);
		long ky = Math.round(p.getY() * mult);
		long kz = Math.round(p.getZ() * mult);
		if (kx == lastX && ky == lastY && kz == lastZ && flags == lastFlags) return;
		lastX = kx;
		lastY = ky;
		lastZ = kz;
		lastFlags = flags;

		String x = TextUtil.fixed(p.getX(), d);
		String y = TextUtil.fixed(p.getY(), d);
		String z = TextUtil.fixed(p.getZ(), d);
		boolean sx = showX.get();
		boolean sy = showY.get();
		boolean sz = showZ.get();

		if (multiline.get()) {
			int n = 0;
			if (sx) setLine(mc.font, n++, "X", x);
			if (sy) setLine(mc.font, n++, "Y", y);
			if (sz) setLine(mc.font, n++, "Z", z);
			setLineCount(n);
		} else {
			StringBuilder label = new StringBuilder(3);
			StringBuilder value = new StringBuilder(32);
			if (sx) { label.append('X'); value.append(x); }
			if (sy) { label.append('Y'); if (value.length() > 0) value.append(", "); value.append(y); }
			if (sz) { label.append('Z'); if (value.length() > 0) value.append(", "); value.append(z); }
			if (value.length() == 0) {
				setLineCount(0);
			} else {
				setLine(mc.font, 0, label.toString(), value.toString());
				setLineCount(1);
			}
		}
	}
}
