package dev.devzx.client.hud;

import dev.devzx.client.module.setting.BooleanSetting;
import dev.devzx.client.util.ClickTracker;
import net.minecraft.client.Minecraft;

/** Left/right clicks per second over a rolling one-second window. */
public final class CpsHud extends TextHud {
	private final ClickTracker clicks;
	private final BooleanSetting showRight;
	private int lastLeft = -1;
	private int lastRight = -1;
	private boolean lastShowRight;

	public CpsHud(ClickTracker clicks) {
		super("CPS", "Clicks per second (left | right)", Anchor.TOP_LEFT, 4, 4);
		this.clicks = clicks;
		this.showRight = addSetting(new BooleanSetting("showRight", "Also show right-click CPS", true));
		setEnabled(true);
	}

	@Override
	public void update(Minecraft mc, long nowMs) {
		int l = clicks.leftCps(nowMs);
		int r = clicks.rightCps(nowMs);
		boolean both = showRight.get();
		if (l != lastLeft || r != lastRight || both != lastShowRight) {
			lastLeft = l;
			lastRight = r;
			lastShowRight = both;
			setLine(mc.font, 0, "CPS", both ? l + " | " + r : Integer.toString(l));
		}
		setLineCount(1);
	}
}
