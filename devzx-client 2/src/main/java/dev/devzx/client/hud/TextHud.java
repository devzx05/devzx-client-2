package dev.devzx.client.hud;

import dev.devzx.client.module.setting.BooleanSetting;
import dev.devzx.client.render.HudRenderer;
import net.minecraft.client.gui.Font;

import java.util.Arrays;

/**
 * Base for simple "label: value" HUD elements. Subclasses push text with
 * {@link #setLine}; widths are re-measured only when a string actually
 * changes, so steady-state frames do no string work.
 */
public abstract class TextHud extends HudComponent {
	private static final int LABEL_GAP = 4;

	protected final BooleanSetting showLabel;
	private String[] labels = new String[4];
	private String[] values = new String[4];
	private int[] labelW = new int[4];
	private int[] valueW = new int[4];
	private int lineCount;

	protected TextHud(String name, String description, Anchor anchor, int offX, int offY) {
		super(name, description, anchor, offX, offY, true);
		showLabel = addSetting(new BooleanSetting("label", "Show the label before the value", true));
	}

	protected final void setLineCount(int count) { lineCount = count; }

	protected final void setLine(Font font, int index, String label, String value) {
		if (index >= labels.length) {
			int n = Math.max(index + 1, labels.length * 2);
			labels = Arrays.copyOf(labels, n);
			values = Arrays.copyOf(values, n);
			labelW = Arrays.copyOf(labelW, n);
			valueW = Arrays.copyOf(valueW, n);
		}
		if (!label.equals(labels[index])) {
			labels[index] = label;
			labelW[index] = label.isEmpty() ? 0 : font.width(label);
		}
		if (!value.equals(values[index])) {
			values[index] = value;
			valueW[index] = font.width(value);
		}
	}

	@Override
	protected final int contentWidth() {
		boolean withLabel = showLabel.get();
		int max = 0;
		for (int i = 0; i < lineCount; i++) {
			int w = valueW[i] + (withLabel && labelW[i] > 0 ? labelW[i] + LABEL_GAP : 0);
			if (w > max) max = w;
		}
		return max;
	}

	@Override
	protected final int contentHeight() { return lineCount * HudRenderer.LINE_HEIGHT - (lineCount > 0 ? 1 : 0); }

	@Override
	protected final void draw(HudRenderer r) {
		boolean withLabel = showLabel.get();
		for (int i = 0; i < lineCount; i++) {
			int y = i * HudRenderer.LINE_HEIGHT;
			int x = 0;
			if (withLabel && labelW[i] > 0) {
				r.secondary(labels[i], 0, y);
				x = labelW[i] + LABEL_GAP;
			}
			r.text(values[i], x, y);
		}
	}
}
