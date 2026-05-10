package com.mygame;

import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

public class PhysicsEngine {
    private Player player;
    private Node rootNode;
    private MovementManager movementManager; // The variable must be here

    // The Constructor MUST receive the movementManager from Main
    public PhysicsEngine(Player player, Node rootNode, MovementManager movementManager) {
        this.player = player;
        this.rootNode = rootNode;
        this.movementManager = movementManager; // This "plugs in" the manager
    }

    public void updatePhysics(float tpf) {
        if (player.isGhostMode()) return;

        // 1. Gravity Logic
        float vY = player.getyVelocity();
        vY += player.getGravity() * tpf;
        player.setyVelocity(vY);

        // 2. WALL COLLISION (The part that uses movementManager)
        if (movementManager != null) {
            Vector3f moveDir = movementManager.getCurrentMoveDirection();
            
            if (moveDir.lengthSquared() > 0) {
                // Check for walls at knee and chest height
                float[] checkHeights = {0.5f, 1.2f};
                for (float h : checkHeights) {
                    Ray wallRay = new Ray(player.position.add(0, h, 0), moveDir);
                    CollisionResults wallResults = new CollisionResults();
                    rootNode.collideWith(wallRay, wallResults);
                    
                    if (wallResults.size() > 0) {
                        float dist = wallResults.getClosestCollision().getDistance();
                        if (dist < 0.6f) { // If hitting a block
                            // Push the player back slightly to keep them outside the block
                            Vector3f pushBack = moveDir.mult(0.6f - dist).negate();
                            player.position.addLocal(pushBack);
                        }
                    }
                }
            }
        }

        // 3. GROUND COLLISION
        Ray groundRay = new Ray(player.position.add(0, 0.5f, 0), Vector3f.UNIT_Y.negate());
        CollisionResults groundResults = new CollisionResults();
        rootNode.collideWith(groundRay, groundResults);

        if (groundResults.size() > 0) {
            CollisionResult closest = groundResults.getClosestCollision();
            if (closest.getDistance() <= 0.51f && vY <= 0) {
                player.setyVelocity(0);
                player.position.y = closest.getContactPoint().y;
            } else {
                player.position.y += vY * tpf;
            }
        } else {
            player.position.y += vY * tpf;
        }
        
        // Void Respawn
        if (player.position.y < -50) {
            player.position.set(0, 10, 0);
            player.setyVelocity(0);
        }
    }
}