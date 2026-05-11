/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mygame;

import com.jme3.asset.AssetManager;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;

public class RaycastManager 
{
    private Camera cam;
    private Node rootNode;
    private AssetManager assetManager;
    private Geometry selectionOutline;

    public RaycastManager(Camera cam, Node rootNode, AssetManager assetManager) 
    {
        this.cam = cam;
        this.rootNode = rootNode;
        this.assetManager = assetManager;
        initOutline();
    }

    // Creates a wireframe box that will follow our crosshair target
    private void initOutline() 
    {
        Box box = new Box(0.51f, 0.51f, 0.51f); // Slightly larger than a block
        selectionOutline = new Geometry("SelectionOutline", box);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.getAdditionalRenderState().setWireframe(true);
        mat.setColor("Color", ColorRGBA.White);
        selectionOutline.setMaterial(mat);
    }

    public void update(float tpf) 
    {
        CollisionResults results = getRaycastResults();
        
        if (results.size() > 0) 
        {
            Geometry target = results.getClosestCollision().getGeometry();
            // Move the outline to the target block
            selectionOutline.setLocalTranslation(target.getLocalTranslation());
            if (selectionOutline.getParent() == null) 
            {
                rootNode.attachChild(selectionOutline);
            }
        } 
        else 
        {
            // Remove outline if we aren't looking at anything
            selectionOutline.removeFromParent();
        }
    }

    public CollisionResults getRaycastResults() 
    {
        // Using cam.getLocation and cam.getDirection is what fixes the accuracy!
        Ray ray = new Ray(cam.getLocation(), cam.getDirection());
        CollisionResults results = new CollisionResults();
        rootNode.collideWith(ray, results);
        return results;
    }
    public void placeBlock() 
    {
    // 1. Create a Ray from the camera pointing forward
    Ray ray = new Ray(cam.getLocation(), cam.getDirection());
    CollisionResults results = new CollisionResults();
    
    // 2. See what we are looking at
    rootNode.collideWith(ray, results);

    if (results.size() > 0) 
    {
        CollisionResult closest = results.getClosestCollision();
        
        // 3. Get the contact point and the normal (which way the face is pointing)
        Vector3f contactPoint = closest.getContactPoint();
        Vector3f faceNormal = closest.getContactNormal();
        
        // 4. Calculate the new block position
        // We move the contact point slightly in the direction of the face normal
        // to ensure the new block sits ON TOP of the face, not inside it.
        Vector3f newBlockPos = contactPoint.add(faceNormal.mult(0.5f));
        
        // 5. Grid Snapping
        // This makes sure blocks align perfectly like Minecraft
        float x = Math.round(newBlockPos.x);
        float y = Math.round(newBlockPos.y);
        float z = Math.round(newBlockPos.z);

        // 6. Create the Box Geometry
        Box box = new Box(0.5f, 0.5f, 0.5f); // A 1x1x1 cube
        Geometry geom = new Geometry("WorldBlock", box); // CRITICAL: Name must be "WorldBlock"
        
        // 7. Apply Material (Blue)
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", com.jme3.math.ColorRGBA.Blue);
        geom.setMaterial(mat);
        
        // 8. Place and Attach
        geom.setLocalTranslation(x, y, z);
        rootNode.attachChild(geom);
        
        //System.out.println("Placed block at: " + x + ", " + y + ", " + z);
    }
}
       public void deleteBlock() 
       {
    // 1. Create a ray from camera location pointing forward
    Ray ray = new Ray(cam.getLocation(), cam.getDirection());
    CollisionResults results = new CollisionResults();
    
    // 2. Check collisions with everything in the world
    rootNode.collideWith(ray, results);

    if (results.size() > 0)
    {
        Geometry target = results.getClosestCollision().getGeometry();
        String hitName = target.getName();

        // 3. Only delete if the name matches exactly
        if (hitName.equals("WorldBlock")) 
        {
            target.removeFromParent();
            //System.out.println("Block Deleted!");
        }
    }
}
}
