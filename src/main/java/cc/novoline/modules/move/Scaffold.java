package cc.novoline.modules.move;

import cc.novoline.events.EventTarget;
import cc.novoline.events.events.*;
import cc.novoline.gui.screen.setting.Manager;
import cc.novoline.gui.screen.setting.Setting;
import cc.novoline.gui.screen.setting.SettingType;
import cc.novoline.modules.AbstractModule;
import cc.novoline.modules.EnumModuleType;
import cc.novoline.modules.ModuleManager;
import cc.novoline.modules.configurations.annotation.Property;
import cc.novoline.modules.configurations.property.object.*;
import cc.novoline.utils.Timer;
import cc.novoline.utils.*;
import cc.novoline.utils.fonts.impl.Fonts;
import cc.novoline.utils.notifications.NotificationType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSnow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition;
import net.minecraft.potion.Potion;
import net.minecraft.util.*;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static cc.novoline.modules.configurations.property.object.PropertyFactory.createColor;

public final class Scaffold extends AbstractModule {

    private List<Block> invalid, blacklistedBlocks;

    /* fields */
    private final Timer timer = new Timer();
    private BlockData blockData;
    private BlockPos blockBelow;
    private int floorY, oldSlot, currentSlot, groundTick;
    private float yaw, pitch;
    private boolean onDisable;
    private List<BlockPos> blockPosList = new CopyOnWriteArrayList();

    /* properties @off */
    @Property("addons")
    private final ListProperty<String> addons = PropertyFactory.createList("Swapper", "Silent", "Down Ward").acceptableValues("Swing Item", "Safe Walk", "Swapper", "Silent", "Tower", "Down Ward");
    @Property("render-rotations")
    private BooleanProperty render_rotations = PropertyFactory.createBoolean(true);
    /*    @Property("expand-dist")
        private final DoubleProperty expand_dist = PropertyFactory.createDouble(4.0).minimum(4.0).maximum(9.0);*/
    @Property("slot")
    private final IntProperty slot = PropertyFactory.createInt(6).minimum(1).maximum(9);
    @Property("path")
    private final BooleanProperty path = PropertyFactory.createBoolean(true);
    @Property("color")
    private final ColorProperty color = createColor(new Color(152, 217, 0).getRGB());
    @Property("no-sprint")
    private final BooleanProperty no_sprint = PropertyFactory.createBoolean(false);
/*    @Property("timer-boost")
    private final FloatProperty timer_boost = PropertyFactory.createFloat(1.0F).minimum(1.0F).maximum(3.0F);*/

    /* constructors @on */
    public Scaffold(@NonNull ModuleManager moduleManager) {
        super(moduleManager, "Scaffold", "Scaffold", Keyboard.KEY_NONE, EnumModuleType.MOVEMENT);
        Manager.put(new Setting("SF_ADDONS", "Addons", SettingType.SELECTBOX, this, addons));
        Manager.put(new Setting("SF_NO_SPRINT", "No Sprint", SettingType.CHECKBOX, this, no_sprint));
        Manager.put(new Setting("SF_RENDER_ROTS", "Render Rotations", SettingType.CHECKBOX, this, render_rotations));
        Manager.put(new Setting("SF_PATH", "Render Path", SettingType.CHECKBOX, this, path));
        Manager.put(new Setting("SF_PATH_COLOR", "Path Color", SettingType.COLOR_PICKER, this, color, null, path::get));
        //    Manager.put(new Setting("SF_TIMER_BOOST", "Timer Boost", SettingType.SLIDER, this, timer_boost, 0.1F, no_sprint::get));
        Manager.put(new Setting("SF_SLOT", "Swap Slot", SettingType.SLIDER, this, slot, 1));

        invalid = Arrays.asList(Blocks.anvil, Blocks.air, Blocks.water, Blocks.fire, Blocks.flowing_water, Blocks.lava, Blocks.skull, Blocks.trapped_chest, Blocks.flowing_lava, // @off
                Blocks.chest, Blocks.enchanting_table, Blocks.ender_chest, Blocks.crafting_table);
        blacklistedBlocks = Arrays.asList(Blocks.air, Blocks.water, Blocks.flowing_water, Blocks.lava, Blocks.wooden_slab, Blocks.wooden_slab, Blocks.chest, Blocks.flowing_lava,
                Blocks.enchanting_table, Blocks.carpet, Blocks.glass_pane, Blocks.skull, Blocks.stained_glass_pane, Blocks.iron_bars, Blocks.snow_layer, Blocks.ice, Blocks.packed_ice,
                Blocks.coal_ore, Blocks.diamond_ore, Blocks.emerald_ore, Blocks.chest, Blocks.trapped_chest, Blocks.tnt, Blocks.torch, Blocks.anvil, Blocks.trapped_chest,
                Blocks.noteblock, Blocks.jukebox, Blocks.tnt, Blocks.gold_ore, Blocks.iron_ore, Blocks.lapis_ore, Blocks.lit_redstone_ore, Blocks.quartz_ore, Blocks.redstone_ore,
                Blocks.wooden_pressure_plate, Blocks.stone_pressure_plate, Blocks.trapped_chest, Blocks.light_weighted_pressure_plate, Blocks.heavy_weighted_pressure_plate,
                Blocks.stone_button, Blocks.wooden_button, Blocks.lever, Blocks.tallgrass, Blocks.tripwire, Blocks.tripwire_hook, Blocks.rail, Blocks.waterlily, Blocks.red_flower,
                Blocks.red_mushroom, Blocks.brown_mushroom, Blocks.vine, Blocks.trapdoor, Blocks.yellow_flower, Blocks.ladder, Blocks.furnace, Blocks.sand, Blocks.cactus,
                Blocks.dispenser, Blocks.noteblock, Blocks.dropper, Blocks.crafting_table, Blocks.pumpkin, Blocks.sapling, Blocks.cobblestone_wall,
                Blocks.oak_fence, Blocks.activator_rail, Blocks.detector_rail, Blocks.golden_rail, Blocks.redstone_torch, Blocks.acacia_stairs,
                Blocks.birch_stairs, Blocks.brick_stairs, Blocks.dark_oak_stairs, Blocks.jungle_stairs, Blocks.nether_brick_stairs, Blocks.oak_stairs,
                Blocks.quartz_stairs, Blocks.red_sandstone_stairs, Blocks.sandstone_stairs, Blocks.spruce_stairs, Blocks.stone_brick_stairs, Blocks.stone_stairs,
                Blocks.wooden_slab, Blocks.double_wooden_slab, Blocks.stone_slab, Blocks.double_stone_slab, Blocks.stone_slab2, Blocks.double_stone_slab2,
                Blocks.web, Blocks.gravel, Blocks.daylight_detector_inverted, Blocks.daylight_detector, Blocks.soul_sand, Blocks.piston, Blocks.piston_extension,
                Blocks.piston_head, Blocks.sticky_piston, Blocks.iron_trapdoor, Blocks.ender_chest, Blocks.end_portal, Blocks.end_portal_frame, Blocks.standing_banner,
                Blocks.wall_banner, Blocks.deadbush, Blocks.slime_block, Blocks.acacia_fence_gate, Blocks.birch_fence_gate, Blocks.dark_oak_fence_gate,
                Blocks.jungle_fence_gate, Blocks.spruce_fence_gate, Blocks.oak_fence_gate); // @on
    }

    private boolean hotBarContainBlock() {
        int i = 36;

        while (i < 45) {
            try {
                final ItemStack slotStack = mc.player.inventoryContainer.getSlot(i).getStack();

                if (slotStack == null || slotStack.getItem() == null || !(slotStack
                        .getItem() instanceof ItemBlock) || !isValid(slotStack.getItem())) {
                    i++;
                    continue;
                }

                return true;
            } catch (Exception ignored) {
            }
        }

        return false;
    }

    private boolean isValid(Item item) {
        if (!(item instanceof ItemBlock)) {
            return false;
        } else {
            final ItemBlock itemBlock = (ItemBlock) item;
            final Block block = itemBlock.getBlock();

            return !getBlacklistedBlocks().contains(block);
        }
    }

    private int getBiggestBlockSlotInv() {
        int slot = -1, size = 0;

        if (getBlockCount() == 0) {
            return -1;
        }

        for (int i = 9; i < 36; i++) {
            final Slot s = mc.player.inventoryContainer.getSlot(i);

            if (s.getHasStack()) {
                final Item item = s.getStack().getItem();
                final ItemStack is = s.getStack();

                if (item instanceof ItemBlock && isValid(item) && is.stackSize > size) {
                    size = is.stackSize;
                    slot = i;
                }
            }
        }

        return slot;
    }

    private int getBiggestBlockSlotHotbar() {
        int slot = -1, size = 0;

        if (getBlockCount() == 0) {
            return -1;
        }

        for (int i = 36; i < 45; i++) {
            final Slot s = mc.player.inventoryContainer.getSlot(i);

            if (s.getHasStack()) {
                final Item item = s.getStack().getItem();
                final ItemStack is = s.getStack();

                if (item instanceof ItemBlock && isValid(item) && is.stackSize > size) {
                    size = is.stackSize;
                    slot = i;
                }
            }
        }

        return slot;
    }

    public boolean isAirBlock(Block block) {
        if (block.getMaterial().isReplaceable()) {
            return !(block instanceof BlockSnow) || !(block.getBlockBoundsMaxY() > 0.125);
        }

        return false;
    }

    public double[] getExpandCoords(double y) {
        BlockPos underPos = new BlockPos(mc.player.posX, y, mc.player.posZ);
        Block underBlock = mc.world.getBlockState(underPos).getBlock();
        MovementInput movementInput = mc.player.movementInput();

        float forward = movementInput.moveForward(), strafe = movementInput.moveStrafe(), yaw = mc.player.rotationYaw;
        double xCalc = -999, zCalc = -999, dist = 0, expandDist = expand() ? 0.5 : 0.0;

        while (!isAirBlock(underBlock)) {
            xCalc = mc.player.posX;
            zCalc = mc.player.posZ;
            dist++;

            if (dist > expandDist) {
                dist = expandDist;
            }

            xCalc += (forward * 0.45 * MathHelper.cos(Math.toRadians(yaw + 90.0f)) + strafe * 0.45 * MathHelper.sin(Math.toRadians(yaw + 90.0f))) * dist;
            zCalc += (forward * 0.45 * MathHelper.sin(Math.toRadians(yaw + 90.0f)) - strafe * 0.45 * MathHelper.cos(Math.toRadians(yaw + 90.0f))) * dist;

            if (dist == expandDist) {
                break;
            }

            underPos = new BlockPos(xCalc, y, zCalc);
            underBlock = mc.world.getBlockState(underPos).getBlock();
        }

        return new double[]{xCalc, zCalc};
    }

    @EventTarget
    public void onPre2(MotionUpdateEvent event) {
        if (event.getState().equals(MotionUpdateEvent.State.PRE)) {
            if (addons.contains("Swapper")) {
                if (timer.delay(250)) {
                    final int bestInvSlot = getBiggestBlockSlotInv(), bestHotBarSlot = getBiggestBlockSlotHotbar();
                    int bestSlot = bestHotBarSlot > 0 ? bestHotBarSlot : bestInvSlot, spoofSlot = 42;

                    if (bestHotBarSlot > 0 && bestInvSlot > 0 && mc.player.inventoryContainer.getSlot(bestInvSlot)
                            .getHasStack() && mc.player.inventoryContainer.getSlot(bestHotBarSlot)
                            .getHasStack() && mc.player.inventoryContainer.getSlot(bestHotBarSlot)
                            .getStack().stackSize < mc.player.inventoryContainer.getSlot(bestInvSlot).getStack().stackSize) {
                        bestSlot = bestInvSlot;
                    }

                    if (hotBarContainBlock()) {
                        for (int a = 36; a < 45; a++) {
                            if (mc.player.inventoryContainer.getSlot(a).getHasStack()) {
                                final Item item = mc.player.inventoryContainer.getSlot(a).getStack().getItem();

                                if (item instanceof ItemBlock && isValid(item)) {
                                    spoofSlot = a;
                                    break;
                                }
                            }
                        }

                    } else {
                        for (int a = 36; a < 45; a++) {
                            if (!mc.player.inventoryContainer.getSlot(a).getHasStack()) {
                                spoofSlot = a;
                                break;
                            }
                        }
                    }

                    if (mc.player.inventoryContainer.getSlot(spoofSlot).slotNumber != bestSlot) {
                        if (hotBarContainBlock()) {
                            mc.player.swap(bestSlot, spoofSlot - 36);
                        } else {
                            mc.player.swap(bestSlot, slot.get() - 1);
                        }
                    }

                    timer.reset();
                }

            } else {
                if (getBlockCountHotBar() <= 0) {
                    mc.player.swap(getBiggestBlockSlotInv(), slot.get() - 1);
                }
            }
        }
    }

    private boolean towering() {
        return addons.contains("Tower") && !mc.player.isMoving() && Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode()) &&
                (!mc.player.isPotionActive(Potion.jump) || mc.player.getActivePotionEffect(Potion.jump).getAmplifier() + 1 < 3);
    }

    private boolean towerMoving() {
        return Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode()) && mc.player.isMoving();
    }

    private boolean downwarding() {
        return addons.contains("Down Ward") && Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode());
    }

    private boolean isOnGround() {
        return mc.player.getDistanceY(blockBelow.getY()) == 1.0 && (mc.world.getBlockState(blockBelow).getBlock() == Blocks.air || mc.world.getBlockState(blockBelow).getBlock().isFullBlock());
    }

    public boolean safeWalk() {
        return !downwarding() && addons.contains("Safe Walk");
    }

    public boolean expand() {
        return !no_sprint.get() && mc.player.getLastTickDistance() < mc.player.getBySprinting(false)
                && !towerMoving() && !isEnabled(Speed.class) && !downwarding() && isOnGround();
    }

    @EventTarget
    public void onJump(JumpEvent event) {
        if (towering() || towerMoving()) {
            event.setCancelled(true);
        }
    }

    @EventTarget
    public void onSneak(SneakEvent event) {
        if (addons.contains("Down Ward")) {
            event.setCancelled(true);
        }
    }

    @EventTarget
    public void onPreUpdate(MotionUpdateEvent event) {
        if (event.getState().equals(MotionUpdateEvent.State.PRE)) {
            if (towerMoving()) {
                towerMove(event);
            } else if (towering()) {
                mc.player.setSpeed(0);
                towerMove(event);
            }

            if (towering() || towerMoving()) {
                mc.player.cameraPitch = 0.0F;
                mc.player.cameraYaw = 0.0F;

            } else if (isOnGround() && ServerUtils.isHypixel()) {
                mc.player.cameraYaw = 0.0F;
                mc.player.onGround = true;
                mc.player.motionY = 0.0;
                event.setOnGround(true);
            }
        }
    }

    private void towerMove(MotionUpdateEvent event) {
        if (mc.player.isOnGround(0.76) && !mc.player.isOnGround(0.75) && mc.player.motionY > 0.23 && mc.player.motionY < 0.25) {
            mc.player.motionY = Math.round(mc.player.posY) - mc.player.posY;
        }

        if (mc.player.isOnGround(0.0001)) {
            mc.player.motionY = 0.41999998688698;
            mc.player.setMotion(0.9);

        } else if (mc.player.posY >= Math.round(mc.player.posY) - 0.0001 && mc.player.posY <= Math.round(mc.player.posY) + 0.0001 && !Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode())) {
            mc.player.motionY = 0.0;
        }

        event.setOnGround(event.getY() % 0.125 == 0);
    }

    private MovingObjectPosition getPositionFromVec3(Vec3 vec3, float[] angles) {
        float borderSize = mc.player.getCollisionBorderSize();
        float distance = mc.playerController.getBlockReachDistance();
        Vec3 positionEyes = RotationUtil.getPositionEyes(1);
        Vec3 startVec = RotationUtil.getVectorForRotation(angles[1], angles[0]);
        Vec3 vector = positionEyes.addVector(startVec.xCoord * distance, startVec.yCoord * distance, startVec.zCoord * distance);

        return mc.world.rayTraceBlocks(positionEyes, vector, false, false, false);
    }

    @EventTarget
    public void onPost(MotionUpdateEvent event) {
        if (event.getState().equals(MotionUpdateEvent.State.PRE)) {
            if (blockData != null) {
                float[] rots = RotationUtil.getAngles(MathHelper.getVec3(blockData.position, blockData.facing));
                this.yaw = rots[0];
                this.pitch = rots[1];

                if (addons.contains("Silent")) {
                    if (currentSlot != neededSlot()) {
                        sendPacketNoEvent(new C09PacketHeldItemChange(currentSlot = neededSlot()));
                    }

                } else if (mc.player.inventory.currentItem != neededSlot()) {
                    mc.player.inventory.currentItem = neededSlot();
                }
            }

            event.setYaw(towering() ? this.yaw : getYawBackward());
            event.setPitch(this.pitch);

            if (ServerUtils.isHypixel() && mc.player.isMoving()) {
                if (isSneak() || no_sprint.get() || towerMoving() || downwarding() || towering()) {
                    mc.player.setSprinting(false);
                } else if (isOnGround() && mc.player.onGround && mc.player.fallDistance == 0) {
                    mc.player.setSprinting(!neededTick());
                }
            }

        } else if (!towering() && blockData != null && (mc.player.getLastTickDistance() < mc.player.getBySprinting(false) && !mc.player.isSprinting()
                || !ServerUtils.isHypixel()) && neededSlot() == (addons.contains("Silent") ? currentSlot : mc.player.inventory.currentItem)) {
            placeBlock();
        }
    }

    public boolean spoofValue() {
        return mc.player.isMoving() && blockBelow != null && isOnGround() && !towering()
                && !towerMoving() && !downwarding() && ServerUtils.isHypixel() && !no_sprint.get() && !isSneak();
    }

    private boolean neededTick() {
        return mc.player.isPotionActive(Potion.moveSpeed) ? mc.player.ticksExisted % 3 != 0 : mc.player.ticksExisted % 2 == 0;
    }

    private boolean isSneak() {
        return !isEnabled(FastSneak.class) && mc.player.movementInput().sneak();
    }

    @EventTarget
    public void onMove(MoveEvent event) {
        if (ServerUtils.isHypixel() && mc.player.isMoving()) {
            if (isSneak()) {
                event.setMoveSpeed(mc.player.bySneaking());
            } else if (no_sprint.get() || towerMoving() || downwarding()) {
                event.setMoveSpeed(mc.player.getBySprinting(false) - 0.01 / 20);
            } else if (isOnGround() && mc.player.onGround && mc.player.fallDistance == 0) {
                event.setMoveSpeed(neededTick() ? mc.player.getBySprinting(false) - 0.01 / 20 : mc.player.getBaseMoveSpeed(0.24) * 1.5);
            }
        }
    }

    @EventTarget
    public void onTower(PacketEvent event) {
        if (event.getState().equals(PacketEvent.State.OUTGOING)) {
            if (addons.contains("Silent")) {
                if (event.getPacket() instanceof C09PacketHeldItemChange) {
                    C09PacketHeldItemChange packet = (C09PacketHeldItemChange) event.getPacket();

                    if (packet.getSlotId() != neededSlot()) {
                        event.setCancelled(true);
                    }
                }
            }

            if (towering() && ServerUtils.isHypixel()) {
                if (event.getPacket() instanceof C03PacketPlayer) {
                    C03PacketPlayer packet = (C03PacketPlayer) event.getPacket();

                    if (blockData != null && packet.isMoving()) {
                        event.setCancelled(true);
                        sendPacketNoEvent(new C04PacketPlayerPosition(mc.player.posX, mc.player.posY + 0.00133597911214, mc.player.posZ, false));
                        sendPacketNoEvent(new C04PacketPlayerPosition(mc.player.posX, mc.player.posY + 0.12633597911214, mc.player.posZ, false));
                        sendPacketNoEvent(new C04PacketPlayerPosition(mc.player.posX, mc.player.posY, mc.player.posZ, true));
                        placeBlock();
                    }
                }
            }

            if (mc.player.isMoving() || towering()) {
                if (event.getPacket() instanceof C08PacketPlayerBlockPlacement) {
                    event.setCancelled(true);
                }

                if (event.getPacket() instanceof C07PacketPlayerDigging) {
                    C07PacketPlayerDigging packet = (C07PacketPlayerDigging) event.getPacket();

                    if (packet.getStatus().equals(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM)) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    public float getYawBackward() {
        float yaw = MathHelper.wrapAngleTo180_float(mc.player.rotationYaw);

        MovementInput input = mc.player.movementInput();
        float strafe = input.moveStrafe(), forward = input.moveForward();

        if (forward != 0) {
            if (strafe < 0) {
                yaw += forward < 0 ? 135 : 45;
            } else if (strafe > 0) {
                yaw -= forward < 0 ? 135 : 45;
            } else if (strafe == 0 && forward < 0) {
                yaw -= 180;
            }

        } else {
            if (strafe < 0) {
                yaw += 90;
            } else if (strafe > 0) {
                yaw -= 90;
            }
        }

        return MathHelper.wrapAngleTo180_float(yaw - 180);
    }

    private void placeBlock() {
        ItemStack stack = mc.player.inventory.getStackInSlot(neededSlot());
        BlockPos pos = blockData.position;
        EnumFacing facing = blockData.facing;
        Vec3 vector = MathHelper.getVec3(pos, facing);

        if (mc.playerController.onPlayerRightClickNoEvent(mc.player, mc.world, stack, pos, facing, vector)) {
            if (path.get() && !blockPosList.contains(pos)) {
                blockPosList.add(pos);
            }

            if (addons.contains("Swing Item")) {
                mc.player.swingItem();
            } else {
                sendPacket(new C0APacketAnimation());
            }
        }
    }

    @EventTarget
    public void onPre(MotionUpdateEvent event) {
        if (event.getState().equals(MotionUpdateEvent.State.PRE)) {
            if (!downwarding() || isOnGround()) {
                floorY = MathHelper.floor_double(mc.player.posY);
            }

            double yPos = floorY - (downwarding() ? 2 : 1);
            boolean air = isAirBlock(mc.world.getBlockState(new BlockPos(mc.player.posX, yPos, mc.player.posZ)).getBlock());
            double xPos = air ? mc.player.posX : getExpandCoords(yPos)[0], zPos = air ? mc.player.posZ : getExpandCoords(yPos)[1];
            blockBelow = new BlockPos(xPos, yPos, zPos);

            boolean setBlockData = mc.world.getBlockState(blockBelow).getBlock().getMaterial().isReplaceable() || mc.world.getBlockState(blockBelow).getBlock() == Blocks.air;
            blockData = setBlockData ? downwarding() ? getBlockDataDownwards(blockBelow) : getBlockData(blockBelow) : null;
        }
    }

    public int getBlockCount() {
        int blockCount = 0;

        for (int i = 0; i < 45; ++i) {
            if (!mc.player.inventoryContainer.getSlot(i).getHasStack()) continue;

            final ItemStack is = mc.player.inventoryContainer.getSlot(i).getStack();
            final Item item = is.getItem();

            if (!(is.getItem() instanceof ItemBlock) || getBlacklistedBlocks().contains(((ItemBlock) item).getBlock())) {
                continue;
            }

            blockCount += is.stackSize;
        }

        return blockCount;
    }

    private int getBlockCountHotBar() {
        int blockCount = 0;

        for (int i = 36; i < 45; ++i) {
            if (!mc.player.inventoryContainer.getSlot(i).getHasStack()) continue;

            final ItemStack is = mc.player.inventoryContainer.getSlot(i).getStack();
            final Item item = is.getItem();

            if (!(is.getItem() instanceof ItemBlock) || getBlacklistedBlocks().contains(((ItemBlock) item).getBlock())) {
                continue;
            }

            blockCount += is.stackSize;
        }

        return blockCount;
    }

    @EventTarget
    public void onUpdate(PlayerUpdateEvent event) {
        if (getBlockCount() == 0) {
            novoline.getNotificationManager().pop(getDisplayName(), "No blocks in inventory", NotificationType.WARNING);
            toggle();
        }
    }

    @EventTarget
    public void onTick(TickUpdateEvent event) {
        setSuffix("Watchdog");
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        GL11.glPushMatrix();
        ScaleUtils.scale(mc);
        ItemStack stack = mc.player.inventory.getStackInSlot(neededSlot());

        if (stack != null && stack.getItem() instanceof ItemBlock && isValid(stack.getItem())) {
            ScaledResolution sr = event.getResolution();
            float width = Fonts.SF.SF_18.SF_18.stringWidth("/" + getBlockCount());
            float x = sr.getScaledWidthStatic(mc) / 2 - width / 2, y = sr.getScaledHeightStatic(mc) / 2;

            RenderUtils.drawStack(Fonts.SF.SF_18.SF_18, false, stack, x - 5.0F, y + 11);
            Fonts.SF.SF_18.SF_18.drawString(Integer.toString(getBlockCount()), x + 11.0F, y + 16, 0xFFFFFF, true);
        }

        GL11.glPopMatrix();
    }

    public float[] getHSB(int color) {
        float[] hsbValues = new float[3];

        float saturation, brightness;
        float hue;

        int cMax = Math.max(color >>> 16 & 0xFF, color >>> 8 & 0xFF);
        if ((color & 0xFF) > cMax) cMax = color & 0xFF;

        int cMin = Math.min(color >>> 16 & 0xFF, color >>> 8 & 0xFF);
        if ((color & 0xFF) < cMin) cMin = color & 0xFF;

        brightness = (float) cMax / 255.0F;
        saturation = cMax != 0 ? (float) (cMax - cMin) / (float) cMax : 0;

        if (saturation == 0) {
            hue = 0;
        } else {
            float redC = (float) (cMax - (color >>> 16 & 0xFF)) / (float) (cMax - cMin), // @off
                    greenC = (float) (cMax - (color >>> 8 & 0xFF)) / (float) (cMax - cMin),
                    blueC = (float) (cMax - (color & 0xFF)) / (float) (cMax - cMin); // @on

            hue = ((color >>> 16 & 0xFF) == cMax ?
                    blueC - greenC :
                    (color >>> 8 & 0xFF) == cMax ? 2.0F + redC - blueC : 4.0F + greenC - redC) / 6.0F;

            if (hue < 0) hue += 1.0F;
        }

        hsbValues[0] = hue;
        hsbValues[1] = saturation;
        hsbValues[2] = brightness;

        return hsbValues;
    }

    @EventTarget
    public void onRender3D(Render3DEvent event) {
        if (path.get()) {
            RenderUtils.pre3D();
            GL11.glLineWidth(2);

            long j = Minecraft.getSystemTime();
            GL11.glBegin(GL11.GL_LINE_STRIP);

            for (BlockPos po : blockPosList) {
                float brightness = 1.0F - MathHelper.abs(MathHelper.sin(j % 6000F / 6000F * Math.PI * 2.0F) * 0.6F);
                final float[] hudHSB = getHSB(color.get());
                Color color = Color.getHSBColor(hudHSB[0], hudHSB[1], brightness);
                RenderUtils.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 255));

                double x = po.getX() + 0.5 - mc.getRenderManager().renderPosX;
                double y = po.getY() + 1 - mc.getRenderManager().renderPosY;
                double z = po.getZ() + 0.5 - mc.getRenderManager().renderPosZ;
                GL11.glVertex3d(x, y, z);
                j -= 300;
            }

            GL11.glEnd();
            RenderUtils.post3D();
        }
    }

    private BlockData getBlockData(BlockPos pos) {
        if (isPosSolid(pos.add(0, -1, 0))) {
            return new BlockData(pos.add(0, -1, 0), EnumFacing.UP);
        } else if (isPosSolid(pos.add(-1, 0, 0))) {
            return new BlockData(pos.add(-1, 0, 0), EnumFacing.EAST);
        } else if (isPosSolid(pos.add(1, 0, 0))) {
            return new BlockData(pos.add(1, 0, 0), EnumFacing.WEST);
        } else if (isPosSolid(pos.add(0, 0, 1))) {
            return new BlockData(pos.add(0, 0, 1), EnumFacing.NORTH);
        } else if (isPosSolid(pos.add(0, 0, -1))) {
            return new BlockData(pos.add(0, 0, -1), EnumFacing.SOUTH);
        }

        BlockPos pos1 = pos.add(-1, 0, 0);

        if (isPosSolid(pos1.add(0, -1, 0))) {
            return new BlockData(pos1.add(0, -1, 0), EnumFacing.UP);
        } else if (isPosSolid(pos1.add(-1, 0, 0))) {
            return new BlockData(pos1.add(-1, 0, 0), EnumFacing.EAST);
        } else if (isPosSolid(pos1.add(1, 0, 0))) {
            return new BlockData(pos1.add(1, 0, 0), EnumFacing.WEST);
        } else if (isPosSolid(pos1.add(0, 0, 1))) {
            return new BlockData(pos1.add(0, 0, 1), EnumFacing.NORTH);
        } else if (isPosSolid(pos1.add(0, 0, -1))) {
            return new BlockData(pos1.add(0, 0, -1), EnumFacing.SOUTH);
        }

        BlockPos pos2 = pos.add(1, 0, 0);

        if (isPosSolid(pos2.add(0, -1, 0))) {
            return new BlockData(pos2.add(0, -1, 0), EnumFacing.UP);
        } else if (isPosSolid(pos2.add(-1, 0, 0))) {
            return new BlockData(pos2.add(-1, 0, 0), EnumFacing.EAST);
        } else if (isPosSolid(pos2.add(1, 0, 0))) {
            return new BlockData(pos2.add(1, 0, 0), EnumFacing.WEST);
        } else if (isPosSolid(pos2.add(0, 0, 1))) {
            return new BlockData(pos2.add(0, 0, 1), EnumFacing.NORTH);
        } else if (isPosSolid(pos2.add(0, 0, -1))) {
            return new BlockData(pos2.add(0, 0, -1), EnumFacing.SOUTH);
        }

        BlockPos pos3 = pos.add(0, 0, 1);

        if (isPosSolid(pos3.add(0, -1, 0))) {
            return new BlockData(pos3.add(0, -1, 0), EnumFacing.UP);
        } else if (isPosSolid(pos3.add(-1, 0, 0))) {
            return new BlockData(pos3.add(-1, 0, 0), EnumFacing.EAST);
        } else if (isPosSolid(pos3.add(1, 0, 0))) {
            return new BlockData(pos3.add(1, 0, 0), EnumFacing.WEST);
        } else if (isPosSolid(pos3.add(0, 0, 1))) {
            return new BlockData(pos3.add(0, 0, 1), EnumFacing.NORTH);
        } else if (isPosSolid(pos3.add(0, 0, -1))) {
            return new BlockData(pos3.add(0, 0, -1), EnumFacing.SOUTH);
        }

        BlockPos pos4 = pos.add(0, 0, -1);

        if (isPosSolid(pos4.add(0, -1, 0))) {
            return new BlockData(pos4.add(0, -1, 0), EnumFacing.UP);
        } else if (isPosSolid(pos4.add(-1, 0, 0))) {
            return new BlockData(pos4.add(-1, 0, 0), EnumFacing.EAST);
        } else if (isPosSolid(pos4.add(1, 0, 0))) {
            return new BlockData(pos4.add(1, 0, 0), EnumFacing.WEST);
        } else if (isPosSolid(pos4.add(0, 0, 1))) {
            return new BlockData(pos4.add(0, 0, 1), EnumFacing.NORTH);
        } else if (isPosSolid(pos4.add(0, 0, -1))) {
            return new BlockData(pos4.add(0, 0, -1), EnumFacing.SOUTH);
        }

        BlockPos pos19 = pos.add(-2, 0, 0);

        if (isPosSolid(pos1.add(0, -1, 0))) {
            return new BlockData(pos1.add(0, -1, 0), EnumFacing.UP);
        } else if (isPosSolid(pos1.add(-1, 0, 0))) {
            return new BlockData(pos1.add(-1, 0, 0), EnumFacing.EAST);
        } else if (isPosSolid(pos1.add(1, 0, 0))) {
            return new BlockData(pos1.add(1, 0, 0), EnumFacing.WEST);
        } else if (isPosSolid(pos1.add(0, 0, 1))) {
            return new BlockData(pos1.add(0, 0, 1), EnumFacing.NORTH);
        } else if (isPosSolid(pos1.add(0, 0, -1))) {
            return new BlockData(pos1.add(0, 0, -1), EnumFacing.SOUTH);
        }

        BlockPos pos29 = pos.add(2, 0, 0);

        if (isPosSolid(pos2.add(0, -1, 0))) {
            return new BlockData(pos2.add(0, -1, 0), EnumFacing.UP);
        } else if (isPosSolid(pos2.add(-1, 0, 0))) {
            return new BlockData(pos2.add(-1, 0, 0), EnumFacing.EAST);
        } else if (isPosSolid(pos2.add(1, 0, 0))) {
            return new BlockData(pos2.add(1, 0, 0), EnumFacing.WEST);
        } else if (isPosSolid(pos2.add(0, 0, 1))) {
            return new BlockData(pos2.add(0, 0, 1), EnumFacing.NORTH);
        } else if (isPosSolid(pos2.add(0, 0, -1))) {
            return new BlockData(pos2.add(0, 0, -1), EnumFacing.SOUTH);
        }

        BlockPos pos39 = pos.add(0, 0, 2);

        if (isPosSolid(pos3.add(0, -1, 0))) {
            return new BlockData(pos3.add(0, -1, 0), EnumFacing.UP);
        } else if (isPosSolid(pos3.add(-1, 0, 0))) {
            return new BlockData(pos3.add(-1, 0, 0), EnumFacing.EAST);
        } else if (isPosSolid(pos3.add(1, 0, 0))) {
            return new BlockData(pos3.add(1, 0, 0), EnumFacing.WEST);
        } else if (isPosSolid(pos3.add(0, 0, 1))) {
            return new BlockData(pos3.add(0, 0, 1), EnumFacing.NORTH);
        } else if (isPosSolid(pos3.add(0, 0, -1))) {
            return new BlockData(pos3.add(0, 0, -1), EnumFacing.SOUTH);
        }

        BlockPos pos49 = pos.add(0, 0, -2);

        if (isPosSolid(pos4.add(0, -1, 0))) {
            return new BlockData(pos4.add(0, -1, 0), EnumFacing.UP);
        } else if (isPosSolid(pos4.add(-1, 0, 0))) {
            return new BlockData(pos4.add(-1, 0, 0), EnumFacing.EAST);
        } else if (isPosSolid(pos4.add(1, 0, 0))) {
            return new BlockData(pos4.add(1, 0, 0), EnumFacing.WEST);
        } else if (isPosSolid(pos4.add(0, 0, 1))) {
            return new BlockData(pos4.add(0, 0, 1), EnumFacing.NORTH);
        } else if (isPosSolid(pos4.add(0, 0, -1))) {
            return new BlockData(pos4.add(0, 0, -1), EnumFacing.SOUTH);
        }

        BlockPos pos5 = pos.add(0, -1, 0);

        if (isPosSolid(pos5.add(0, -1, 0))) {
            return new BlockData(pos5.add(0, -1, 0), EnumFacing.UP);
        } else if (isPosSolid(pos5.add(-1, 0, 0))) {
            return new BlockData(pos5.add(-1, 0, 0), EnumFacing.EAST);
        } else if (isPosSolid(pos5.add(1, 0, 0))) {
            return new BlockData(pos5.add(1, 0, 0), EnumFacing.WEST);
        } else if (isPosSolid(pos5.add(0, 0, 1))) {
            return new BlockData(pos5.add(0, 0, 1), EnumFacing.NORTH);
        } else if (isPosSolid(pos5.add(0, 0, -1))) {
            return new BlockData(pos5.add(0, 0, -1), EnumFacing.SOUTH);
        }

        BlockPos pos6 = pos5.add(1, 0, 0);

        if (isPosSolid(pos6.add(0, -1, 0))) {
            return new BlockData(pos6.add(0, -1, 0), EnumFacing.UP);
        } else if (isPosSolid(pos6.add(-1, 0, 0))) {
            return new BlockData(pos6.add(-1, 0, 0), EnumFacing.EAST);
        } else if (isPosSolid(pos6.add(1, 0, 0))) {
            return new BlockData(pos6.add(1, 0, 0), EnumFacing.WEST);
        } else if (isPosSolid(pos6.add(0, 0, 1))) {
            return new BlockData(pos6.add(0, 0, 1), EnumFacing.NORTH);
        } else if (isPosSolid(pos6.add(0, 0, -1))) {
            return new BlockData(pos6.add(0, 0, -1), EnumFacing.SOUTH);
        }

        BlockPos pos7 = pos5.add(-1, 0, 0);

        if (isPosSolid(pos7.add(0, -1, 0))) {
            return new BlockData(pos7.add(0, -1, 0), EnumFacing.UP);
        } else if (isPosSolid(pos7.add(-1, 0, 0))) {
            return new BlockData(pos7.add(-1, 0, 0), EnumFacing.EAST);
        } else if (isPosSolid(pos7.add(1, 0, 0))) {
            return new BlockData(pos7.add(1, 0, 0), EnumFacing.WEST);
        } else if (isPosSolid(pos7.add(0, 0, 1))) {
            return new BlockData(pos7.add(0, 0, 1), EnumFacing.NORTH);
        } else if (isPosSolid(pos7.add(0, 0, -1))) {
            return new BlockData(pos7.add(0, 0, -1), EnumFacing.SOUTH);
        }

        BlockPos pos8 = pos5.add(0, 0, 1);

        if (isPosSolid(pos8.add(0, -1, 0))) {
            return new BlockData(pos8.add(0, -1, 0), EnumFacing.UP);
        } else if (isPosSolid(pos8.add(-1, 0, 0))) {
            return new BlockData(pos8.add(-1, 0, 0), EnumFacing.EAST);
        } else if (isPosSolid(pos8.add(1, 0, 0))) {
            return new BlockData(pos8.add(1, 0, 0), EnumFacing.WEST);
        } else if (isPosSolid(pos8.add(0, 0, 1))) {
            return new BlockData(pos8.add(0, 0, 1), EnumFacing.NORTH);
        } else if (isPosSolid(pos8.add(0, 0, -1))) {
            return new BlockData(pos8.add(0, 0, -1), EnumFacing.SOUTH);
        }

        BlockPos pos9 = pos5.add(0, 0, -1);

        if (isPosSolid(pos9.add(0, -1, 0))) {
            return new BlockData(pos9.add(0, -1, 0), EnumFacing.UP);
        } else if (isPosSolid(pos9.add(-1, 0, 0))) {
            return new BlockData(pos9.add(-1, 0, 0), EnumFacing.EAST);
        } else if (isPosSolid(pos9.add(1, 0, 0))) {
            return new BlockData(pos9.add(1, 0, 0), EnumFacing.WEST);
        } else if (isPosSolid(pos9.add(0, 0, 1))) {
            return new BlockData(pos9.add(0, 0, 1), EnumFacing.NORTH);
        } else if (isPosSolid(pos9.add(0, 0, -1))) {
            return new BlockData(pos9.add(0, 0, -1), EnumFacing.SOUTH);
        }
        return null;
    }

    private boolean isPosSolid(BlockPos pos) {
        final Block block = mc.world.getBlockState(pos).getBlock();
        return !invalid.contains(block);
    }

    private BlockData getBlockDataDownwards(BlockPos pos) {
        // region Main
        if (isPosSolid(pos.add(0, 1, 0))) {
            return new BlockData(pos.add(0, 1, 0), EnumFacing.DOWN);
        } else if (isPosSolid(pos.add(0, 0, 1))) {
            return new BlockData(pos.add(0, 0, 1), EnumFacing.NORTH);
        } else if (isPosSolid(pos.add(0, 1, 1))) {
            return new BlockData(pos.add(0, 1, 1), EnumFacing.DOWN);
        } else if (isPosSolid(pos.add(-1, 0, 0))) {
            return new BlockData(pos.add(-1, 0, 0), EnumFacing.EAST);
        } else if (isPosSolid(pos.add(-1, 1, 0))) {
            return new BlockData(pos.add(-1, 1, 0), EnumFacing.DOWN);
        } else if (isPosSolid(pos.add(1, 0, 0))) {
            return new BlockData(pos.add(1, 0, 0), EnumFacing.WEST);
        } else if (isPosSolid(pos.add(1, 1, 0))) {
            return new BlockData(pos.add(1, 1, 0), EnumFacing.DOWN);
        } else if (isPosSolid(pos.add(0, 0, -1))) {
            return new BlockData(pos.add(0, 0, -1), EnumFacing.SOUTH);
        } else if (isPosSolid(pos.add(0, 1, -1))) {
            return new BlockData(pos.add(0, 1, -1), EnumFacing.DOWN);
        }

        // region East
        final BlockPos add = pos.add(-1, 0, 0);

        if (isPosSolid(add.add(-1, 0, 0))) {
            return new BlockData(add.add(-1, 0, 0), EnumFacing.EAST);
        } else if (isPosSolid(add.add(-1, 1, 0))) {
            return new BlockData(add.add(-1, 1, 0), EnumFacing.DOWN);
        } else if (isPosSolid(add.add(1, 0, 0))) {
            return new BlockData(add.add(1, 0, 0), EnumFacing.WEST);
        } else if (isPosSolid(add.add(1, 1, 0))) {
            return new BlockData(add.add(1, 1, 0), EnumFacing.DOWN);
        } else if (isPosSolid(add.add(0, 0, -1))) {
            return new BlockData(add.add(0, 0, -1), EnumFacing.SOUTH);
        } else if (isPosSolid(add.add(0, 1, -1))) {
            return new BlockData(add.add(0, 1, -1), EnumFacing.DOWN);
        } else if (isPosSolid(add.add(0, 0, 1))) {
            return new BlockData(add.add(0, 0, 1), EnumFacing.NORTH);
        } else if (isPosSolid(add.add(0, 1, 1))) {
            return new BlockData(add.add(0, 1, 1), EnumFacing.DOWN);
        }

        // region West
        final BlockPos add2 = pos.add(1, 0, 0);

        if (isPosSolid(add2.add(-1, 0, 0))) {
            return new BlockData(add2.add(-1, 0, 0), EnumFacing.EAST);
        } else if (isPosSolid(add2.add(-1, 1, 0))) {
            return new BlockData(add2.add(-1, 1, 0), EnumFacing.DOWN);
        } else if (isPosSolid(add2.add(1, 0, 0))) {
            return new BlockData(add2.add(1, 0, 0), EnumFacing.WEST);
        } else if (isPosSolid(add2.add(1, 1, 0))) {
            return new BlockData(add2.add(1, 1, 0), EnumFacing.DOWN);
        } else if (isPosSolid(add2.add(0, 0, -1))) {
            return new BlockData(add2.add(0, 0, -1), EnumFacing.SOUTH);
        } else if (isPosSolid(add2.add(0, 1, -1))) {
            return new BlockData(add2.add(0, 1, -1), EnumFacing.DOWN);
        } else if (isPosSolid(add2.add(0, 0, 1))) {
            return new BlockData(add2.add(0, 0, 1), EnumFacing.NORTH);
        } else if (isPosSolid(add2.add(0, 1, 1))) {
            return new BlockData(add2.add(0, 1, 1), EnumFacing.DOWN);
        }

        // region South
        final BlockPos add3 = pos.add(0, 0, -1);

        if (isPosSolid(add3.add(-1, 0, 0))) {
            return new BlockData(add3.add(-1, 0, 0), EnumFacing.EAST);
        } else if (isPosSolid(pos.add(-1, 1, 0))) {
            return new BlockData(add3.add(-1, 1, 0), EnumFacing.DOWN);
        } else if (isPosSolid(add3.add(1, 0, 0))) {
            return new BlockData(add3.add(1, 0, 0), EnumFacing.WEST);
        } else if (isPosSolid(add3.add(1, 1, 0))) {
            return new BlockData(add3.add(1, 1, 0), EnumFacing.DOWN);
        } else if (isPosSolid(add3.add(0, 0, -1))) {
            return new BlockData(add3.add(0, 0, -1), EnumFacing.SOUTH);
        } else if (isPosSolid(add3.add(0, 1, -1))) {
            return new BlockData(add3.add(0, 1, -1), EnumFacing.DOWN);
        } else if (isPosSolid(add3.add(0, 0, 1))) {
            return new BlockData(add3.add(0, 0, 1), EnumFacing.NORTH);
        } else if (isPosSolid(add3.add(0, 1, 1))) {
            return new BlockData(add3.add(0, 1, 1), EnumFacing.DOWN);
        }

        // region North
        final BlockPos add4 = pos.add(0, 0, 1);

        if (isPosSolid(add4.add(-1, 0, 0))) {
            return new BlockData(add4.add(-1, 0, 0), EnumFacing.EAST);
        } else if (isPosSolid(add4.add(-1, 1, 0))) {
            return new BlockData(add4.add(-1, 1, 0), EnumFacing.DOWN);
        } else if (isPosSolid(add4.add(1, 0, 0))) {
            return new BlockData(add4.add(1, 0, 0), EnumFacing.WEST);
        } else if (isPosSolid(add4.add(1, 1, 0))) {
            return new BlockData(add4.add(1, 1, 0), EnumFacing.DOWN);
        } else if (isPosSolid(add4.add(0, 0, -1))) {
            return new BlockData(add4.add(0, 0, -1), EnumFacing.SOUTH);
        } else if (isPosSolid(add4.add(0, 1, -1))) {
            return new BlockData(add4.add(0, 1, -1), EnumFacing.DOWN);
        } else if (isPosSolid(add4.add(0, 0, 1))) {
            return new BlockData(add4.add(0, 0, 1), EnumFacing.NORTH);
        } else if (isPosSolid(add4.add(0, 1, 1))) {
            return new BlockData(add4.add(0, 1, 1), EnumFacing.DOWN);
        }

        return null;
    }

    private int neededSlot() {
        for (int i = 0; i < 9; ++i) {
            if (mc.player.inventory.getStackInSlot(i) != null && mc.player.inventory.getStackInSlot(i).stackSize != 0) {
                Item item = mc.player.inventory.getStackInSlot(i).getItem();

                if (isValid(item)) {
                    return i;
                }
            }
        }

        return mc.player.inventory.currentItem;
    }

    @Override
    public void onEnable() {
        if (getBlockCount() > 0) {
            onDisable = true;
            checkModule(Speed.class, Flight.class, LongJump.class);
            setSuffix("Watchdog");

            currentSlot = mc.player.inventory.currentItem;
            oldSlot = mc.player.inventory.currentItem;

            if (mc.world != null) {
                floorY = MathHelper.floor_double(mc.player.posY);
            }

        } else {
            onDisable = false;
        }
    }

    @Override
    public void onDisable() {
        if (onDisable) {
            if (addons.contains("Silent")) {
                if (currentSlot != mc.player.inventory.currentItem) {
                    sendPacketNoEvent(new C09PacketHeldItemChange(currentSlot = mc.player.inventory.currentItem));
                }

            } else {
                mc.player.inventory.currentItem = oldSlot;
            }

            mc.timer.timerSpeed = 1.0F;
            blockPosList.clear();
        }
    }

    private class BlockData {

        private BlockPos position;
        private EnumFacing facing;

        public BlockData(BlockPos position, EnumFacing facing) {
            this.position = position;
            this.facing = facing;
        }
    }

    public BlockData getBlockData() {
        return blockData;
    }

    public List<Block> getBlacklistedBlocks() {
        return blacklistedBlocks;
    }

    public BlockPos getBlockBelow() {
        return blockBelow;
    }

    public ListProperty<String> getAddons() {
        return addons;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public boolean renderRotations() {
        return render_rotations.get();
    }

    public boolean isNoSprint() {
        return no_sprint.get();
    }
}