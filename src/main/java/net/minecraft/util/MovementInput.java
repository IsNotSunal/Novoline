package net.minecraft.util;

import cc.novoline.events.EventManager;
import cc.novoline.events.events.JumpEvent;
import cc.novoline.events.events.SneakEvent;

public class MovementInput {
    private float moveStrafe;

    private float moveForward;
    private boolean jump;
    private boolean sneak;

    public void updatePlayerMoveState() {
    }

    /**
     * The speed at which the player is moving forward. Negative numbers will move backwards.
     */
    public float moveForward() {
        return moveForward;
    }

    public void setMoveForward(float moveForward) {
        this.moveForward = moveForward;
    }

    /**
     * The speed at which the player is strafing. Postive numbers to the left and negative to the right.
     */
    public float moveStrafe() {
        return moveStrafe;
    }

    public void setMoveStrafe(float moveStrafe) {
        this.moveStrafe = moveStrafe;
    }

    public boolean jump() {
        return jump;
    }

    public void setJump(boolean jump) {
        JumpEvent event = new JumpEvent();
        EventManager.call(event);
        this.jump = jump && !event.isCancelled();
    }

    public boolean sneak() {
        return sneak;
    }

    public void setSneak(boolean sneak) {
        SneakEvent event = new SneakEvent();
        EventManager.call(event);
        this.sneak = sneak && !event.isCancelled();
    }
}
