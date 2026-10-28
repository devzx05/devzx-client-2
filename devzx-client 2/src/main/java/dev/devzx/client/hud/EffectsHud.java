package dev.devzx.client.hud;

import dev.devzx.client.module.setting.NumberSetting;
import dev.devzx.client.render.HudRenderer;
import dev.devzx.client.util.TextUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Active potion effects with remaining time. Rebuilt 4x per second, not per frame. */
public final class EffectsHud extends HudComponent {
	private static final int ROWS_MAX = 12;
	private static final long REFRESH_MS = 250;
	private static final int COLUMN_GAP = 8;
	private static final int URGENT_COLOR = 0xFFFF5555;
	private static final int URGENT_TICKS = 200; // 10 s

	private final NumberSetting maxRows;
	private final List<MobEffectInstance> scratch = new ArrayList<>();
	private final String[] names = new String[ROWS_MAX];
	private final String[] times = new String[ROWS_MAX];
	private final boolean[] urgent = new boolean[ROWS_MAX];
	private final int[] nameW = new int[ROWS_MAX];
	private final int[] timeW = new int[ROWS_MAX];
	private int rows;
	private int nameCol;
	private int timeCol;
	private long nextRefresh;

	public EffectsHud() {
		super("Effects", "Active potion effects and remaining time", Anchor.MIDDLE_LEFT, 4, 0, true);
		maxRows = addSetting(new NumberSetting("max", "Maximum effects shown", 6, 1, ROWS_MAX));
		setEnabled(true);
	}

	@Override
	public void update(Minecraft mc, long nowMs) {
		if (mc.player == null) return;
		if (nowMs < nextRefresh) return;
		nextRefresh = nowMs + REFRESH_MS;

		scratch.clear();
		scratch.addAll(mc.player.getActiveEffects());
		scratch.sort(Comparator.comparingInt(EffectsHud::sortKey).reversed());

		int limit = Math.min(maxRows.get().intValue(), Math.min(ROWS_MAX, scratch.size()));
		int nc = 0;
		int tc = 0;
		for (int i = 0; i < limit; i++) {
			MobEffectInstance inst = scratch.get(i);
			String name = inst.getEffect().value().getDisplayName().getString();
			if (inst.getAmplifier() > 0) name = name + " " + TextUtil.roman(inst.getAmplifier() + 1);
			names[i] = name;
			nameW[i] = mc.font.width(name);
			if (inst.isInfiniteDuration()) {
				times[i] = "**";
				urgent[i] = false;
			} else {
				times[i] = TextUtil.duration(inst.getDuration() / 20);
				urgent[i] = inst.getDuration() <= URGENT_TICKS;
			}
			timeW[i] = mc.font.width(times[i]);
			if (nameW[i] > nc) nc = nameW[i];
			if (timeW[i] > tc) tc = timeW[i];
		}
		rows = limit;
		nameCol = nc;
		timeCol = tc;
	}

	private static int sortKey(MobEffectInstance e) { return e.isInfiniteDuration() ? Integer.MAX_VALUE : e.getDuration(); }

	@Override
	protected int contentWidth() { return rows == 0 ? 0 : nameCol + COLUMN_GAP + timeCol; }

	@Override
	protected int contentHeight() { return rows == 0 ? 0 : rows * HudRenderer.LINE_HEIGHT - 1; }

	@Override
	protected void draw(HudRenderer r) {
		int total = nameCol + COLUMN_GAP + timeCol;
		for (int i = 0; i < rows; i++) {
			int y = i * HudRenderer.LINE_HEIGHT;
			r.text(names[i], 0, y);
			int tx = total - timeW[i];
			if (urgent[i]) r.text(times[i], tx, y, URGENT_COLOR);
			else r.secondary(times[i], tx, y);
		}
	}
}
