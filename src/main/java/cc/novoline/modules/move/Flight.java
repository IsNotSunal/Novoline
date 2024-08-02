package cc.novoline.modules.move;

import cc.novoline.events.EventTarget;
import cc.novoline.events.events.MotionUpdateEvent;
import cc.novoline.events.events.MoveEvent;
import cc.novoline.events.events.PacketEvent;
import cc.novoline.events.events.TickUpdateEvent;
import cc.novoline.gui.screen.setting.Manager;
import cc.novoline.gui.screen.setting.Setting;
import cc.novoline.gui.screen.setting.SettingType;
import cc.novoline.modules.AbstractModule;
import cc.novoline.modules.EnumModuleType;
import cc.novoline.modules.ModuleManager;
import cc.novoline.modules.configurations.annotation.Property;
import cc.novoline.modules.configurations.property.object.*;
import cc.novoline.modules.player.GameSpeed;
import cc.novoline.utils.ServerUtils;
import cc.novoline.utils.Servers;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.potion.Potion;
import org.checkerframework.checker.nullness.qual.NonNull;

public class Flight extends AbstractModule {

    private int tick;
    private boolean wait;

    @Property("mode")
    private final StringProperty mode = PropertyFactory.createString("Vanilla").acceptableValues("Vanilla", "Watchdog");
    @Property("dmg-mode")
    private final StringProperty dmg_mode = PropertyFactory.createString("Normal").acceptableValues("Fast", "Normal");
    @Property("speed")
    private final DoubleProperty speed = PropertyFactory.createDouble(2.5D).maximum(9.0D).minimum(1.0D);
    @Property("viewbobbing")
    private final FloatProperty view_bobbing = PropertyFactory.createFloat(60.0F).minimum(0.0F).maximum(100.0F);
    @Property("pearl")
    private final BooleanProperty pearl = PropertyFactory.booleanFalse();

    public Flight(@NonNull ModuleManager novoline) {
        super(novoline, EnumModuleType.MOVEMENT, "Flight", "Flight");
        Manager.put(new Setting("FLY_MODE", "Mode", SettingType.COMBOBOX, this, mode));
        Manager.put(new Setting("FLY_SPEED", "Speed", SettingType.SLIDER, this, speed, 0.1, () -> mode.equals("Vanilla")));
        Manager.put(new Setting("FLY_VB", "Viewbobbing", SettingType.SLIDER, this, view_bobbing, 5.0F, () -> mode.equals("Vanilla")));
        Manager.put(new Setting("FLY_PEARL", "Pearl boost", SettingType.CHECKBOX, this, pearl, () -> mode.equals("Vanilla")));
    }

    @Override
    public void onDisable() {
        if (tick > 0) {
            mc.player.motionX = 0;
            mc.player.motionZ = 0;
            mc.player.motionY = -0.41999998688698;
        }

        mc.timer.timerSpeed = 1.0F;
        wait = false;
        tick = 0;
    }


    @Override
    public void onEnable() {
        if (mode.equals("Vanilla")) {
            checkModule(Speed.class, Scaffold.class, LongJump.class);

            if (mc.player.onGround && pearl.get() && pearlSlot() != -1) {
                wait = true;
                throwPearl();
                mc.player.motionY = mc.player.getBaseMotionY();
            }

            setSuffix("Vanilla");
        } else {
            checkModule(Speed.class, Scaffold.class, LongJump.class, GameSpeed.class);
        }
    }

    public int pearlSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);

            if (stack != null && stack.getItem() == Items.ender_pearl && (!ServerUtils.serverIs(Servers.SW) ||
                    ServerUtils.inGameSeconds() >= 30 || !stack.getDisplayName().contains("\u00A7"))) {
                return i;
            }
        }

        return -1;
    }

    private void throwPearl() {
        sendPacketNoEvent(new C09PacketHeldItemChange(pearlSlot()));
        sendPacketNoEvent(new C03PacketPlayer.C05PacketPlayerLook(mc.player.rotationYaw, 90, mc.player.onGround));
        sendPacketNoEvent(new C08PacketPlayerBlockPlacement(mc.player.getHeldItem()));
        sendPacketNoEvent(new C03PacketPlayer.C05PacketPlayerLook(mc.player.rotationYaw, mc.player.rotationPitch, mc.player.onGround));
        sendPacketNoEvent(new C09PacketHeldItemChange(mc.player.inventory.currentItem));
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (mode.equals("Vanilla")) {
            if (event.getState().equals(PacketEvent.State.INCOMING)) {
                if (event.getPacket() instanceof S08PacketPlayerPosLook) {
                    if (wait) {
                        wait = false;
                    } else if (tick > 3) {
                        checkModule(getClass());
                    }
                }
            }
        }
    }

    @EventTarget
    public void onTick(TickUpdateEvent event) {
        if (!wait && mc.player.isMoving()) {
            tick++;
        }

        if (mode.equals("Vanilla")) {
            setSuffix("Vanilla");
        } else {
            setSuffix("");
        }
    }

    private int getPacketsSize() {
        int jumps = mc.player.isPotionActive(Potion.jump) ? mc.player.getActivePotionEffect(Potion.jump).getAmplifier() + 1 : 0;
        int hypixel = ServerUtils.serverIs(Servers.UHC) || ServerUtils.serverIs(Servers.SG) || ServerUtils.serverIs(Servers.MW) ? 1 : 0;
        double fallHeight = 3 + jumps + hypixel, amp = 0.125;

        return (int) (fallHeight / amp) * 2 + 2;
    }

    @EventTarget
    public void onPre(MotionUpdateEvent event) {
        if (event.getState() == MotionUpdateEvent.State.PRE) {
            if (mode.equals("Vanilla")) {
                mc.player.cameraYaw = view_bobbing.get() / 1000.0F;

                if (mc.player.movementInput().jump()) {
                    mc.player.motionY = 1.8;
                } else if (mc.player.movementInput().sneak()) {
                    mc.player.motionY = -1.8;
                } else if (!mc.player.onGround) {
                    mc.player.motionY = 0.0;
                }

            } else {
                mc.player.motionY = 0;

                if (mc.player.isMoving() && mc.player.ticksExisted % 10 == 0) {
                    mc.player.setPosition(mc.player.posX + -Math.sin(Math.toRadians(mc.player.rotationYaw)) * mc.player.getBaseMoveSpeed(),
                            mc.player.posY, mc.player.posZ + Math.cos(Math.toRadians(mc.player.rotationYaw)) * mc.player.getBaseMoveSpeed());
                }
            }
        }
    }

    @EventTarget
    public void onMove(MoveEvent event) {
        event.setMoveSpeed(wait || mode.equals("Watchdog") ? 0 : speed.get());
    }

    public DoubleProperty getSpeed() {
        return speed;
    }

    public int getTick() {
        return tick;
    }

    public boolean spoofValue() {
        return mc.player.isMoving() && mode.equals("Watchdog");
    }
}
