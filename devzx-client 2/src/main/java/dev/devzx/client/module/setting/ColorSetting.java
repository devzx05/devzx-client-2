package dev.devzx.client.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.Locale;

/** An RGB colour stored as 0xRRGGBB; use {@link #argb()} for draw calls. */
public final class ColorSetting extends Setting<Integer> {
	public ColorSetting(String name, String description, int rgbDefault) {
		super(name, description, rgbDefault & 0xFFFFFF);
	}

	public int argb() { return 0xFF000000 | get(); }

	@Override
	protected Integer sanitize(Integer newValue) {
		return newValue == null ? value : (newValue & 0xFFFFFF);
	}

	@Override
	public JsonElement toJson() { return new JsonPrimitive(display()); }

	@Override
	public void fromJson(JsonElement element) {
		if (element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
			parse(element.getAsString());
		}
	}

	@Override
	public boolean parse(String text) {
		Integer parsed = parseHex(text);
		if (parsed == null) return false;
		set(parsed);
		return true;
	}

	@Override
	public String display() { return String.format(Locale.ROOT, "#%06X", get()); }

	@Override
	public String hint() { return "#RRGGBB"; }

	static Integer parseHex(String text) {
		if (text == null) return null;
		String t = text.trim();
		if (t.startsWith("#")) t = t.substring(1);
		else if (t.startsWith("0x") || t.startsWith("0X")) t = t.substring(2);
		if (t.length() != 6) return null;
		try {
			return Integer.parseInt(t, 16);
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
