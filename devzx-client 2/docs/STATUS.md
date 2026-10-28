# devzx Client - status

Target: Minecraft 1.21.11, Fabric (Loom remap plugin), Mojang mappings, Java 21.

## What "verified" means here
The authoring sandbox has no Gradle, no network and no real Minecraft jars, so the project has NEVER been built
with Gradle and NO JAR EXISTS. What was done instead:
1. All sources were compiled with javac against hand-written stubs of the Minecraft/Fabric/Gson APIs
   (this proves internal consistency, NOT that the real APIs match the stubs).
2. `tools/SelfTest.java` (63 checks) exercises the pure-Java logic: ClickTracker, Compass, TextUtil, Theme,
   settings parsing/clamping, module registry. All pass.
Nothing has been run inside Minecraft.

## Stage 1 (unchanged, still present)
Gradle build, fabric.mod.json, DevzxClient, Theme, Module/Category/ModuleManager, settings, ConfigManager.

## Stage 2: HUD (written, compile-checked against stubs only)
FPS, CPS, Ping, Keystrokes, ArmorHUD, Coordinates, Direction, Effects, Clock, ModuleList, Crosshair, Session.
Shared: HudComponent (anchor/x/y/scale/color/background/shadow/stack settings), TextHud, HudRenderer, HudManager
(anchor stacking + on-screen clamping), /devzx command (list | toggle | info | set), ColorSetting, EnumSetting.
Default ON: FPS, CPS, Ping, Keystrokes, ArmorHUD, Coordinates, Effects. Default OFF: Direction, Clock, Session, ModuleList, Crosshair.

## Minecraft API calls that are NOT verified (check these first when the real build fails)
Confirmed in Fabric's 1.21.11 docs: HudElementRegistry.addLast, Identifier.fromNamespaceAndPath, GuiGraphics.fill /
renderOutline / drawString(font, String, x, y, argb, shadow), graphics.pose() as Matrix3x2fStack
(pushMatrix/popMatrix/translate(x,y)/scale(x,y)), Util.getMillis, minecraft.font, ClientCommandManager +
ClientCommandRegistrationCallback + sendFeedback, Component.literal.
Written from memory, unconfirmed:
- HudElementRegistry.replaceElement(Identifier, Function<HudElement,HudElement>) and VanillaHudElements.CROSSHAIR
- GuiGraphics.renderItem(ItemStack, int, int)
- Minecraft: getConnection(), getCurrentServer(), getWindow().getGuiScaledWidth()/Height(), player, level
- ClientPacketListener.getPlayerInfo(UUID), PlayerInfo.getLatency()
- Options fields keyUp/keyDown/keyLeft/keyRight/keyJump/keyShift/keySprint/keyAttack/keyUse, getCameraType().isFirstPerson()
- LocalPlayer: getX/Y/Z, getYRot, getUUID, getItemBySlot(EquipmentSlot), getActiveEffects
- MobEffectInstance: getEffect().value().getDisplayName(), getDuration, getAmplifier, isInfiniteDuration
- ItemStack: isEmpty, isDamageableItem, getDamageValue, getMaxDamage; Font.width(String)
- Package names of the above (e.g. net.minecraft.resources.Identifier, net.minecraft.world.effect.MobEffectInstance)
Version pins also unverified: loom 1.17-SNAPSHOT, loader 0.19.3, Gradle 9.2.1 (no wrapper jar included).

## Known limitations / design notes
- No click GUI or drag-to-move yet: configure with /devzx or by editing config/devzx-client.json.
- CPS and Keystrokes read key state once per rendered frame (not mouse events): clicks shorter than one frame are missed,
  and nothing is tracked while the HUD is hidden (F1).
- FPS is measured by counting HUD frames over 500 ms, not read from Minecraft.
- Ping: "--" = no measurement yet (vanilla reports 0), "N/A" = no remote server (singleplayer/LAN host).
- Keystroke labels are fixed text (W A S D LMB RMB Sprint Sneak Space), not the user's rebound keys.
- Effects HUD is text only (no icons). Session HUD has no kills/deaths (not available client-side).
- A HUD component that throws is disabled for the session and logged, instead of crashing the game.

## Not started
Click GUI / HUD editor screen, keybinds, PvP gameplay modules, mixins, config UI, further utils.
