package cc.novoline.modules.visual;

import cc.novoline.events.EventTarget;
import cc.novoline.events.events.Render3DEvent;
import cc.novoline.gui.screen.setting.Manager;
import cc.novoline.gui.screen.setting.Setting;
import cc.novoline.modules.AbstractModule;
import cc.novoline.modules.EnumModuleType;
import cc.novoline.modules.ModuleManager;
import cc.novoline.modules.configurations.annotation.Property;
import cc.novoline.modules.configurations.property.object.BooleanProperty;
import cc.novoline.modules.configurations.property.object.ColorProperty;
import cc.novoline.modules.configurations.property.object.PropertyFactory;
import cc.novoline.utils.RenderUtils;
import cc.novoline.utils.RotationUtil;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

import java.awt.*;

import static cc.novoline.gui.screen.setting.SettingType.CHECKBOX;
import static cc.novoline.gui.screen.setting.SettingType.COLOR_PICKER;
import static cc.novoline.modules.configurations.property.object.PropertyFactory.createColor;

public class BlockOutline extends AbstractModule {

    @Property("color")
    private final ColorProperty color = createColor(new Color(152, 217, 0).getRGB());
    @Property("vector")
    private final BooleanProperty vector = PropertyFactory.booleanFalse();

    public BlockOutline(@NotNull ModuleManager moduleManager) {
        super(moduleManager, "BlocksOutline", "Blocks Outline", EnumModuleType.VISUALS, "Outlines the block you're looking at");
        Manager.put(new Setting("BO_COLOR", "Outline Color", COLOR_PICKER, this, color, null));
        Manager.put(new Setting("BO_VECTOR", "Infinite", CHECKBOX, this, vector));
    }

    public MovingObjectPosition getPosition() {
        float borderSize = mc.player.getCollisionBorderSize();
        float distance = vector.get() ? 250 : mc.playerController.getBlockReachDistance();
        Vec3 positionEyes = RotationUtil.getPositionEyes(1);
        Vec3 startVec = RotationUtil.getVectorForRotation(mc.player.rotationPitch, mc.player.rotationYaw);
        Vec3 endVec = positionEyes.addVector(startVec.xCoord * distance, startVec.yCoord * distance, startVec.zCoord * distance);
        return mc.world.rayTraceBlocks(positionEyes, endVec, false, false, false);
    }

    @EventTarget
    public void onRender3D(Render3DEvent event) {
        if (getPosition() != null) {
            if (getPosition() != null && getPosition().typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                BlockPos pos = getPosition().getBlockPos();
                RenderUtils.pre3D();
                RenderUtils.setColor(color.getAwtColor());

                double x = pos.getX() - mc.getRenderManager().renderPosX;
                double y = pos.getY() - mc.getRenderManager().renderPosY;
                double z = pos.getZ() - mc.getRenderManager().renderPosZ;
                double height = mc.world.getBlockState(pos).getBlock().getBlockBoundsMaxY() - mc.world.getBlockState(pos).getBlock().getBlockBoundsMinY();

                GL11.glLineWidth(1);
                GL11.glBegin(GL11.GL_LINE_STRIP);
                GL11.glVertex3d(x, y, z);
                GL11.glVertex3d(x, y + height, z);
                GL11.glEnd();
                GL11.glBegin(GL11.GL_LINE_STRIP);
                GL11.glVertex3d(x + 1, y, z);
                GL11.glVertex3d(x + 1, y + height, z);
                GL11.glEnd();
                GL11.glBegin(GL11.GL_LINE_STRIP);
                GL11.glVertex3d(x + 1, y, z + 1);
                GL11.glVertex3d(x + 1, y + height, z + 1);
                GL11.glEnd();
                GL11.glBegin(GL11.GL_LINE_STRIP);
                GL11.glVertex3d(x, y, z + 1);
                GL11.glVertex3d(x, y + height, z + 1);
                GL11.glEnd();
                GL11.glBegin(GL11.GL_LINE_STRIP);
                GL11.glVertex3d(x, y, z);
                GL11.glVertex3d(x + 1, y, z);
                GL11.glEnd();
                GL11.glBegin(GL11.GL_LINE_STRIP);
                GL11.glVertex3d(x, y + height, z);
                GL11.glVertex3d(x + 1, y + height, z);
                GL11.glEnd();
                GL11.glBegin(GL11.GL_LINE_STRIP);
                GL11.glVertex3d(x, y, z);
                GL11.glVertex3d(x, y, z + 1);
                GL11.glEnd();
                GL11.glBegin(GL11.GL_LINE_STRIP);
                GL11.glVertex3d(x, y + height, z);
                GL11.glVertex3d(x, y + height, z + 1);
                GL11.glEnd();
                GL11.glBegin(GL11.GL_LINE_STRIP);
                GL11.glVertex3d(x + 1, y, z + 1);
                GL11.glVertex3d(x + 1, y, z + 1);
                GL11.glEnd();
                GL11.glBegin(GL11.GL_LINE_STRIP);
                GL11.glVertex3d(x + 1, y + height, z + 1);
                GL11.glVertex3d(x + 1, y + height, z + 1);
                GL11.glEnd();
                GL11.glBegin(GL11.GL_LINE_STRIP);
                GL11.glVertex3d(x + 1, y, z + 1);
                GL11.glVertex3d(x + 1, y, z);
                GL11.glEnd();
                GL11.glBegin(GL11.GL_LINE_STRIP);
                GL11.glVertex3d(x + 1, y + height, z + 1);
                GL11.glVertex3d(x + 1, y + height, z);
                GL11.glEnd();
                GL11.glBegin(GL11.GL_LINE_STRIP);
                GL11.glVertex3d(x, y, z + 1);
                GL11.glVertex3d(x + 1, y, z + 1);
                GL11.glEnd();
                GL11.glBegin(GL11.GL_LINE_STRIP);
                GL11.glVertex3d(x, y + height, z + 1);
                GL11.glVertex3d(x + 1, y + height, z + 1);
                GL11.glEnd();
                RenderUtils.post3D();
            }
        }
    }
}
