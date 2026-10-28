package dev.devzx.client.hud;

import dev.devzx.client.module.Category;
import dev.devzx.client.module.Module;
import dev.devzx.client.module.setting.BooleanSetting;
import dev.devzx.client.module.setting.ColorSetting;
import dev.devzx.client.module.setting.EnumSetting;
import dev.devzx.client.module.setting.NumberSetting;
import dev.devzx.client.render.HudRenderer;
import dev.devzx.client.theme.Theme;
import net.minecraft.client.Minecraft;
import org.joml.Matrix3x2fStack;

/**
 * A positioned, scalable, recolourable HUD element. Subclasses cache their
 * state in {@link #update} (cheap, called once per frame while enabled) and
 * draw in local coordinates in {@link #draw}. {@link HudManager} owns layout.
 */
public abstract class HudComponent extends Module {
	/** Inner padding when the background panel is shown. */
	public static final int PAD = 3;

	protected final EnumSetting<Anchor> anchor;
	protected final NumberSetting offsetX;
	protected final NumberSetting offsetY;
	protected final NumberSetting scale;
	protected final ColorSetting color;
	protected final BooleanSetting background;
	protected final BooleanSetting shadow;
	protected final BooleanSetting stack;

	protected HudComponent(String name, String description, Anchor defaultAnchor, int defaultOffsetX, int defaultOffsetY, boolean defaultBackground) {
		super(name, description, Category.HUD);
		anchor = addSetting(new EnumSetting<>("anchor", "Screen anchor point", Anchor.class, defaultAnchor));
		offsetX = addSetting(new NumberSetting("x", "Horizontal offset from the anchor (GUI pixels, inward)", defaultOffsetX, -2000, 2000));
		offsetY = addSetting(new NumberSetting("y", "Vertical offset from the anchor (GUI pixels, inward)", defaultOffsetY, -2000, 2000));
		scale = addSetting(new NumberSetting("scale", "Size multiplier", 1.0, 0.5, 3.0));
		color = addSetting(new ColorSetting("color", "Main colour", Theme.PRIMARY));
		background = addSetting(new BooleanSetting("background", "Draw a background panel", defaultBackground));
		shadow = addSetting(new BooleanSetting("shadow", "Draw text shadow", true));
		stack = addSetting(new BooleanSetting("stack", "Stack below/above other stacked elements at the same anchor", true));
	}

	/** Refresh cached state. Must be cheap and allocation-free in the steady state. */
	public abstract void update(Minecraft mc, long nowMs);

	/** Unscaled content size, excluding background padding. */
	protected abstract int contentWidth();
	protected abstract int contentHeight();

	/** Draw at local (0,0); the matrix stack is already translated and scaled. */
	protected abstract void draw(HudRenderer r);

	/** False when there is nothing to show (the element is skipped and takes no space). */
	public boolean hasContent() { return contentWidth() > 0 && contentHeight() > 0; }

	public final int boxWidth() { return contentWidth() + (background.get() ? PAD * 2 : 0); }
	public final int boxHeight() { return contentHeight() + (background.get() ? PAD * 2 : 0); }

	public final Anchor anchorValue() { return anchor.get(); }
	public final float scaleValue() { return scale.get().floatValue(); }
	public final float offsetXValue() { return offsetX.get().floatValue(); }
	public final float offsetYValue() { return offsetY.get().floatValue(); }
	public final boolean stacks() { return stack.get(); }
	public final int colorArgb() { return color.argb(); }

	final void render(HudRenderer r, int x, int y) {
		Matrix3x2fStack pose = r.pose();
		float s = scaleValue();
		pose.pushMatrix();
		try {
			pose.translate(x, y);
			pose.scale(s, s);
			r.style(color.argb(), shadow.get());
			if (background.get()) {
				r.panel(0, 0, boxWidth(), boxHeight());
				pose.translate(PAD, PAD);
			}
			draw(r);
		} finally {
			pose.popMatrix();
		}
	}
}
