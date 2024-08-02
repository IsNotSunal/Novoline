package cc.novoline.modules.misc;

import cc.novoline.events.EventTarget;
import cc.novoline.events.events.MotionUpdateEvent;
import cc.novoline.events.events.Render2DEvent;
import cc.novoline.events.events.Render3DEvent;
import cc.novoline.modules.AbstractModule;
import cc.novoline.modules.EnumModuleType;
import cc.novoline.modules.ModuleManager;
import cc.novoline.utils.fonts.impl.Fonts;
import org.checkerframework.checker.nullness.qual.NonNull;

public class Debug extends AbstractModule {

    public Debug(@NonNull ModuleManager novoline) {
        super(novoline, EnumModuleType.MISC, "Debug");
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        int y = 90;

        try {
            for (Object debug : new Object[]{mc.player.getLastTickDistance(), mc.player.isSprinting()}) {
                Fonts.SF.SF_20.SF_20.drawString(String.valueOf(debug), 1, y, 0xFFFFFF, true);
                y += Fonts.SF.SF_20.SF_20.getHeight() + 2;
            }

        } catch (NullPointerException ignored) {
        }
    }

    @EventTarget
    public void onMotion(MotionUpdateEvent event) {
        if (event.getState().equals(MotionUpdateEvent.State.PRE)) {
            if (mc.player.isMoving()) {
                //    DebugUtil.print(mc.player.getLastTickDistance());
            }
        }
    }

    @EventTarget
    public void onRender3D(Render3DEvent event) {

    }
}
