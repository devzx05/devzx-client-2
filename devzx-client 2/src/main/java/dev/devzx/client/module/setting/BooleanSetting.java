package dev.devzx.client.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.Locale;

public final class BooleanSetting extends Setting<Boolean> {
	public BooleanSetting(String name, String description, boolean defaultValue) {
		super(name, description, defaultValue);
	}

	public void toggle() { set(!get()); }

	@Override
	public JsonElement toJson() { return new JsonPrimitive(get()); }

	@Override
	public void fromJson(JsonElement element) {
		if (element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean()) {
			set(element.getAsBoolean());
		}
	}

	@Override
	public boolean parse(String text) {
		String t = text.trim().toLowerCase(Locale.ROOT);
		switch (t) {
			case "true": case "on": case "yes": case "1": set(true); return true;
			case "false": case "off": case "no": case "0": set(false); return true;
			default: return false;
		}
	}

	@Override
	public String hint() { return "true|false"; }
}
