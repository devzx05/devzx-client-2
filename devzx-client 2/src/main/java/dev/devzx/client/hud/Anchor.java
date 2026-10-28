package dev.devzx.client.hud;

/** Screen anchor; fx/fy are the fractional position (0 = left/top, 1 = right/bottom). */
public enum Anchor {
	TOP_LEFT(0f, 0f), TOP_CENTER(0.5f, 0f), TOP_RIGHT(1f, 0f),
	MIDDLE_LEFT(0f, 0.5f), CENTER(0.5f, 0.5f), MIDDLE_RIGHT(1f, 0.5f),
	BOTTOM_LEFT(0f, 1f), BOTTOM_CENTER(0.5f, 1f), BOTTOM_RIGHT(1f, 1f);

	public final float fx;
	public final float fy;

	Anchor(float fx, float fy) {
		this.fx = fx;
		this.fy = fy;
	}

	public boolean isRight() { return fx > 0.75f; }
	public boolean isBottom() { return fy > 0.75f; }
}
