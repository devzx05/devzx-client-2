package dev.devzx.client.hud;

import dev.devzx.client.module.setting.BooleanSetting;
import dev.devzx.client.util.Compass;
import net.minecraft.client.Minecraft;

public final class DirectionHud extends TextHud {
	private final BooleanSetting shortNames;
	private final BooleanSetting showYaw;
	private int lastKey = Integer.MIN_VALUE;

	public DirectionHud() {
		super("Direction", "Compass direction you are facing", Anchor.MIDDLE_LEFT, 4, 0);
		shortNames = addSetting(new BooleanSetting("short", "Use N/E/S/W style names", false));
		showYaw = addSetting(new BooleanSetting("yaw", "Also show yaw in degrees", false));
	}

	@Override
	public void update(Minecraft mc, long nowMs) {
		if (mc.player == null) return;
		float yaw = mc.player.getYRot();
		int idx = Compass.index(yaw);
		boolean shortForm = shortNames.get();
		boolean withYaw = showYaw.get();
		int yawInt = withYaw ? Math.round(Compass.wrap(yaw)) : 0;
		int key = (idx * 1000 + yawInt + 500) * 4 + (shortForm ? 1 : 0) + (withYaw ? 2 : 0);
		setLineCount(1);
		if (key == lastKey) return;
		lastKey = key;

		StringBuilder sb = new StringBuilder(24);
		if (shortForm) {
			sb.append(Compass.shortName(idx));
		} else {
			sb.append(Compass.longName(idx));
			String axis = Compass.axis(idx);
			if (!axis.isEmpty()) sb.append(" (").append(axis).append(')');
		}
		if (withYaw) sb.append("  ").append(yawInt).append('\u00B0');
		setLine(mc.font, 0, "Dir", sb.toString());
	}
}
