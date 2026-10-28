package dev.devzx.client.theme;

/**
 * Central colour palette for devzx Client. All values are ARGB ints
 * (0xAARRGGBB) so they can be passed straight to Minecraft draw calls.
 * Pure Java: no Minecraft types, so it is safe to use from any package.
 */
public final class Theme {
	private Theme() {}

	// Brand
	public static final int PRIMARY = 0xFFFF7A00;      // #FF7A00
	public static final int SECONDARY = 0xFFFF9D2E;    // #FF9D2E

	// Surfaces
	public static final int BACKGROUND = 0xFF101010;   // #101010
	public static final int PANEL = 0xFF191919;        // #191919
	public static final int PANEL_ALT = 0xFF202020;    // #202020

	// Text
	public static final int TEXT = 0xFFFFFFFF;
	public static final int TEXT_SECONDARY = 0xFFA0A0A0; // gray, value chosen by devzx Client (not specified)

	/** Returns {@code argb} with its alpha channel replaced by {@code alpha} (0-255). */
	public static int withAlpha(int argb, int alpha) {
		return (clamp(alpha) << 24) | (argb & 0x00FFFFFF);
	}

	/** Linear interpolation between two ARGB colours; {@code t} is clamped to [0, 1]. */
	public static int lerp(int from, int to, float t) {
		float k = Math.max(0f, Math.min(1f, t));
		int a = mix(from >>> 24, to >>> 24, k);
		int r = mix((from >> 16) & 0xFF, (to >> 16) & 0xFF, k);
		int g = mix((from >> 8) & 0xFF, (to >> 8) & 0xFF, k);
		int b = mix(from & 0xFF, to & 0xFF, k);
		return (a << 24) | (r << 16) | (g << 8) | b;
	}

	private static int mix(int a, int b, float t) {
		return Math.round(a + (b - a) * t);
	}

	private static int clamp(int v) {
		return Math.max(0, Math.min(255, v));
	}
}
