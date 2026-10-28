package dev.devzx.client.hud;

import dev.devzx.client.module.setting.BooleanSetting;
import dev.devzx.client.render.HudRenderer;
import dev.devzx.client.theme.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/** Equipped armor icons with durability bars and optional percentages. */
public final class ArmorHud extends HudComponent {
	private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
	private static final int ICON = 16;
	private static final int PITCH = 18;
	private static final int BAR_W = 13;
	private static final int LOW_COLOR = 0xFFFF3B30;
	private static final int FULL_COLOR = 0xFF4CD964;

	private final BooleanSetting bar;
	private final BooleanSetting percent;
	private final BooleanSetting horizontal;

	private final ItemStack[] stacks = new ItemStack[4];
	private final float[] fraction = new float[4]; // -1 = not damageable
	private final String[] percentText = new String[4];
	private final int[] percentW = new int[4];
	private final int[] lastPercent = {-2, -2, -2, -2};
	private int widest100 = -1;

	public ArmorHud() {
		super("ArmorHUD", "Equipped armor with durability", Anchor.MIDDLE_RIGHT, 4, 0, false);
		bar = addSetting(new BooleanSetting("bar", "Show durability bars", true));
		percent = addSetting(new BooleanSetting("percent", "Show durability percentages", false));
		horizontal = addSetting(new BooleanSetting("horizontal", "Lay out in a row instead of a column", false));
		for (int i = 0; i < 4; i++) { stacks[i] = ItemStack.EMPTY; fraction[i] = -1f; percentText[i] = ""; }
		setEnabled(true);
	}

	@Override
	public void update(Minecraft mc, long nowMs) {
		if (mc.player == null) return;
		if (widest100 < 0) widest100 = mc.font.width("100%");
		for (int i = 0; i < 4; i++) {
			ItemStack stack = mc.player.getItemBySlot(SLOTS[i]);
			stacks[i] = stack;
			if (!stack.isEmpty() && stack.isDamageableItem() && stack.getMaxDamage() > 0) {
				int max = stack.getMaxDamage();
				fraction[i] = Math.max(0f, Math.min(1f, (max - stack.getDamageValue()) / (float) max));
				int pct = Math.round(fraction[i] * 100f);
				if (pct != lastPercent[i]) {
					lastPercent[i] = pct;
					percentText[i] = pct + "%";
					percentW[i] = mc.font.width(percentText[i]);
				}
			} else {
				fraction[i] = -1f;
				lastPercent[i] = -2;
			}
		}
	}

	private int pitchX() { return horizontal.get() ? Math.max(PITCH, percent.get() ? widest100 + 2 : PITCH) : 0; }

	@Override
	protected int contentWidth() {
		if (widest100 < 0) return 0;
		if (horizontal.get()) return pitchX() * 4 - (pitchX() - ICON);
		return ICON + (percent.get() ? 3 + widest100 : 0);
	}

	@Override
	protected int contentHeight() {
		if (widest100 < 0) return 0;
		if (horizontal.get()) return ICON + (percent.get() ? HudRenderer.LINE_HEIGHT + 1 : 0);
		return PITCH * 4 - (PITCH - ICON);
	}

	@Override
	protected void draw(HudRenderer r) {
		boolean row = horizontal.get();
		int px = pitchX();
		for (int i = 0; i < 4; i++) {
			ItemStack stack = stacks[i];
			if (stack.isEmpty()) continue;
			int x = row ? i * px + (px - ICON) / 2 : 0;
			int y = row ? 0 : i * PITCH;
			r.item(stack, x, y);

			float f = fraction[i];
			if (f < 0f) continue;
			int barColor = Theme.lerp(LOW_COLOR, FULL_COLOR, f);
			if (bar.get()) {
				int bx = x + (ICON - BAR_W) / 2;
				int by = y + ICON - 3;
				r.fill(bx, by, bx + BAR_W, by + 2, 0xFF000000);
				r.fill(bx, by, bx + Math.max(1, Math.round(BAR_W * f)), by + 1, barColor);
			}
			if (percent.get()) {
				if (row) r.text(percentText[i], x + (ICON - percentW[i]) / 2, ICON + 2, barColor);
				else r.text(percentText[i], ICON + 3, y + 4, barColor);
			}
		}
	}
}
