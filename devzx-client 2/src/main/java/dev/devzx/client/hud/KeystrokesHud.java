package dev.devzx.client.hud;

import dev.devzx.client.module.setting.BooleanSetting;
import dev.devzx.client.render.HudRenderer;
import dev.devzx.client.theme.Theme;
import dev.devzx.client.util.ClickTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;

/** WASD, mouse buttons, sprint, sneak and space with eased press animations. */
public final class KeystrokesHud extends HudComponent {
	private static final int KEY = 22;
	private static final int GAP = 2;
	private static final int FULL = KEY * 3 + GAP * 2;
	private static final float ANIM_SPEED = 24f; // higher = snappier

	private static final class Key {
		final String label;
		int labelW = -1;
		int x, y, w, h;
		boolean visible;
		boolean down;
		float anim;
		String sub = "";
		int subW;

		Key(String label) { this.label = label; }

		void place(int x, int y, int w, int h) {
			this.x = x; this.y = y; this.w = w; this.h = h;
			this.visible = true;
		}
	}

	private final ClickTracker clicks;
	private final BooleanSetting mouse;
	private final BooleanSetting showCps;
	private final BooleanSetting extras;
	private final BooleanSetting space;

	private final Key w = new Key("W");
	private final Key a = new Key("A");
	private final Key s = new Key("S");
	private final Key d = new Key("D");
	private final Key lmb = new Key("LMB");
	private final Key rmb = new Key("RMB");
	private final Key sprint = new Key("Sprint");
	private final Key sneak = new Key("Sneak");
	private final Key spaceKey = new Key("Space");
	private final Key[] all = {w, a, s, d, lmb, rmb, sprint, sneak, spaceKey};

	private long lastMs;
	private int height = 0;
	private int lastLeft = -1;
	private int lastRight = -1;

	public KeystrokesHud(ClickTracker clicks) {
		super("Keystrokes", "Movement keys and mouse buttons", Anchor.BOTTOM_RIGHT, 6, 6, false);
		this.clicks = clicks;
		mouse = addSetting(new BooleanSetting("mouse", "Show mouse buttons", true));
		showCps = addSetting(new BooleanSetting("cps", "Show CPS under the mouse buttons", true));
		extras = addSetting(new BooleanSetting("extras", "Show sprint and sneak", true));
		space = addSetting(new BooleanSetting("space", "Show the space bar", true));
		setEnabled(true);
	}

	@Override
	public void update(Minecraft mc, long nowMs) {
		Options o = mc.options;
		if (w.labelW < 0) {
			for (Key k : all) k.labelW = mc.font.width(k.label);
		}
		float dt = lastMs == 0 ? 0f : Math.min(0.1f, (nowMs - lastMs) / 1000f);
		lastMs = nowMs;

		layout();

		w.down = o.keyUp.isDown();
		a.down = o.keyLeft.isDown();
		s.down = o.keyDown.isDown();
		d.down = o.keyRight.isDown();
		lmb.down = o.keyAttack.isDown();
		rmb.down = o.keyUse.isDown();
		sprint.down = o.keySprint.isDown();
		sneak.down = o.keyShift.isDown();
		spaceKey.down = o.keyJump.isDown();

		float k = 1f - (float) Math.exp(-dt * ANIM_SPEED);
		for (Key key : all) key.anim += ((key.down ? 1f : 0f) - key.anim) * k;

		if (mouse.get() && showCps.get()) {
			int l = clicks.leftCps(nowMs);
			int r = clicks.rightCps(nowMs);
			if (l != lastLeft) { lastLeft = l; lmb.sub = Integer.toString(l); lmb.subW = mc.font.width(lmb.sub); }
			if (r != lastRight) { lastRight = r; rmb.sub = Integer.toString(r); rmb.subW = mc.font.width(rmb.sub); }
		}
	}

	private void layout() {
		for (Key key : all) key.visible = false;
		w.place(KEY + GAP, 0, KEY, KEY);
		a.place(0, KEY + GAP, KEY, KEY);
		s.place(KEY + GAP, KEY + GAP, KEY, KEY);
		d.place((KEY + GAP) * 2, KEY + GAP, KEY, KEY);
		int y = (KEY + GAP) * 2;
		int half = (FULL - GAP) / 2;
		if (mouse.get()) {
			int mh = showCps.get() ? 26 : KEY;
			lmb.place(0, y, half, mh);
			rmb.place(half + GAP, y, FULL - half - GAP, mh);
			y += mh + GAP;
		}
		if (extras.get()) {
			sprint.place(0, y, half, 18);
			sneak.place(half + GAP, y, FULL - half - GAP, 18);
			y += 18 + GAP;
		}
		if (space.get()) {
			spaceKey.place(0, y, FULL, 14);
			y += 14 + GAP;
		}
		height = y - GAP;
	}

	@Override
	protected int contentWidth() { return FULL; }

	@Override
	protected int contentHeight() { return height; }

	@Override
	protected void draw(HudRenderer r) {
		int accent = colorArgb();
		for (Key key : all) {
			if (!key.visible) continue;
			int bg = Theme.lerp(Theme.withAlpha(Theme.PANEL, 0xC8), Theme.withAlpha(accent, 0xE6), key.anim);
			int border = Theme.lerp(Theme.PANEL_ALT, accent, key.anim);
			int fg = Theme.lerp(Theme.TEXT, Theme.BACKGROUND, key.anim);
			r.fill(key.x, key.y, key.x + key.w, key.y + key.h, bg);
			r.outline(key.x, key.y, key.w, key.h, border);

			boolean hasSub = !key.sub.isEmpty() && (key == lmb || key == rmb) && mouse.get() && showCps.get();
			int textY = hasSub ? key.y + 4 : key.y + (key.h - 8) / 2;
			r.text(key.label, key.x + (key.w - key.labelW) / 2, textY, fg);
			if (hasSub) {
				int subColor = Theme.lerp(Theme.TEXT_SECONDARY, Theme.BACKGROUND, key.anim);
				r.text(key.sub, key.x + (key.w - key.subW) / 2, key.y + 14, subColor);
			}
		}
	}
}
