package dev.devzx.client.util;

/** Maps a Minecraft yaw (0 = south/+Z, 90 = west/-X) to compass names. */
public final class Compass {
	private static final String[] SHORT = {"S", "SW", "W", "NW", "N", "NE", "E", "SE"};
	private static final String[] LONG = {"South", "South-West", "West", "North-West", "North", "North-East", "East", "South-East"};
	private static final String[] AXIS = {"+Z", "", "-X", "", "-Z", "", "+X", ""};

	private Compass() {}

	/** Index 0..7 into the name tables, 45 degrees per step. */
	public static int index(float yaw) {
		float wrapped = ((yaw % 360f) + 360f) % 360f;
		return Math.round(wrapped / 45f) % 8;
	}

	/** Wraps to [-180, 180). */
	public static float wrap(float yaw) {
		float w = ((yaw % 360f) + 360f) % 360f;
		return w >= 180f ? w - 360f : w;
	}

	public static String shortName(int index) { return SHORT[index]; }
	public static String longName(int index) { return LONG[index]; }
	/** Axis hint for cardinal directions, empty for diagonals. */
	public static String axis(int index) { return AXIS[index]; }
}
