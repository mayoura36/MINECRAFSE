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
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.font.BitmapText;
import com.jme3.math.Vector3f;

public class Main extends SimpleApplication {

    private Player player;
    private MovementManager movementManager;
    private PhysicsEngine physicsEngine;
    private RaycastManager raycastManager;

    // This handles single-press actions (Jump, Place, Delete)
    private final ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if (name.equals("Forward")) movementManager.setForward(isPressed);
            else if (name.equals("Back")) movementManager.setBack(isPressed);
            else if (name.equals("Left")) movementManager.setLeft(isPressed);
            else if (name.equals("Right")) movementManager.setRight(isPressed);
            
            else if (name.equals("Jump") && isPressed) {
                player.jump();
            }
            else if (name.equals("Shoot") && isPressed) {
                raycastManager.placeBlock();
            } 
            else if (name.equals("Delete") && isPressed) {
                raycastManager.deleteBlock();
            }
            else if (name.equals("ToggleGhost") && isPressed) {
                player.toggleGhostMode();
            }
        }
    };

    // This handles continuous actions (Looking around)
    private final AnalogListener analogListener = new AnalogListener() {
        @Override
        public void onAnalog(String name, float value, float tpf) {
            if (name.equals("MouseRight")) player.rotate(-value, 0);
            if (name.equals("MouseLeft"))  player.rotate(value, 0);
            if (name.equals("MouseUp"))    player.rotate(0, value);
            if (name.equals("MouseDown"))  player.rotate(0, -value);
            
            if (name.equals("SpeedUp"))    player.adjustSpeed(1.0f);
            if (name.equals("SpeedDown"))  player.adjustSpeed(-1.0f);
        }
    };

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        // --- STEP 1: INITIALIZATION ORDER IS CRITICAL ---
        player = new Player();
        
        // 1. Create movement first
        movementManager = new MovementManager(player);
        
        // 2. Pass movement into physics so physics can check for walls
        physicsEngine = new PhysicsEngine(player, rootNode, movementManager);
        
        // 3. Setup interaction
        raycastManager = new RaycastManager(cam, rootNode, assetManager);

        // --- STEP 2: WORLD SETUP ---
        Box floorBox = new Box(40, 0.1f, 40); 
        Geometry floorGeom = new Geometry("Floor", floorBox);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Green);
        floorGeom.setMaterial(mat);
        rootNode.attachChild(floorGeom);

        flyCam.setEnabled(false);
        mouseInput.setCursorVisible(false); 

        // --- STEP 3: INPUT MAPPINGS ---
        inputManager.addMapping("Forward", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Back",    new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Left",    new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Right",   new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Jump",    new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("ToggleGhost", new KeyTrigger(KeyInput.KEY_C));
        
        inputManager.addMapping("Shoot",   new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addMapping("Delete",  new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        
        inputManager.addMapping("MouseLeft",  new MouseAxisTrigger(MouseInput.AXIS_X, true));
        inputManager.addMapping("MouseRight", new MouseAxisTrigger(MouseInput.AXIS_X, false));
        inputManager.addMapping("MouseUp",    new MouseAxisTrigger(MouseInput.AXIS_Y, true));
        inputManager.addMapping("MouseDown",  new MouseAxisTrigger(MouseInput.AXIS_Y, false));
        
        inputManager.addMapping("SpeedUp",    new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));
        inputManager.addMapping("SpeedDown",  new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));

        inputManager.addListener(actionListener, "Forward", "Back", "Left", "Right", "Jump", "ToggleGhost", "Shoot", "Delete");
        inputManager.addListener(analogListener, "MouseLeft", "MouseRight", "MouseUp", "MouseDown", "SpeedUp", "SpeedDown");

        initUI();
    }

    private void initUI() {
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        
        // Setup Crosshair
        BitmapText ch = new BitmapText(guiFont, false);
        ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
        ch.setText("+"); 
        float centerX = settings.getWidth() / 2 - ch.getLineWidth() / 2;
        float centerY = settings.getHeight() / 2 + ch.getLineHeight() / 2;
        ch.setLocalTranslation(centerX, centerY, 0);
        guiNode.attachChild(ch);

        // Setup HUD
        BitmapText hud = new BitmapText(guiFont, false);
        hud.setName("CoordHUD");
        hud.setSize(guiFont.getCharSet().getRenderedSize());
        hud.setLocalTranslation(20, settings.getHeight() - 20, 0);
        guiNode.attachChild(hud);
    }

    @Override
    public void simpleUpdate(float tpf) {
        // Run all logic updates
        movementManager.updateMovement(tpf);
        physicsEngine.updatePhysics(tpf);
        raycastManager.update(tpf);

        // Position the camera at eye level (1.6 units above feet)
        cam.setLocation(player.position.add(0, 1.6f, 0));
        
        // Rotate the camera to match player view
        Quaternion q = new Quaternion();
        q.fromAngles(player.pitch, player.yaw, 0); 
        cam.setRotation(q);

        // Update the HUD display
        BitmapText hud = (BitmapText) guiNode.getChild("CoordHUD");
        if (hud != null) {
            hud.setText("X: " + (int)player.position.x + 
                         " Y: " + (int)player.position.y + 
                         " Z: " + (int)player.position.z +
                         " | Speed: " + player.getMoveSpeed());
        }
    }
}