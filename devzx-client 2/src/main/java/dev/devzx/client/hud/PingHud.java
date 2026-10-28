package dev.devzx.client.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;

/**
 * Latency from the server's tab-list entry for the local player.
 * "N/A" = no remote server (singleplayer / LAN host); "--" = no measurement yet.
 */
public final class PingHud extends TextHud {
	private static final long POLL_MS = 500;
	private long nextPoll;

	public PingHud() {
		super("Ping", "Network latency to the server", Anchor.TOP_LEFT, 4, 4);
		setEnabled(true);
	}

	@Override
	public void update(Minecraft mc, long nowMs) {
		setLineCount(1);
		if (nowMs < nextPoll) return;
		nextPoll = nowMs + POLL_MS;

		String text;
		ClientPacketListener connection = mc.getConnection();
		if (mc.player == null || connection == null) {
			text = "--";
		} else if (mc.getCurrentServer() == null) {
			text = "N/A";
		} else {
			PlayerInfo info = connection.getPlayerInfo(mc.player.getUUID());
			int ms = info == null ? -1 : info.getLatency();
			// The vanilla value is 0 until the first keep-alive round trip completes.
			text = ms > 0 ? ms + " ms" : "--";
		}
		setLine(mc.font, 0, "Ping", text);
	}
}
