package dev.devzx.client;

import dev.devzx.client.command.DevzxCommands;
import dev.devzx.client.config.ConfigManager;
import dev.devzx.client.hud.HudManager;
import dev.devzx.client.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DevzxClient implements ClientModInitializer {
	public static final String MOD_ID = "devzx-client";
	public static final String NAME = "devzx Client";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static DevzxClient instance;

	private ModuleManager moduleManager;
	private ConfigManager configManager;
	private HudManager hudManager;

	public static DevzxClient get() { return instance; }
	public ModuleManager modules() { return moduleManager; }
	public ConfigManager config() { return configManager; }
	public HudManager hud() { return hudManager; }

	@Override
	public void onInitializeClient() {
		instance = this;
		moduleManager = new ModuleManager();
		// Modules must be registered BEFORE the config is loaded.
		hudManager = new HudManager(moduleManager);
		configManager = new ConfigManager(moduleManager);
		configManager.load();
		hudManager.register();
		DevzxCommands.register(moduleManager, configManager);
		Runtime.getRuntime().addShutdownHook(new Thread(configManager::save, "devzx-config-save"));
		LOGGER.info("{} initialised ({} modules)", NAME, moduleManager.all().size());
	}
}
