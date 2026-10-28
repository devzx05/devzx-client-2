package dev.devzx.client.hud;

import dev.devzx.client.module.Module;
import dev.devzx.client.module.ModuleManager;
import dev.devzx.client.render.HudRenderer;
import dev.devzx.client.theme.Theme;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Lists enabled modules, longest name first. The list is rebuilt only when a module is toggled. */
public final class ModuleListHud extends HudComponent {
	private static final int ROW_H = 11;
	private static final int ACCENT = 2;
	private static final int ACCENT_GAP = 3;

	private static final class Row {
		final String name;
		final int width;
		Row(String name, int width) { this.name = name; this.width = width; }
	}

	private final ModuleManager modules;
	private final List<Row> rows = new ArrayList<>();
	private int version = -1;
	private int maxWidth;

	public ModuleListHud(ModuleManager modules) {
		super("ModuleList", "Lists the enabled modules", Anchor.TOP_RIGHT, 4, 4, false);
		this.modules = modules;
	}

	@Override
	public void update(Minecraft mc, long nowMs) {
		if (version == Module.stateVersion()) return;
		version = Module.stateVersion();
		rows.clear();
		maxWidth = 0;
		for (Module m : modules.all()) {
			if (m == this || !m.isEnabled()) continue;
			int w = mc.font.width(m.getName());
			rows.add(new Row(m.getName(), w));
			if (w > maxWidth) maxWidth = w;
		}
		rows.sort(Comparator.<Row>comparingInt(row -> row.width).reversed().thenComparing(row -> row.name));
	}

	@Override
	protected int contentWidth() { return rows.isEmpty() ? 0 : maxWidth + ACCENT + ACCENT_GAP; }

	@Override
	protected int contentHeight() { return rows.isEmpty() ? 0 : rows.size() * ROW_H - 1; }

	@Override
	protected void draw(HudRenderer r) {
		boolean right = anchorValue().isRight();
		int total = contentWidth();
		for (int i = 0; i < rows.size(); i++) {
			Row row = rows.get(i);
			int y = i * ROW_H;
			if (right) {
				r.fill(total - ACCENT, y, total, y + ROW_H - 1, Theme.PRIMARY);
				r.text(row.name, total - ACCENT - ACCENT_GAP - row.width, y + 1);
			} else {
				r.fill(0, y, ACCENT, y + ROW_H - 1, Theme.PRIMARY);
				r.text(row.name, ACCENT + ACCENT_GAP, y + 1);
			}
		}
	}
}
