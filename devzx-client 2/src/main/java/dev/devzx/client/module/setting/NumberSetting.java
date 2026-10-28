package dev.devzx.client.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.Locale;

/** A double constrained to [min, max]. */
public final class NumberSetting extends Setting<Double> {
	private final double min;
	private final double max;

	public NumberSetting(String name, String description, double defaultValue, double min, double max) {
		super(name, description, Math.max(min, Math.min(max, defaultValue)));
		this.min = min;
		this.max = max;
	}

	public double getMin() { return min; }
	public double getMax() { return max; }

	@Override
	protected Double sanitize(Double newValue) {
		if (newValue == null || newValue.isNaN()) return value;
		return Math.max(min, Math.min(max, newValue));
	}

	@Override
	public JsonElement toJson() { return new JsonPrimitive(get()); }

	@Override
	public void fromJson(JsonElement element) {
		if (element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
			set(element.getAsDouble());
		}
	}

	@Override
	public boolean parse(String text) {
		try {
			double parsed = Double.parseDouble(text.trim());
			if (Double.isNaN(parsed) || Double.isInfinite(parsed)) return false;
			set(parsed);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	@Override
	public String display() {
		double v = get();
		return v == Math.rint(v) ? Long.toString((long) v) : String.format(Locale.ROOT, "%.2f", v);
	}

	@Override
	public String hint() { return display(min) + ".." + display(max); }

	private static String display(double v) {
		return v == Math.rint(v) ? Long.toString((long) v) : String.format(Locale.ROOT, "%.2f", v);
	}
}
