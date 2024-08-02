package cc.novoline.modules.player;

import cc.novoline.events.EventTarget;
import cc.novoline.events.events.PacketEvent;
import cc.novoline.events.events.PlayerUpdateEvent;
import cc.novoline.gui.screen.setting.Manager;
import cc.novoline.gui.screen.setting.Setting;
import cc.novoline.gui.screen.setting.SettingType;
import cc.novoline.modules.AbstractModule;
import cc.novoline.modules.EnumModuleType;
import cc.novoline.modules.ModuleManager;
import cc.novoline.modules.configurations.annotation.Property;
import cc.novoline.modules.configurations.property.object.BooleanProperty;
import cc.novoline.modules.configurations.property.object.FloatProperty;
import cc.novoline.modules.configurations.property.object.PropertyFactory;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import org.jetbrains.annotations.NotNull;

public class GameSpeed extends AbstractModule {

    @Property("timer-boost")
    private final FloatProperty timer_boost = PropertyFactory.createFloat(2.0F).minimum(1.0F).maximum(9.0F);
    @Property("lag-check")
    private final BooleanProperty lag_check = PropertyFactory.createBoolean(true);

    public GameSpeed(@NotNull ModuleManager novoline) {
        super(novoline, EnumModuleType.PLAYER, "GameSpeed", "Game Speed");
        Manager.put(new Setting("GS_LAG_BACK_CHECK", "Lagback check", SettingType.CHECKBOX, this, lag_check));
        Manager.put(new Setting("GS_TIMER_BOOST", "Timer Speed", SettingType.SLIDER, this, timer_boost, 0.2F));
    }

    @EventTarget
    public void onUpdate(PlayerUpdateEvent event) {
        mc.timer.timerSpeed = timer_boost.get();
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (lag_check.get()) {
            if (event.getState().equals(PacketEvent.State.INCOMING) && event.getPacket() instanceof S08PacketPlayerPosLook) {
                checkModule(getClass());
            }
        }
    }

    @Override
    public void onDisable() {
        mc.timer.timerSpeed = 1.0F;
    }
}
