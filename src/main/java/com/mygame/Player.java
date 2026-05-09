package com.mygame;

import com.jme3.math.Vector3f;
import com.jme3.math.Quaternion;

public class Player 
{
    public Vector3f position = new Vector3f(0, 2, 0); 
    public float yaw = 0;   
    public float pitch = 0; 
    private float yVelocity = 0;
    
    private final float JUMP_FORCE = 5.0f;
    private final float GRAVITY = -9.81f;
    
    private boolean forward = false;
    private boolean back = false;
    private float moveSpeed = 8.0f;
    private boolean isGhostMode = false;

    public Player() {
        // Constructor
    }

    public void toggleGhostMode() {
        isGhostMode = !isGhostMode;
        yVelocity = 0; 
        System.out.println("Ghost Mode: " + isGhostMode);
    }

    public void rotate(float yawValue, float pitchValue) {
        yaw += yawValue * 5f;
        pitch += pitchValue * 5f;
        if (pitch > 1.5f) pitch = 1.5f;
        if (pitch < -1.5f) pitch = -1.5f;
    }

    public void setForward(boolean pressed) { 
        forward = pressed; 
    }

    public void setBack(boolean pressed) { 
        back = pressed; 
    }

    public void update(float tpf) {
        Quaternion q = new Quaternion();
        q.fromAngles(pitch, yaw, 0); 
        
        Vector3f camDir = q.getRotationColumn(2); 

        if (isGhostMode) {
            // Flying movement
            if (forward) position.addLocal(camDir.mult(moveSpeed * tpf));
            if (back)    position.subtractLocal(camDir.mult(moveSpeed * tpf));
        } else {
            // Walking movement (locked to XZ plane)
            Vector3f walkDir = new Vector3f(camDir.x, 0, camDir.z).normalizeLocal();
            if (forward) position.addLocal(walkDir.mult(moveSpeed * tpf));
            if (back)    position.subtractLocal(walkDir.mult(moveSpeed * tpf));

            // Gravity logic
            position.y += yVelocity * tpf; 
            if (position.y > 0) {
                yVelocity += GRAVITY * tpf; 
            } else {
                position.y = 0;
                yVelocity = 0; 
            }
        }
    }

    public void jump() {
        if (position.y <= 0 && !isGhostMode) { 
            yVelocity = JUMP_FORCE;
        }
    }

    public void adjustSpeed(float delta) {
        moveSpeed += delta;
        if (moveSpeed < 2.0f) moveSpeed = 2.0f; 
        if (moveSpeed > 30.0f) moveSpeed = 30.0f;
        System.out.println("Current Speed: " + moveSpeed);
    }
}