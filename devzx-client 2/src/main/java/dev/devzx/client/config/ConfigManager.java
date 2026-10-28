package dev.devzx.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.devzx.client.module.Module;
import dev.devzx.client.module.ModuleManager;
import dev.devzx.client.module.setting.Setting;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Persists module enabled-state and setting values to
 * {@code <config dir>/devzx-client.json}. Loading is tolerant: unknown or
 * malformed entries are skipped and defaults are kept.
 */
public final class ConfigManager {
	private static final Logger LOGGER = LoggerFactory.getLogger("devzx-client/config");
	private static final int FORMAT_VERSION = 1;
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private final Path file;
	private final ModuleManager modules;

	public ConfigManager(ModuleManager modules) {
		this(FabricLoader.getInstance().getConfigDir().resolve("devzx-client.json"), modules);
	}

	public ConfigManager(Path file, ModuleManager modules) {
		this.file = file;
		this.modules = modules;
	}

	public Path getFile() { return file; }

	public void load() {
		if (!Files.isRegularFile(file)) {
			LOGGER.info("No config at {}, using defaults", file);
			return;
		}
		try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
			JsonElement parsed = JsonParser.parseReader(reader);
			if (!parsed.isJsonObject()) {
				LOGGER.warn("Config root is not an object, ignoring");
				return;
			}
			JsonObject modulesJson = parsed.getAsJsonObject().getAsJsonObject("modules");
			if (modulesJson == null) return;
			for (Module module : modules.all()) {
				JsonElement entry = modulesJson.get(module.getName());
				if (entry != null && entry.isJsonObject()) applyModule(module, entry.getAsJsonObject());
			}
		} catch (IOException | RuntimeException e) {
			// RuntimeException covers JsonParseException / ClassCastException on corrupt files.
			LOGGER.error("Failed to read config {}, using defaults", file, e);
		}
	}

	public void save() {
		JsonObject root = new JsonObject();
		root.addProperty("formatVersion", FORMAT_VERSION);
		JsonObject modulesJson = new JsonObject();
		for (Module module : modules.all()) {
			JsonObject m = new JsonObject();
			m.addProperty("enabled", module.isEnabled());
			JsonObject settingsJson = new JsonObject();
			for (Setting<?> s : module.getSettings()) settingsJson.add(s.getName(), s.toJson());
			m.add("settings", settingsJson);
			modulesJson.add(module.getName(), m);
		}
		root.add("modules", modulesJson);

		Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
		try {
			Files.createDirectories(file.getParent());
			try (Writer writer = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {
				GSON.toJson(root, writer);
			}
			try {
				Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
			} catch (java.nio.file.AtomicMoveNotSupportedException e) {
				Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (IOException e) {
			LOGGER.error("Failed to write config {}", file, e);
		}
	}

	private static void applyModule(Module module, JsonObject json) {
		JsonElement enabled = json.get("enabled");
		if (enabled != null && enabled.isJsonPrimitive() && enabled.getAsJsonPrimitive().isBoolean()) {
			module.setEnabled(enabled.getAsBoolean());
		}
		JsonObject settingsJson = json.has("settings") && json.get("settings").isJsonObject()
				? json.getAsJsonObject("settings") : null;
		if (settingsJson == null) return;
		for (Setting<?> s : module.getSettings()) {
			s.fromJson(settingsJson.get(s.getName()));
		}
	}
}
