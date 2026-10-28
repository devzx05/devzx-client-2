package dev.devzx.client.module;

import dev.devzx.client.module.setting.Setting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base class for every devzx Client feature. Subclasses declare their
 * settings in the constructor via {@link #addSetting(Setting)} and react to
 * {@link #onEnable()} / {@link #onDisable()}.
 */
public abstract class Module {
	private final String name;
	private final String description;
	private final Category category;
	private final List<Setting<?>> settings = new ArrayList<>();
	private static int stateVersion;
	private boolean enabled;

	protected Module(String name, String description, Category category) {
		this.name = name;
		this.description = description;
		this.category = category;
	}

	public String getName() { return name; }
	public String getDescription() { return description; }
	public Category getCategory() { return category; }
	public boolean isEnabled() { return enabled; }
	public List<Setting<?>> getSettings() { return Collections.unmodifiableList(settings); }

	protected <S extends Setting<?>> S addSetting(S setting) {
		settings.add(setting);
		return setting;
	}

	/** Incremented whenever any module is enabled/disabled; lets UI cache derived lists. */
	public static int stateVersion() { return stateVersion; }

	public void toggle() { setEnabled(!enabled); }

	public void setEnabled(boolean value) {
		if (value == enabled) return;
		enabled = value;
		stateVersion++;
		if (enabled) onEnable(); else onDisable();
	}

	protected void onEnable() {}
	protected void onDisable() {}
}
