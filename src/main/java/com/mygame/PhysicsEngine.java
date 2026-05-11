package com.mygame;

import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;

public class PhysicsEngine 
{
    private Player player;
    private Node rootNode;
    private MovementManager movementManager; 

    public PhysicsEngine(Player player, Node rootNode, MovementManager movementManager) 
    {
        this.player = player;
        this.rootNode = rootNode;
        this.movementManager = movementManager; 
    }
   public void updatePhysics(float tpf) {
    if (player.isGhostMode()) return;

    // 1. Gravity and Y movement
    float vY = player.getyVelocity();
    vY += player.getGravity() * tpf;
    player.setyVelocity(vY);
    player.position.y += vY * tpf;

    // 2. Define the Player's Rectangle (AABB)
    float playerHalfWidth = 0.3f;
    float playerHalfHeight = 0.9f;
    Vector3f playerCenter = player.position.add(0, playerHalfHeight, 0);
    
    com.jme3.bounding.BoundingBox playerBox = new com.jme3.bounding.BoundingBox(
        playerCenter, playerHalfWidth, playerHalfHeight, playerHalfWidth
    );

    // 3. Check for collisions
    CollisionResults results = new CollisionResults();
    rootNode.collideWith(playerBox, results);

    // Use a for-each loop to avoid the .getCollision(i) error
    for (com.jme3.collision.CollisionResult res : results) {
        Geometry geom = res.getGeometry();
        
        if (geom.getName().equals("WorldBlock") || geom.getName().equals("Floor")) {
            // Get the block's bounding box to compare faces
            com.jme3.bounding.BoundingBox blockBox = (com.jme3.bounding.BoundingBox) geom.getWorldBound();
            
            if (playerBox.intersects(blockBox)) {
                // Check if we hit the TOP face (standing on ground)
                // If player's center is higher than the block's center, it's a floor hit
                if (playerCenter.y > blockBox.getCenter().y && vY <= 0) {
                    player.position.y = blockBox.getCenter().y + blockBox.getYExtent();
                    player.setyVelocity(0);
                } 
                // Otherwise, it's a SIDE face (wall hit)
                else {
                   Vector3f moveDir = movementManager.getCurrentMoveDirection();
                   // Push the player back slightly opposite to their movement direction
                   player.position.addLocal(moveDir.mult(-0.02f));
                }
            }
        }
    }

    // Void Respawn logic
    if (player.position.y < -50) {
        player.position.set(0, 10, 0);
        player.setyVelocity(0);
    }
}
}