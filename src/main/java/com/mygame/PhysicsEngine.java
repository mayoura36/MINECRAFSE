/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mygame;

import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

public class PhysicsEngine 
{
    private Player player;
    private Node rootNode;

    public PhysicsEngine(Player player, Node rootNode) 
    {
        this.player = player;
        this.rootNode = rootNode;
    }

    public void updatePhysics(float tpf) {
        // We only apply gravity if NOT in Ghost Mode
        if (!player.isGhostMode()) {
            float currentYVel = player.getyVelocity();
            
            // Apply gravity to the velocity
            currentYVel += player.getGravity() * tpf;
            player.setyVelocity(currentYVel);

            // Apply velocity to the position
            player.position.y += currentYVel * tpf;

            // Simple floor collision (y=0)
            // Soon we will replace this with real block detection!
            if (player.position.y < 0) {
                player.position.y = 0;
                player.setyVelocity(0);
            }
        }
    }
}
