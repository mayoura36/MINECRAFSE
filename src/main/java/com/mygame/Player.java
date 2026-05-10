package com.mygame;

import com.jme3.math.Vector3f;

public class Player {
    // Basic physical properties
    public Vector3f position = new Vector3f(0, 2, 0); 
    public float yaw = 0;   
    public float pitch = 0; 
    private float yVelocity = 0;
    
    // Constants for movement behavior
    private final float JUMP_FORCE = 5.0f;
    private final float GRAVITY = -9.81f;
    
    private float moveSpeed = 8.0f;
    private boolean isGhostMode = false;

    public Player() {
        // Constructor
    }

    // Methods to modify player state
    public void toggleGhostMode() {
        isGhostMode = !isGhostMode;
        yVelocity = 0; // Reset velocity so you don't fall when exiting ghost mode
        System.out.println("Ghost Mode: " + isGhostMode);
    }

    public void rotate(float yawValue, float pitchValue) {
        yaw += yawValue * 5f;
        pitch += pitchValue * 5f;
        
        // Limit looking up and down to prevent the camera from flipping over
        if (pitch > 1.5f) pitch = 1.5f;
        if (pitch < -1.5f) pitch = -1.5f;
    }

    public void jump() {
        // Only allow jumping if the player is nearly on the ground and not flying
        if (position.y <= 0.05f && !isGhostMode) { 
            setyVelocity(JUMP_FORCE);
        }
    }

    public void adjustSpeed(float delta) {
        moveSpeed += delta;
        // Keep speed within a reasonable range
        if (moveSpeed < 2.0f) moveSpeed = 2.0f; 
        if (moveSpeed > 30.0f) moveSpeed = 30.0f;
        System.out.println("Current Speed: " + moveSpeed);
    }

    // Getters and Setters for the other Manager classes to use
    public float getMoveSpeed() { return moveSpeed; }
    public boolean isGhostMode() { return isGhostMode; }
    public float getyVelocity() { return yVelocity; }
    public void setyVelocity(float yVelocity) { this.yVelocity = yVelocity; }
    public float getGravity() { return GRAVITY; }
}