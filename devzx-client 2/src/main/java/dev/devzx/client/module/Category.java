package dev.devzx.client.module;

public enum Category {
	COMBAT("Combat"),
	MOVEMENT("Movement"),
	VISUAL("Visual"),
	HUD("HUD"),
	UTILITY("Utility");

	private final String displayName;

	Category(String displayName) { this.displayName = displayName; }

	public String getDisplayName() { return displayName; }
}
