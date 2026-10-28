package dev.devzx.client.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.devzx.client.config.ConfigManager;
import dev.devzx.client.module.Module;
import dev.devzx.client.module.ModuleManager;
import dev.devzx.client.module.setting.Setting;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

/**
 * Client-side /devzx command: the configuration interface until the click GUI exists.
 * /devzx list | toggle &lt;module&gt; | info &lt;module&gt; | set &lt;module&gt; &lt;setting&gt; &lt;value&gt;
 */
public final class DevzxCommands {
	private static final String PREFIX = "[devzx] ";

	private DevzxCommands() {}

	public static void register(ModuleManager modules, ConfigManager config) {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
			dispatcher.register(ClientCommandManager.literal("devzx")
				.executes(ctx -> help(ctx))
				.then(ClientCommandManager.literal("list").executes(ctx -> list(ctx, modules)))
				.then(ClientCommandManager.literal("toggle")
					.then(ClientCommandManager.argument("module", StringArgumentType.word())
						.executes(ctx -> toggle(ctx, modules, config))))
				.then(ClientCommandManager.literal("info")
					.then(ClientCommandManager.argument("module", StringArgumentType.word())
						.executes(ctx -> info(ctx, modules))))
				.then(ClientCommandManager.literal("set")
					.then(ClientCommandManager.argument("module", StringArgumentType.word())
						.then(ClientCommandManager.argument("setting", StringArgumentType.word())
							.then(ClientCommandManager.argument("value", StringArgumentType.greedyString())
								.executes(ctx -> set(ctx, modules, config))))))));
	}

	private static void say(CommandContext<FabricClientCommandSource> ctx, String message) {
		ctx.getSource().sendFeedback(Component.literal(PREFIX + message));
	}

	private static int help(CommandContext<FabricClientCommandSource> ctx) {
		say(ctx, "/devzx list | toggle <module> | info <module> | set <module> <setting> <value>");
		return 1;
	}

	private static int list(CommandContext<FabricClientCommandSource> ctx, ModuleManager modules) {
		StringBuilder sb = new StringBuilder();
		for (Module m : modules.all()) {
			if (sb.length() > 0) sb.append(", ");
			sb.append(m.getName()).append(m.isEnabled() ? " [on]" : " [off]");
		}
		say(ctx, sb.toString());
		return 1;
	}

	private static Module find(CommandContext<FabricClientCommandSource> ctx, ModuleManager modules) {
		String name = StringArgumentType.getString(ctx, "module");
		Module m = modules.get(name);
		if (m == null) say(ctx, "Unknown module '" + name + "'. Use /devzx list.");
		return m;
	}

	private static int toggle(CommandContext<FabricClientCommandSource> ctx, ModuleManager modules, ConfigManager config) {
		Module m = find(ctx, modules);
		if (m == null) return 0;
		m.toggle();
		config.save();
		say(ctx, m.getName() + " is now " + (m.isEnabled() ? "enabled" : "disabled"));
		return 1;
	}

	private static int info(CommandContext<FabricClientCommandSource> ctx, ModuleManager modules) {
		Module m = find(ctx, modules);
		if (m == null) return 0;
		say(ctx, m.getName() + " (" + (m.isEnabled() ? "on" : "off") + "): " + m.getDescription());
		for (Setting<?> s : m.getSettings()) {
			say(ctx, "  " + s.getName() + " = " + s.display() + "  [" + s.hint() + "]  " + s.getDescription());
		}
		return 1;
	}

	private static int set(CommandContext<FabricClientCommandSource> ctx, ModuleManager modules, ConfigManager config) {
		Module m = find(ctx, modules);
		if (m == null) return 0;
		String settingName = StringArgumentType.getString(ctx, "setting");
		String value = StringArgumentType.getString(ctx, "value");
		for (Setting<?> s : m.getSettings()) {
			if (s.getName().equalsIgnoreCase(settingName)) {
				if (!s.parse(value)) {
					say(ctx, "Invalid value '" + value + "' for " + s.getName() + " (expected " + s.hint() + ")");
					return 0;
				}
				config.save();
				say(ctx, m.getName() + "." + s.getName() + " = " + s.display());
				return 1;
			}
		}
		say(ctx, "Unknown setting '" + settingName + "'. Use /devzx info " + m.getName());
		return 0;
	}
}
