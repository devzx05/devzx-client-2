package dev.devzx.client.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.Locale;

public final class EnumSetting<E extends Enum<E>> extends Setting<E> {
	private final Class<E> type;

	public EnumSetting(String name, String description, Class<E> type, E defaultValue) {
		super(name, description, defaultValue);
		this.type = type;
	}

	@Override
	protected E sanitize(E newValue) { return newValue == null ? value : newValue; }

	@Override
	public JsonElement toJson() { return new JsonPrimitive(get().name()); }

	@Override
	public void fromJson(JsonElement element) {
		if (element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
			parse(element.getAsString());
		}
	}

	@Override
	public boolean parse(String text) {
		String t = text.trim();
		for (E constant : type.getEnumConstants()) {
			if (constant.name().equalsIgnoreCase(t)) {
				set(constant);
				return true;
			}
		}
		return false;
	}

	@Override
	public String display() { return get().name().toLowerCase(Locale.ROOT); }

	@Override
	public String hint() {
		StringBuilder sb = new StringBuilder();
		for (E constant : type.getEnumConstants()) {
			if (sb.length() > 0) sb.append('|');
			sb.append(constant.name().toLowerCase(Locale.ROOT));
		}
		return sb.toString();
	}
}
