package cc.novoline.modules.move;

import cc.novoline.events.EventTarget;
import cc.novoline.events.events.MoveEvent;
import cc.novoline.events.events.PlayerUpdateEvent;
import cc.novoline.gui.screen.setting.Manager;
import cc.novoline.gui.screen.setting.Setting;
import cc.novoline.gui.screen.setting.SettingType;
import cc.novoline.modules.AbstractModule;
import cc.novoline.modules.EnumModuleType;
import cc.novoline.modules.ModuleManager;
import cc.novoline.modules.configurations.annotation.Property;
import cc.novoline.modules.configurations.property.object.BooleanProperty;
import cc.novoline.modules.configurations.property.object.PropertyFactory;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.lwjgl.input.Keyboard;

public final class Sprint extends AbstractModule {

    private int groundTick;

    @Property("omni")
    private final BooleanProperty omni = PropertyFactory.createBoolean(false);

    /* constructors @on */
    public Sprint(@NonNull ModuleManager moduleManager) {
        super(moduleManager, "Sprint", "Sprint", Keyboard.KEY_NONE, EnumModuleType.MOVEMENT);
        Manager.put(new Setting("OMNI", "Omni", SettingType.CHECKBOX, this, omni));
    }

    private boolean isSneak() {
        return !isEnabled(FastSneak.class) && mc.player.movementInput().sneak();
    }

    /* events */
    @EventTarget
    public void onMotion(PlayerUpdateEvent event) {
        if (!isEnabled(Scaffold.class)) {
            mc.player.setSprinting(mc.player.isMoving());
        }
    }

    @EventTarget
    public void onMove(MoveEvent event) {
        groundTick = mc.player.onGround ? groundTick + 1 : 0;

        if (omni.get() && groundTick > 1 && mc.player.isMoving() && !isEnabled(Scaffold.class)) {
            event.setMoveSpeed(mc.player.getBaseMoveSpeed(isSneak() ? mc.player.bySneaking() : mc.player.getBySprinting(true), 0.2));
        }
    }
}
