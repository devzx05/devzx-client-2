package dev.devzx.client.render;

import dev.devzx.client.theme.Theme;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;

/**
 * Thin reusable wrapper over {@link GuiGraphics} for HUD drawing. One shared
 * instance is re-targeted each frame with {@link #begin}, so nothing is
 * allocated per frame. All coordinates are GUI-scaled pixels.
 */
public final class HudRenderer {
	/** Vertical pitch of one text line (font height 9 + 1px spacing). */
	public static final int LINE_HEIGHT = 10;

	private GuiGraphics graphics;
	private Font font;
	private boolean shadow = true;
	private int color = Theme.TEXT;

	public void begin(GuiGraphics graphics, Font font) {
		this.graphics = graphics;
		this.font = font;
	}

	/** Sets the default text colour (ARGB) and shadow flag used by {@link #text(String, int, int)}. */
	public void style(int argb, boolean shadow) {
		this.color = argb;
		this.shadow = shadow;
	}

	public Matrix3x2fStack pose() { return graphics.pose(); }
	public int width(String s) { return font.width(s); }

	public void text(String s, int x, int y) { graphics.drawString(font, s, x, y, color, shadow); }
	public void text(String s, int x, int y, int argb) { graphics.drawString(font, s, x, y, argb, shadow); }
	public void secondary(String s, int x, int y) { graphics.drawString(font, s, x, y, Theme.TEXT_SECONDARY, shadow); }

	public void fill(int x1, int y1, int x2, int y2, int argb) { graphics.fill(x1, y1, x2, y2, argb); }
	public void outline(int x, int y, int w, int h, int argb) { graphics.renderOutline(x, y, w, h, argb); }

	/** Standard devzx panel: translucent dark fill with a 1px orange accent on the left edge. */
	public void panel(int x, int y, int w, int h) {
		graphics.fill(x, y, x + w, y + h, Theme.withAlpha(Theme.PANEL, 0xC8));
		graphics.fill(x, y, x + 1, y + h, Theme.PRIMARY);
	}

	public void item(ItemStack stack, int x, int y) { graphics.renderItem(stack, x, y); }
}
