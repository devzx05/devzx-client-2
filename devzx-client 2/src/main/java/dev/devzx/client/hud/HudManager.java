package dev.devzx.client.hud;

import dev.devzx.client.DevzxClient;
import dev.devzx.client.module.ModuleManager;
import dev.devzx.client.render.HudRenderer;
import dev.devzx.client.util.ClickTracker;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.Util;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Creates, registers and lays out every HUD component. Elements sharing an
 * anchor with "stack" enabled are flowed top-to-bottom (bottom anchors: upward)
 * using their real measured size, so defaults never overlap; everything is
 * clamped on-screen so small windows / large GUI scales stay usable.
 */
public final class HudManager {
	private static final int STACK_GAP = 3;

	private final List<HudComponent> components = new ArrayList<>();
	private final CrosshairModule crosshair = new CrosshairModule();
	private final ClickTracker clicks = new ClickTracker();
	private final HudRenderer renderer = new HudRenderer();
	private final float[] stackCursor = new float[Anchor.values().length];
	private final boolean[] failed;

	public HudManager(ModuleManager modules) {
		// Registration order = stacking order within an anchor.
		add(modules, new FpsHud());
		add(modules, new CpsHud(clicks));
		add(modules, new PingHud());
		add(modules, new ClockHud());
		add(modules, new SessionHud(clicks));
		add(modules, new CoordinatesHud());
		add(modules, new DirectionHud());
		add(modules, new EffectsHud());
		add(modules, new ArmorHud());
		add(modules, new KeystrokesHud(clicks));
		add(modules, new ModuleListHud(modules));
		modules.register(crosshair);
		failed = new boolean[components.size()];
	}

	private void add(ModuleManager modules, HudComponent component) {
		components.add(component);
		modules.register(component);
	}

	public List<HudComponent> components() { return Collections.unmodifiableList(components); }
	public CrosshairModule crosshair() { return crosshair; }

	public void register() {
		HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(DevzxClient.MOD_ID, "hud"), this::render);
		HudElementRegistry.replaceElement(VanillaHudElements.CROSSHAIR, original -> (graphics, delta) -> {
			if (crosshair.isEnabled()) {
				try {
					crosshair.render(graphics, Minecraft.getInstance());
				} catch (RuntimeException e) {
					DevzxClient.LOGGER.error("Crosshair failed, disabling it", e);
					crosshair.setEnabled(false);
				}
			} else {
				original.render(graphics, delta);
			}
		});
	}

	private void render(GuiGraphics graphics, DeltaTracker delta) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return;
		long now = Util.getMillis();

		// Polled every rendered frame so clicks faster than the 20 Hz tick are still seen.
		clicks.update(mc.options.keyAttack.isDown(), mc.options.keyUse.isDown(), now);

		renderer.begin(graphics, mc.font);
		int guiW = mc.getWindow().getGuiScaledWidth();
		int guiH = mc.getWindow().getGuiScaledHeight();
		Arrays.fill(stackCursor, 0f);

		for (int i = 0; i < components.size(); i++) {
			HudComponent c = components.get(i);
			if (!c.isEnabled() || failed[i]) continue;
			try {
				c.update(mc, now);
				if (!c.hasContent()) continue;
				drawComponent(c, guiW, guiH);
			} catch (RuntimeException e) {
				// One broken element must never take the whole client down.
				failed[i] = true;
				DevzxClient.LOGGER.error("HUD component {} failed and was disabled for this session", c.getName(), e);
			}
		}
	}

	private void drawComponent(HudComponent c, int guiW, int guiH) {
		Anchor a = c.anchorValue();
		float s = c.scaleValue();
		int bw = Math.round(c.boxWidth() * s);
		int bh = Math.round(c.boxHeight() * s);
		float ox = c.offsetXValue();
		float oy = c.offsetYValue();

		float x = a.fx * (guiW - bw) + (a.isRight() ? -ox : ox);
		float y = a.fy * (guiH - bh) + (a.isBottom() ? -oy : oy);

		if (c.stacks()) {
			int slot = a.ordinal();
			float cursor = stackCursor[slot];
			y += a.isBottom() ? -cursor : cursor;
			stackCursor[slot] = cursor + bh + STACK_GAP;
		}

		int px = Math.round(Math.max(0f, Math.min(x, Math.max(0, guiW - bw))));
		int py = Math.round(Math.max(0f, Math.min(y, Math.max(0, guiH - bh))));
		c.render(renderer, px, py);
	}
}
