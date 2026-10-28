package dev.devzx.client.module.setting;

import com.google.gson.JsonElement;

/** A named, persisted value belonging to a module. */
public abstract class Setting<T> {
	private final String name;
	private final String description;
	protected T value;
	private final T defaultValue;

	protected Setting(String name, String description, T defaultValue) {
		this.name = name;
		this.description = description;
		this.defaultValue = defaultValue;
		this.value = defaultValue;
	}

	public String getName() { return name; }
	public String getDescription() { return description; }
	public T get() { return value; }
	public T getDefault() { return defaultValue; }
	public void set(T newValue) { this.value = sanitize(newValue); }
	public void reset() { this.value = defaultValue; }

	/** Hook for subclasses to clamp / validate. */
	protected T sanitize(T newValue) { return newValue; }

	public abstract JsonElement toJson();

	/** Loads from JSON. Must ignore malformed input and keep the current value. */
	public abstract void fromJson(JsonElement element);

	/** Parses user text (e.g. from a command). Returns false and keeps the value if invalid. */
	public abstract boolean parse(String text);

	/** Human-readable current value. */
	public String display() { return String.valueOf(value); }

	/** Short description of accepted values, e.g. "true|false". */
	public String hint() { return ""; }
}
