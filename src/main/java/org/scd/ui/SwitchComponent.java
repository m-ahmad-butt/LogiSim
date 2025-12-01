package org.scd.ui;

import javax.swing.*;
import java.awt.*;


public class SwitchComponent extends JLabel {
    private int positionX;
    private int positionY;
    private boolean isOn;
    private int row;    // Row position in grid
    private int column; // Column position in grid
    
    // Store original border for reset
    private Color originalBorderColor = Color.LIGHT_GRAY;
    
    public SwitchComponent(int x, int y) {
        this.positionX = x;
        this.positionY = y;
        this.isOn = false; // Start in OFF state
        
        initComponent();
    }

    private void initComponent() {
        // Load default OFF image
        loadImage(isOn);
        
        // Set bounds (same size as LED for consistency)
        setBounds(positionX, positionY, 60, 60);
        setHorizontalAlignment(SwingConstants.CENTER);
        
        // Make the component opaque with white background
        setOpaque(true);
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(originalBorderColor, 1));
        
        // Add click and drag listeners
        final Point[] dragOffset = {null};
        
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                // Notify parent for connector mode
                // Notify parent for connector mode
                Container parent = getParent();
                if (parent instanceof CircuitCanvas) {
                    CircuitCanvas canvas = (CircuitCanvas) parent;
                    
                    if (canvas.isDeleteMode()) {
                        canvas.handleComponentClick(SwitchComponent.this);
                    } else if (!canvas.isConnectorMode()) {
                        toggle();
                    } else {
                        // In connector mode, handle connection
                        canvas.handleComponentClick(SwitchComponent.this);
                    }
                }
            }
            
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                // Store offset for smooth dragging
                dragOffset[0] = e.getPoint();
            }
            
            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                dragOffset[0] = null;
            }
        });
        
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseDragged(java.awt.event.MouseEvent e) {
                if (dragOffset[0] != null) {
                    // Calculate new position
                    Point parentPoint = SwingUtilities.convertPoint(SwitchComponent.this, e.getPoint(), getParent());
                    int newX = parentPoint.x - dragOffset[0].x;
                    int newY = parentPoint.y - dragOffset[0].y;
                    
                    // Check for overlap before moving
                    Container parent = getParent();
                    if (parent instanceof CircuitCanvas) {
                        CircuitCanvas canvas = (CircuitCanvas) parent;
                        Rectangle newBounds = new Rectangle(newX, newY, getWidth(), getHeight());
                        
                        // Only move if no overlap (excluding self)
                        if (!canvas.checkOverlap(newBounds, SwitchComponent.this)) {
                            // Update position
                            setLocation(newX, newY);
                            positionX = newX;
                            positionY = newY;
                            
                            // Update row/column for wire routing
                            int col = (newX - 20 + 25) / (150 + 50);
                            int row = (newY - 20 + 40) / (80 + 80);
                            col = Math.max(0, col);
                            row = Math.max(0, row);
                            setRowColumn(row, col);
                            
                            // Repaint canvas to update wires
                            canvas.repaint();
                        }
                    }
                }
            }
        });
    }
    
    /**
     * Toggle the switch state between ON (1) and OFF (0)
     */
    public void toggle() {
        isOn = !isOn;
        loadImage(isOn);
        
        // Update all connected components
        Container parent = getParent();
        if (parent instanceof CircuitCanvas) {
            ((CircuitCanvas) parent).updateCircuitFromSwitch(this);
        }
    }
  
    private void loadImage(boolean on) {
        ResourcePath resourcePath = ResourcePath.getInstance();
        String imagePath = on ? resourcePath.getSwitchOn() : resourcePath.getSwitchOff();
        
        try {
            java.net.URL imgURL = getClass().getClassLoader().getResource(imagePath);
            if (imgURL != null) {
                ImageIcon icon = new ImageIcon(imgURL);
                Image scaledImage = icon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
                setIcon(new ImageIcon(scaledImage));
                setText(""); // Clear text if image loads
            } else {
                // Fallback: show text if image not found
                setText("SW " + (on ? " ON" : " OFF"));
                setForeground(on ? Color.GREEN : Color.RED);
                setHorizontalAlignment(SwingConstants.CENTER);
            }
        } catch (Exception e) {
            // Fallback: show text if image not found
            setText("SW " + (on ? " ON" : " OFF"));
            setForeground(on ? Color.GREEN : Color.RED);
            setHorizontalAlignment(SwingConstants.CENTER);
        }
    }
    
    /**
     * Get the current output value of the switch (0 or 1)
     */
    public Integer getOutput() {
        return isOn ? 1 : 0;
    }
    
    // Getters
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
    
    public Point getOutputPoint() {
        // Output point is at the right side center of the switch
        return new Point(positionX + 60, positionY + 30);
    }
    
    /**
     * Highlight this component with a green border to indicate it's selected as source
     */
    public void setSelectedAsSrc() {
        setBorder(BorderFactory.createLineBorder(Color.GREEN, 3));
    }
    
    /**
     * Reset border to original color
     */
    public void resetBorder() {
        setBorder(BorderFactory.createLineBorder(originalBorderColor, 1));
    }
}
