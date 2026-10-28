package dev.devzx.client.util;

/** Allocation-light formatting helpers. Pure Java. */
public final class TextUtil {
	private static final long[] POW10 = {1L, 10L, 100L, 1000L};
	private static final String[] ROMAN = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};

	private TextUtil() {}

	/** Fixed-point formatting without String.format. Decimals are clamped to 0..3. */
	public static String fixed(double value, int decimals) {
		int d = Math.max(0, Math.min(3, decimals));
		if (Double.isNaN(value) || Double.isInfinite(value)) return "0";
		long scaled = Math.round(Math.abs(value) * POW10[d]);
		StringBuilder sb = new StringBuilder(16);
		if (value < 0 && scaled != 0) sb.append('-');
		sb.append(scaled / POW10[d]);
		if (d > 0) {
			String frac = Long.toString(scaled % POW10[d]);
			sb.append('.');
			for (int i = frac.length(); i < d; i++) sb.append('0');
			sb.append(frac);
		}
		return sb.toString();
	}

	/** "M:SS", or "H:MM:SS" from one hour up. */
	public static String duration(long totalSeconds) {
		long s = Math.max(0, totalSeconds);
		long h = s / 3600;
		long m = (s / 60) % 60;
		long sec = s % 60;
		return h > 0 ? h + ":" + twoDigits(m) + ":" + twoDigits(sec) : m + ":" + twoDigits(sec);
	}

	public static String twoDigits(long v) { return v < 10 ? "0" + v : Long.toString(v); }

	/** Roman numerals 1..10, plain digits otherwise. */
	public static String roman(int n) { return n >= 1 && n < ROMAN.length ? ROMAN[n] : Integer.toString(n); }

	public static String distance(double meters) {
		return meters < 1000 ? Math.round(meters) + " m" : fixed(meters / 1000.0, 2) + " km";
	}
}
