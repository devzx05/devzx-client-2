package dev.devzx.client.hud;

import dev.devzx.client.module.Category;
import dev.devzx.client.module.Module;
import dev.devzx.client.module.setting.BooleanSetting;
import dev.devzx.client.module.setting.ColorSetting;
import dev.devzx.client.module.setting.NumberSetting;
import dev.devzx.client.theme.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/** Custom crosshair. When enabled it replaces the vanilla crosshair (see {@link HudManager#register}). */
public final class CrosshairModule extends Module {
	private static final int OUTLINE = 0xFF000000;

	private final NumberSetting size;
	private final NumberSetting gap;
	private final NumberSetting thickness;
	private final ColorSetting color;
	private final BooleanSetting dot;
	private final BooleanSetting outline;

	public CrosshairModule() {
		super("Crosshair", "Custom crosshair (size, gap, thickness, colour)", Category.VISUAL);
		size = addSetting(new NumberSetting("size", "Arm length in GUI pixels", 6, 1, 30));
		gap = addSetting(new NumberSetting("gap", "Gap around the centre", 3, 0, 20));
		thickness = addSetting(new NumberSetting("thickness", "Arm thickness", 1, 1, 6));
		color = addSetting(new ColorSetting("color", "Crosshair colour", Theme.PRIMARY));
		dot = addSetting(new BooleanSetting("dot", "Centre dot", false));
		outline = addSetting(new BooleanSetting("outline", "Dark outline for contrast", true));
	}

	public void render(GuiGraphics g, Minecraft mc) {
		if (mc.player == null || !mc.options.getCameraType().isFirstPerson()) return;
		int cx = mc.getWindow().getGuiScaledWidth() / 2;
		int cy = mc.getWindow().getGuiScaledHeight() / 2;
		int sz = size.get().intValue();
		int gp = gap.get().intValue();
		int t = thickness.get().intValue();
		int col = color.argb();

		int x0 = cx - t / 2;
		int x1 = x0 + t;
		int y0 = cy - t / 2;
		int y1 = y0 + t;

		if (outline.get()) {
			int o = 1;
			rect(g, x0 - o, y0 - gp - sz - o, x1 + o, y0 - gp + o, OUTLINE);     // top
			rect(g, x0 - o, y1 + gp - o, x1 + o, y1 + gp + sz + o, OUTLINE);     // bottom
			rect(g, x0 - gp - sz - o, y0 - o, x0 - gp + o, y1 + o, OUTLINE);    // left
			rect(g, x1 + gp - o, y0 - o, x1 + gp + sz + o, y1 + o, OUTLINE);    // right
			if (dot.get()) rect(g, x0 - o, y0 - o, x1 + o, y1 + o, OUTLINE);
		}
		rect(g, x0, y0 - gp - sz, x1, y0 - gp, col);
		rect(g, x0, y1 + gp, x1, y1 + gp + sz, col);
		rect(g, x0 - gp - sz, y0, x0 - gp, y1, col);
		rect(g, x1 + gp, y0, x1 + gp + sz, y1, col);
		if (dot.get()) rect(g, x0, y0, x1, y1, col);
	}

	private static void rect(GuiGraphics g, int x1, int y1, int x2, int y2, int argb) { g.fill(x1, y1, x2, y2, argb); }
}
