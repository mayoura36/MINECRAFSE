/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mygame;

import com.jme3.asset.AssetManager;
import com.jme3.collision.CollisionResults;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;

public class RaycastManager {
    private Camera cam;
    private Node rootNode;
    private AssetManager assetManager;
    private Geometry selectionOutline;

    public RaycastManager(Camera cam, Node rootNode, AssetManager assetManager) {
        this.cam = cam;
        this.rootNode = rootNode;
        this.assetManager = assetManager;
        initOutline();
    }

    // Creates a wireframe box that will follow our crosshair target
    private void initOutline() {
        Box box = new Box(0.51f, 0.51f, 0.51f); // Slightly larger than a block
        selectionOutline = new Geometry("SelectionOutline", box);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.getAdditionalRenderState().setWireframe(true);
        mat.setColor("Color", ColorRGBA.White);
        selectionOutline.setMaterial(mat);
    }

    public void update(float tpf) {
        CollisionResults results = getRaycastResults();
        
        if (results.size() > 0) {
            Geometry target = results.getClosestCollision().getGeometry();
            // Move the outline to the target block
            selectionOutline.setLocalTranslation(target.getLocalTranslation());
            if (selectionOutline.getParent() == null) {
                rootNode.attachChild(selectionOutline);
            }
        } else {
            // Remove outline if we aren't looking at anything
            selectionOutline.removeFromParent();
        }
    }

    public CollisionResults getRaycastResults() {
        // Using cam.getLocation and cam.getDirection is what fixes the accuracy!
        Ray ray = new Ray(cam.getLocation(), cam.getDirection());
        CollisionResults results = new CollisionResults();
        rootNode.collideWith(ray, results);
        return results;
    }

    public void placeBlock() {
        CollisionResults results = getRaycastResults();
        if (results.size() > 0) {
            Vector3f hitPoint = results.getClosestCollision().getContactPoint();
            // Use Math.round to snap to the grid
            float x = Math.round(hitPoint.x);
            float y = Math.round(hitPoint.y + 0.5f);
            float z = Math.round(hitPoint.z);

            Box box = new Box(0.5f, 0.5f, 0.5f);
            Geometry cube = new Geometry("WorldBlock", box);
            Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            mat.setColor("Color", ColorRGBA.Blue);
            cube.setMaterial(mat);
            cube.setLocalTranslation(new Vector3f(x, y, z));
            rootNode.attachChild(cube);
        }
    }

    public void deleteBlock() {
    // 1. Create the Ray from the center of the camera
    Ray ray = new Ray(cam.getLocation(), cam.getDirection());
    CollisionResults results = new CollisionResults();
    
    // 2. Scan the rootNode for hits
    rootNode.collideWith(ray, results);

    if (results.size() > 0) {
        // Get the closest thing the crosshair is touching
        Geometry target = results.getClosestCollision().getGeometry();
        
        // 3. Safety Check: Only delete if it's a WorldBlock
        // We check the name so we don't delete the Floor or the Selection Outline!
        if (target.getName().equals("WorldBlock")) {
            target.removeFromParent(); // This deletes the block from the game
        } else if (target.getName().equals("Floor")) {
            System.out.println("You cannot delete the floor!");
        }
    }
}
}
