package com.mygame;

import com.jme3.collision.CollisionResults;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;

public class PhysicsEngine {
    private Player player;
    private Node rootNode;
    private MovementManager movementManager;

    public PhysicsEngine(Player player, Node rootNode, MovementManager movementManager) {
        this.player = player;
        this.rootNode = rootNode;
        this.movementManager = movementManager;
    }

    public void updatePhysics(float tpf) {
        if (player.isGhostMode()) return;

        // 1. Calculate Vertical Movement
        float vY = player.getyVelocity();
        vY += player.getGravity() * tpf;
        player.setyVelocity(vY);
        float deltaY = vY * tpf;

        // 2. Calculate Horizontal Movement
        Vector3f moveDir = movementManager.getCurrentMoveDirection();
        float speed = player.getMoveSpeed() * tpf;
        float deltaX = moveDir.x * speed;
        float deltaZ = moveDir.z * speed;

        // 3. Step-by-Step Prevention (Check faces before moving)
        
        // Handle Y Axis
        player.position.y += deltaY;
        checkFaceCollision(true);

        // Handle X Axis
        player.position.x += deltaX;
        checkFaceCollision(false);

        // Handle Z Axis
        player.position.z += deltaZ;
        checkFaceCollision(false);

        // Void Respawn
        if (player.position.y < -50) {
            player.position.set(0, 10, 0);
            player.setyVelocity(0);
        }
    }

    private void checkFaceCollision(boolean isVertical) {
        float playerHalfWidth = 0.3f;
        float playerHalfHeight = 0.9f;
        Vector3f playerCenter = player.position.add(0, playerHalfHeight, 0);

        com.jme3.bounding.BoundingBox playerBox = new com.jme3.bounding.BoundingBox(
            playerCenter, playerHalfWidth, playerHalfHeight, playerHalfWidth
        );

        CollisionResults results = new CollisionResults();
        rootNode.collideWith(playerBox, results);

        for (com.jme3.collision.CollisionResult res : results) {
            Geometry geom = res.getGeometry();
            if (geom.getName().equals("WorldBlock") || geom.getName().equals("Floor")) {
                com.jme3.bounding.BoundingBox blockBox = (com.jme3.bounding.BoundingBox) geom.getWorldBound();

                if (playerBox.intersects(blockBox)) {
                    // STOP at the face
                    if (isVertical) {
                        if (player.getyVelocity() < 0) {
                            // Hit Top Face
                            player.position.y = blockBox.getCenter().y + blockBox.getYExtent();
                        } else {
                            // Hit Bottom Face
                            player.position.y = blockBox.getCenter().y - blockBox.getYExtent() - (playerHalfHeight * 2);
                        }
                        player.setyVelocity(0);
                    } else {
                        // Resolve horizontal by pushing back to the edge of the face
                        Vector3f bCenter = blockBox.getCenter();
                        float dx = playerCenter.x - bCenter.x;
                        float dz = playerCenter.z - bCenter.z;

                        float overlapX = (playerHalfWidth + blockBox.getXExtent()) - Math.abs(dx);
                        float overlapZ = (playerHalfWidth + blockBox.getZExtent()) - Math.abs(dz);

                        // The face comparison: resolve on the axis currently being updated
                        if (overlapX < overlapZ) {
                            player.position.x += (dx > 0) ? overlapX : -overlapX;
                        } else {
                            player.position.z += (dz > 0) ? overlapZ : -overlapZ;
                        }
                    }
                    // Sync the center for the next potential block in the loop
                    playerCenter = player.position.add(0, playerHalfHeight, 0);
                    playerBox.setCenter(playerCenter);
                }
            }
        }
    }
}