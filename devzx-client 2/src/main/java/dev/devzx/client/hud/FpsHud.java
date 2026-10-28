package dev.devzx.client.hud;

import net.minecraft.client.Minecraft;

/** FPS counter. Counts rendered frames over a 500 ms window (no dependence on internal Minecraft FPS getters). */
public final class FpsHud extends TextHud {
	private long windowStart;
	private int frames;
	private int fps = -1;
	private int shown = Integer.MIN_VALUE;

	public FpsHud() {
		super("FPS", "Frames per second", Anchor.TOP_LEFT, 4, 4);
		setEnabled(true);
	}

	@Override
	protected void onEnable() {
		windowStart = 0;
		frames = 0;
		fps = -1;
		shown = Integer.MIN_VALUE;
	}

	@Override
	public void update(Minecraft mc, long nowMs) {
		if (windowStart == 0) windowStart = nowMs;
		frames++;
		long elapsed = nowMs - windowStart;
		if (elapsed >= 500) {
			fps = (int) (frames * 1000L / elapsed);
			frames = 0;
			windowStart = nowMs;
		}
		if (fps != shown) {
			shown = fps;
			setLine(mc.font, 0, "FPS", fps < 0 ? "--" : Integer.toString(fps));
		}
		setLineCount(1);
	}
}
