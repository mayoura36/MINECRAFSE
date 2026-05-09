package com.mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.input.KeyInput; 
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener; 
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger; 
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Ray;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.font.BitmapText;
import com.jme3.math.Vector3f;
public class Main extends SimpleApplication 
{
    private Player player;
    private final ActionListener actionListener = new ActionListener() {
    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (name.equals("Forward"))
        {
            player.setForward(isPressed);
        } 
        else if (name.equals("Back")) 
        {
            player.setBack(isPressed);
        } 
        else if (name.equals("Shoot") && isPressed) 
        {
            Ray ray = new Ray(cam.getLocation(), cam.getDirection());
            CollisionResults results = new CollisionResults();
            rootNode.collideWith(ray, results);
            if (results.size() > 0) 
            {
                Vector3f hitPoint = results.getClosestCollision().getContactPoint();
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
        else if (name.equals("Delete") && isPressed) 
        {
            Ray ray = new Ray(cam.getLocation(), cam.getDirection());
            CollisionResults results = new CollisionResults();
            rootNode.collideWith(ray, results);
            if (results.size() > 0) {
                Geometry target = results.getClosestCollision().getGeometry();
                if (!target.getName().equals("Floor")) 
                {
                    target.removeFromParent();
                }
            }
        }
        else if (name.equals("ToggleGhost") && isPressed) {
    player.toggleGhostMode();
}
    }
};
    private final AnalogListener analogListener = new AnalogListener() {
    @Override
    public void onAnalog(String name, float value, float tpf) {
        if (name.equals("MouseRight")) player.rotate(-value, 0);
        if (name.equals("MouseLeft"))  player.rotate(value, 0);
        if (name.equals("MouseUp"))    player.rotate(0, value);
        if (name.equals("MouseDown"))  player.rotate(0, -value);
        
        // NEW: Speed control logic
        if (name.equals("SpeedUp"))    player.adjustSpeed(1.0f);
        if (name.equals("SpeedDown"))  player.adjustSpeed(-1.0f);
    }
};
    public static void main(String[] args) 
    {
        Main app = new Main();
        app.start();
    }
    @Override
    public void simpleInitApp() 
    {
        player = new Player();
        Box floorBox = new Box(20, 0.1f, 20); 
        Geometry floorGeom = new Geometry("Floor", floorBox);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Green);
        floorGeom.setMaterial(mat);
        rootNode.attachChild(floorGeom);
        flyCam.setEnabled(false);
        // Make the mouse cursor invisible so it feels like a game
        mouseInput.setCursorVisible(false); 

        // Keyboard Mappings
        inputManager.addMapping("Forward", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Back", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addListener(actionListener, "Forward", "Back");

        // Mouse Mappings
        inputManager.addMapping("MouseLeft", new MouseAxisTrigger(MouseInput.AXIS_X, true));
        inputManager.addMapping("MouseRight", new MouseAxisTrigger(MouseInput.AXIS_X, false));
        inputManager.addListener(analogListener, "MouseLeft", "MouseRight");
        // Add this to your other mappings in simpleInitApp
// Replace the MouseButtonTrigger with a KeyTrigger
inputManager.addMapping("Shoot", new KeyTrigger(KeyInput.KEY_SPACE)); 
inputManager.addListener(actionListener, "Shoot");
guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
BitmapText ch = new BitmapText(guiFont, false);
ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
ch.setText("+"); // The crosshair
ch.setLocalTranslation(settings.getWidth() / 2 - ch.getLineWidth()/2, 
                       settings.getHeight() / 2 + ch.getLineHeight()/2, 0);
guiNode.attachChild(ch);
inputManager.addMapping("MouseUp", new MouseAxisTrigger(MouseInput.AXIS_Y, true));
inputManager.addMapping("MouseDown", new MouseAxisTrigger(MouseInput.AXIS_Y, false));
inputManager.addListener(analogListener, "MouseUp", "MouseDown");
inputManager.addMapping("Delete", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
inputManager.addListener(actionListener, "Delete");
BitmapText hud = new BitmapText(guiFont, false);
hud.setName("CoordHUD");
hud.setSize(guiFont.getCharSet().getRenderedSize());
hud.setLocalTranslation(20, settings.getHeight() - 20, 0);
guiNode.attachChild(hud);
// Scroll Up increases speed, Scroll Down decreases it
inputManager.addMapping("SpeedUp", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));
inputManager.addMapping("SpeedDown", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));

// Add them to your analogListener
inputManager.addListener(analogListener, "SpeedUp", "SpeedDown");
inputManager.addMapping("ToggleGhost", new KeyTrigger(KeyInput.KEY_C));
inputManager.addListener(actionListener, "ToggleGhost");
    }
 @Override
public void simpleUpdate(float tpf) {
    // 1. Update player physics and coordinates
    player.update(tpf);

    // 2. Sync Camera position to player (1.6f is eye-height)
    cam.setLocation(player.position.add(0, 1.6f, 0));

    // 3. Sync Camera rotation (Pitch and Yaw)
    Quaternion q = new Quaternion();
    q.fromAngles(player.pitch, player.yaw, 0); 
    cam.setRotation(q);

    // 4. Update the On-Screen HUD with coordinates
    BitmapText hud = (BitmapText) guiNode.getChild("CoordHUD");
    if (hud != null) {
        hud.setText("X: " + (int)player.position.x + 
                     " Y: " + (int)player.position.y + 
                     " Z: " + (int)player.position.z);
    }
    
    // Optional: Keep this for debugging in the console
    // System.out.println("Current Pos: " + player.position);
}
}