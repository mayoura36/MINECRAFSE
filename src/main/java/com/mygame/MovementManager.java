
package com.mygame;

import com.jme3.math.Vector3f;
import com.jme3.math.Quaternion;

public class MovementManager 
{
    private boolean forward = false, back = false, left = false, right = false;
    private Player player;
    public MovementManager(Player player) 
    {
        this.player = player;
    }

    // Methods to be called by the ActionListener in Main
    public void setForward(boolean pressed) { forward = pressed; }
    public void setBack(boolean pressed) { back = pressed; }
    public void setLeft(boolean pressed) { left = pressed; }
    public void setRight(boolean pressed) { right = pressed; }

    public void updateMovement(float tpf) {
        Quaternion q = new Quaternion();
        q.fromAngles(player.pitch, player.yaw, 0);

        Vector3f camDir = q.getRotationColumn(2); // Forward Vector
        Vector3f camLeft = q.getRotationColumn(0); // Left Vector

        if (player.isGhostMode()) {
            handleGhostMovement(camDir, camLeft, tpf);
        } else {
            handlePhysicsMovement(camDir, camLeft, tpf);
        }
    }

    private void handleGhostMovement(Vector3f dir, Vector3f leftVec, float tpf) {
        float speed = player.getMoveSpeed() * tpf;
        if (forward) player.position.addLocal(dir.mult(speed));
        if (back)    player.position.subtractLocal(dir.mult(speed));
        if (left)    player.position.addLocal(leftVec.mult(speed));
        if (right)   player.position.subtractLocal(leftVec.mult(speed));
    }

    private void handlePhysicsMovement(Vector3f dir, Vector3f leftVec, float tpf) {
        float speed = player.getMoveSpeed() * tpf;
        // Flatten vectors to XZ plane so looking up doesn't make you walk into the sky
        Vector3f walkDir = new Vector3f(dir.x, 0, dir.z).normalizeLocal();
        Vector3f walkLeft = new Vector3f(leftVec.x, 0, leftVec.z).normalizeLocal();

        if (forward) player.position.addLocal(walkDir.mult(speed));
        if (back)    player.position.subtractLocal(walkDir.mult(speed));
        if (left)    player.position.addLocal(walkLeft.mult(speed));
        if (right)   player.position.subtractLocal(walkLeft.mult(speed));
    }
}
