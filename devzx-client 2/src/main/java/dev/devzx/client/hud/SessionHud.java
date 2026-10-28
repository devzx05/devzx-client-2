package dev.devzx.client.hud;

import dev.devzx.client.module.setting.BooleanSetting;
import dev.devzx.client.util.ClickTracker;
import dev.devzx.client.util.TextUtil;
import net.minecraft.client.Minecraft;

/**
 * Session statistics that can be measured client-side without server support:
 * time since the client started, time since joining the current world/server,
 * total clicks, and horizontal distance moved since joining.
 * (Kills/deaths/K-D are not available from the client without per-server parsing and are not included.)
 */
public final class SessionHud extends TextHud {
	private final ClickTracker clicks;
	private final BooleanSetting showSession;
	private final BooleanSetting showWorld;
	private final BooleanSetting showClicks;
	private final BooleanSetting showDistance;

	private final long clientStartMs = System.currentTimeMillis();
	private long joinMs = System.currentTimeMillis();
	private Object lastConnection;
	private double lastX;
	private double lastZ;
	private boolean havePos;
	private double distance;
	private long lastSecond = Long.MIN_VALUE;
	private int lastFlags = -1;

	public SessionHud(ClickTracker clicks) {
		super("Session", "Session time, clicks and distance", Anchor.TOP_LEFT, 4, 4);
		this.clicks = clicks;
		showSession = addSetting(new BooleanSetting("showSession", "Time since the game started", true));
		showWorld = addSetting(new BooleanSetting("showWorld", "Time since joining this world/server", true));
		showClicks = addSetting(new BooleanSetting("showClicks", "Total clicks this session", true));
		showDistance = addSetting(new BooleanSetting("showDistance", "Distance moved since joining", false));
	}

	@Override
	public void update(Minecraft mc, long nowMs) {
		if (mc.player == null) return;
		long wall = System.currentTimeMillis();

		Object connection = mc.getConnection();
		if (connection != lastConnection) {
			lastConnection = connection;
			joinMs = wall;
			distance = 0;
			havePos = false;
		}
		double x = mc.player.getX();
		double z = mc.player.getZ();
		if (havePos) {
			double dx = x - lastX;
			double dz = z - lastZ;
			double step = Math.sqrt(dx * dx + dz * dz);
			if (step < 10.0) distance += step; // ignore teleports / dimension changes
		}
		lastX = x;
		lastZ = z;
		havePos = true;

		long second = wall / 1000L;
		int flags = (showSession.get() ? 1 : 0) | (showWorld.get() ? 2 : 0) | (showClicks.get() ? 4 : 0) | (showDistance.get() ? 8 : 0);
		if (second == lastSecond && flags == lastFlags) return;
		lastSecond = second;
		lastFlags = flags;

		int n = 0;
		if (showSession.get()) setLine(mc.font, n++, "Session", TextUtil.duration((wall - clientStartMs) / 1000L));
		if (showWorld.get()) setLine(mc.font, n++, mc.getCurrentServer() != null ? "Server" : "World", TextUtil.duration((wall - joinMs) / 1000L));
		if (showClicks.get()) setLine(mc.font, n++, "Clicks", Long.toString(clicks.totalLeft() + clicks.totalRight()));
		if (showDistance.get()) setLine(mc.font, n++, "Moved", TextUtil.distance(distance));
		setLineCount(n);
	}
}
