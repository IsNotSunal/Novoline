package net.minecraft.util;

import net.minecraft.client.settings.GameSettings;

public class MovementInputFromOptions extends MovementInput {
    private final GameSettings gameSettings;

    public MovementInputFromOptions(GameSettings gameSettingsIn) {
        this.gameSettings = gameSettingsIn;
    }

    public void updatePlayerMoveState() {
        this.setMoveStrafe(0.0F);
        this.setMoveForward(0.0F);

        if (this.gameSettings.keyBindForward.isKeyDown()) {
            this.setMoveForward(this.moveForward() + 1);
        }

        if (this.gameSettings.keyBindBack.isKeyDown()) {
            this.setMoveForward(this.moveForward() - 1);
        }

        if (this.gameSettings.keyBindLeft.isKeyDown()) {
            this.setMoveStrafe(this.moveStrafe() + 1);
        }

        if (this.gameSettings.keyBindRight.isKeyDown()) {
            this.setMoveStrafe(this.moveStrafe() - 1);
        }

        this.setJump(this.gameSettings.keyBindJump.isKeyDown());
        this.setSneak(this.gameSettings.keyBindSneak.isKeyDown());

        if (this.sneak()) {
            this.setMoveStrafe(this.moveStrafe() * 0.3F);
            this.setMoveForward(this.moveForward() * 0.3F);
        }
    }
}
