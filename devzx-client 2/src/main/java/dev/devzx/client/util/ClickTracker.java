package dev.devzx.client.util;

import java.util.ArrayDeque;

/**
 * Rolling one-second click counter for the left and right mouse buttons.
 * Feed it the current button state every frame; a click is a released->pressed
 * edge. Pure Java so it can be unit-tested without Minecraft.
 */
public final class ClickTracker {
	public static final long WINDOW_MS = 1000L;

	private final ArrayDeque<Long> left = new ArrayDeque<>();
	private final ArrayDeque<Long> right = new ArrayDeque<>();
	private boolean leftDown;
	private boolean rightDown;
	private long totalLeft;
	private long totalRight;

	public void update(boolean leftNow, boolean rightNow, long nowMs) {
		if (leftNow && !leftDown) { left.addLast(nowMs); totalLeft++; }
		if (rightNow && !rightDown) { right.addLast(nowMs); totalRight++; }
		leftDown = leftNow;
		rightDown = rightNow;
		prune(left, nowMs);
		prune(right, nowMs);
	}

	public int leftCps(long nowMs) { prune(left, nowMs); return left.size(); }
	public int rightCps(long nowMs) { prune(right, nowMs); return right.size(); }
	public long totalLeft() { return totalLeft; }
	public long totalRight() { return totalRight; }

	public void reset() {
		left.clear();
		right.clear();
		leftDown = false;
		rightDown = false;
		totalLeft = 0;
		totalRight = 0;
	}

	private static void prune(ArrayDeque<Long> q, long nowMs) {
		while (!q.isEmpty() && nowMs - q.peekFirst() >= WINDOW_MS) q.pollFirst();
	}
}
