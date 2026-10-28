package dev.devzx.client.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Registry of all modules. Registration order is preserved. */
public final class ModuleManager {
	private final Map<String, Module> modules = new LinkedHashMap<>();

	public void register(Module module) {
		String key = module.getName().toLowerCase(Locale.ROOT);
		if (modules.putIfAbsent(key, module) != null) {
			throw new IllegalStateException("Duplicate module name: " + module.getName());
		}
	}

	public Module get(String name) {
		return name == null ? null : modules.get(name.toLowerCase(Locale.ROOT));
	}

	public List<Module> all() {
		return Collections.unmodifiableList(new ArrayList<>(modules.values()));
	}

	public List<Module> byCategory(Category category) {
		List<Module> out = new ArrayList<>();
		for (Module m : modules.values()) {
			if (m.getCategory() == category) out.add(m);
		}
		return out;
	}
}
