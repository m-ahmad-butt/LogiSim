package org.scd.ui;

import org.scd.business.service.CircuitService;

import javax.swing.*;
import java.awt.*;


public class LEDComponent extends JLabel {
    private int componentId; // Store only the ID, not the model
    private CircuitService service; 
    
    private ComponentInput input;
    private int positionX;
    private int positionY;
    private boolean isOn;
    private int row;    // Row position in grid
    private int column; // Column position in grid
    
    public LEDComponent(int x, int y) {
        this.service = CircuitService.getInstance();
        this.positionX = x;
        this.positionY = y;
        this.input = new ComponentInput(0);
        this.isOn = false;
        
        // Add LED through service and store only the component ID
        this.componentId = service.getLEDComponentId(service.addLED(x, y));
        service.registerUIComponent(this.componentId, this);
        
        // Load default OFF image
        loadImage(false);
        
        // Set bounds
        setBounds(x, y, 60, 60);
        setHorizontalAlignment(SwingConstants.CENTER);
        
        // Make the component opaque with white background
        setOpaque(true);
        setBackground(Color.WHITE);
        
        // Add click listener for connector mode
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                // Notify parent for connector mode
                Container parent = getParent();
                if (parent instanceof CircuitCanvas) {
                    ((CircuitCanvas) parent).handleComponentClick(LEDComponent.this);
                }
            }
        });
    }
    
  
    private void loadImage(boolean on) {
        ResourcePath resourcePath = ResourcePath.getInstance();
        String imagePath = on ? resourcePath.getLedOn() : resourcePath.getLedOff();
        
        try {
            java.net.URL imgURL = getClass().getClassLoader().getResource(imagePath);
            if (imgURL != null) {
                ImageIcon icon = new ImageIcon(imgURL);
                Image scaledImage = icon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
                setIcon(new ImageIcon(scaledImage));
                setText(""); // Clear text if image loads
            } else {
                // Fallback: show text if image not found
                setText("LED " + getComponentId() + (on ? " ON" : " OFF"));
                setForeground(on ? Color.GREEN : Color.RED);
                setHorizontalAlignment(SwingConstants.CENTER);
            }
        } catch (Exception e) {
            // Fallback: show text if image not found
            setText("LED " + getComponentId() + (on ? " ON" : " OFF"));
            setForeground(on ? Color.GREEN : Color.RED);
            setHorizontalAlignment(SwingConstants.CENTER);
        }
    }
    
  
    public void updateState() {
        Integer inputValue = input.getValue();
        if (inputValue != null) {
            Integer sourceId = (input.getSourceComponent() != null) ? 
                input.getSourceComponent().getComponentId() : null;
            // Use service to update LED input and get state
            service.setLEDInput(this.componentId, inputValue, sourceId);
            isOn = service.getLEDState(this.componentId);
            loadImage(isOn);
        }
    }
    
   
    public void setInputSource(GateComponent source) {
        input.setSourceComponent(source);
        // Update LED state based on source output
        if (source.getOutput() != null) {
            input.setValue(source.getOutput());
            updateState();
        }
    }
    
    // Getters
    public int getComponentId() {
        return this.componentId;
    }
    
    public ComponentInput getInput() {
        return input;
    }
    
    public void setRowColumn(int row, int column) {
        this.row = row;
        this.column = column;
    }
    
    public int getRow() {
        return row;
    }
    
    public int getColumn() {
        return column;
    }
    
    public int getPositionX() {
        return positionX;
    }
    
    public int getPositionY() {
        return positionY;
    }
    
    public boolean isOn() {
        return isOn;
    }
    
    public Point getInputPoint() {
        // Input point is on the left side of the LED
        return new Point(positionX, positionY + 30);
    }
}
