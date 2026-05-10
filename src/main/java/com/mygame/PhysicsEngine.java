package com.mygame;

import com.jme3.collision.CollisionResults;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.collision.CollisionResult;
import com.jme3.math.Ray;

public class PhysicsEngine {
    private Player player;
    private Node rootNode;

    public PhysicsEngine(Player player, Node rootNode) {
        this.player = player;
        this.rootNode = rootNode;
    }

    public void updatePhysics(float tpf) {
        if (player.isGhostMode()) return;

        // 1. Apply Gravity to Velocity
        float vY = player.getyVelocity();
        vY += player.getGravity() * tpf;
        player.setyVelocity(vY);

        // 2. Predict next position
        Vector3f nextPos = player.position.add(0, vY * tpf, 0);

        // 3. Ground Collision Check
        // We cast a short ray downward from the player's center to detect the floor/blocks
        Ray groundRay = new Ray(player.position.add(0, 0.5f, 0), Vector3f.UNIT_Y.negate());
        CollisionResults results = new CollisionResults();
        rootNode.collideWith(groundRay, results);

        boolean onGround = false;
        if (results.size() > 0) {
            CollisionResult closest = results.getClosestCollision();
            float dist = closest.getDistance();
            
            // If the distance to the floor is very small, we are standing on it
            if (dist <= 0.5f && vY <= 0) {
                onGround = true;
                player.setyVelocity(0);
                // Snap player to the surface of the block/floor
                player.position.y = closest.getContactPoint().y;
            }
        }

        // 4. Fall into the void if not on ground
        if (!onGround) {
            player.position.y += vY * tpf;
        }
        
        // Safety: If you fall too deep into the void, respawn
        if (player.position.y < -50) {
            player.position.set(0, 10, 0);
            player.setyVelocity(0);
        }
    }
}