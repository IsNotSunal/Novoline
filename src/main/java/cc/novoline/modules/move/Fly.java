package cc.novoline.modules.move;

import cc.novoline.events.EventTarget;
import cc.novoline.events.events.MotionUpdateEvent;
import cc.novoline.events.events.MoveEvent;
import cc.novoline.modules.AbstractModule;
import cc.novoline.modules.EnumModuleType;
import cc.novoline.modules.ModuleManager;
import org.jetbrains.annotations.NotNull;

public class Fly extends AbstractModule {

    public Fly(@NotNull ModuleManager novoline) {
        super(novoline, EnumModuleType.MOVEMENT, "Fly", "Fly");
    }

    @EventTarget
    public void onMotion(MotionUpdateEvent event) {
        if (event.getState().equals(MotionUpdateEvent.State.PRE)) {
            if (mc.player.ticksExisted % 3 == 0) {
                mc.player.setPosition(mc.player.posX + -Math.sin(Math.toRadians(mc.player.rotationYaw)) * mc.player.getBaseMoveSpeed() * 1.4,
                        mc.player.posY, mc.player.posZ + Math.cos(Math.toRadians(mc.player.rotationYaw)) * mc.player.getBaseMoveSpeed() * 1.4);
            } else {
                mc.player.setPosition(mc.player.posX + -Math.sin(Math.toRadians(mc.player.rotationYaw)) * mc.player.getBySprinting(false),
                        mc.player.posY, mc.player.posZ + Math.cos(Math.toRadians(mc.player.rotationYaw)) * mc.player.getBySprinting(false));
            }
        }
    }

    @EventTarget
    public void onMove(MoveEvent event) {
        event.setMoveSpeed(0);
    }

    @Override
    public void onDisable() {
        mc.timer.timerSpeed = 1;
    }
}