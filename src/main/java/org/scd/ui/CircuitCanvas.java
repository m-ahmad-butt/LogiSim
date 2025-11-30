package org.scd.ui;

import org.scd.business.model.Circuit;
import org.scd.business.model.Connector;
import org.scd.business.model.Gate;
import org.scd.business.model.LED;
import org.scd.business.service.CircuitService;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class CircuitCanvas extends JPanel {
    private CircuitService service; 
    private List<GateComponent> gates;
    private List<LEDComponent> leds;
    private List<WireConnection> wires;
    
    // Layout settings
    private int currentX = 20;
    private int currentY = 20;
    private int gateWidth = 150;  // Updated for new gate panel width
    private int gateHeight = 80;  // Updated for new gate panel height
    private int horizontalSpacing = 50; // Adjusted spacing
    private int verticalSpacing = 80;   // Increased from 30 to 80 for more wire space
    private int maxColumns = 5; // Reduced from 6 to 5 due to wider gates
    private int currentColumn = 0;
    private int currentRow = 0;
    
    // Position tracking for wire routing
    private List<Integer> rowYPositions;     // Y position of each row
    private List<Integer> columnXPositions;  // X position of each column
    private List<Integer> routingChannels;   // Y positions of routing channels between rows
    
    // Connector mode
    private boolean connectorMode = false;
    private boolean deleteMode = false;
    private GateComponent selectedSource = null;
    
    // Callback for component count updates
    private Runnable onComponentCountChange;
    
    public CircuitCanvas() {
        this.service = CircuitService.getInstance();
        gates = new ArrayList<>();
        leds = new ArrayList<>();
        wires = new ArrayList<>();
        rowYPositions = new ArrayList<>();
        columnXPositions = new ArrayList<>();
        routingChannels = new ArrayList<>();
        
        // Initialize first row and column positions
        rowYPositions.add(currentY);
        for (int i = 0; i < maxColumns; i++) {
            columnXPositions.add(currentX + (i * (gateWidth + horizontalSpacing)));
        }
        
        setLayout(null); 
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(1800, 1000));
    }
    
    public void refreshCircuit() {
        for (GateComponent gate : gates) {
            gate.updateImage();
        }
        for (LEDComponent led : leds) {
            led.updateImage();
        }
        repaint();
    }
    
  
    public void addGate(String gateType) {
        // Calculate position using grid
        int x = currentX + (currentColumn * (gateWidth + horizontalSpacing));
        int y = currentY + (currentRow * (gateHeight + verticalSpacing));
        
        addGate(gateType, x, y);
        
        // Update position for next gate
        currentColumn++;
        if (currentColumn >= maxColumns) {
            currentColumn = 0;
            currentRow++;
            
            // Add new row position and routing channel
            int newRowY = currentY + (currentRow * (gateHeight + verticalSpacing));
            rowYPositions.add(newRowY);
            
            // Routing channel is in the middle of the gap between rows
            int previousRowY = currentY + ((currentRow - 1) * (gateHeight + verticalSpacing));
            int channelY = previousRowY + gateHeight + (verticalSpacing / 2);
            routingChannels.add(channelY);
        }
        
        // Update preferred size if needed
        int requiredHeight = currentY + ((currentRow + 1) * (gateHeight + verticalSpacing)) + 50;
        if (requiredHeight > getPreferredSize().height) {
            setPreferredSize(new Dimension(800, requiredHeight));
            revalidate();
        }
    }
    
    /**
     * Add gate at specific coordinates (for drag-and-drop)
     */
    public void addGate(String gateType, int x, int y) {
        GateComponent gate = new GateComponent(gateType, x, y);
        
        // Calculate row/column from position for wire routing
        int col = (x - currentX + (horizontalSpacing / 2)) / (gateWidth + horizontalSpacing);
        int row = (y - currentY + (verticalSpacing / 2)) / (gateHeight + verticalSpacing);
        col = Math.max(0, col);
        row = Math.max(0, row);
        
        gate.setRowColumn(row, col);
        gates.add(gate);
        add(gate);
        
        repaint();
    }
    
  
    public void addLED() {
        // Calculate position using grid
        int x = currentX + (currentColumn * (gateWidth + horizontalSpacing));
        int y = currentY + (currentRow * (gateHeight + verticalSpacing));
        
        addLED(x, y);
        
        // Update position for next component
        currentColumn++;
        if (currentColumn >= maxColumns) {
            currentColumn = 0;
            currentRow++;
            
            // Add new row position and routing channel
            int newRowY = currentY + (currentRow * (gateHeight + verticalSpacing));
            rowYPositions.add(newRowY);
            
            // Routing channel is in the middle of the gap between rows
            int previousRowY = currentY + ((currentRow - 1) * (gateHeight + verticalSpacing));
            int channelY = previousRowY + gateHeight + (verticalSpacing / 2);
            routingChannels.add(channelY);
        }
        
        // Update preferred size if needed
        int requiredHeight = currentY + ((currentRow + 1) * (gateHeight + verticalSpacing)) + 50;
        if (requiredHeight > getPreferredSize().height) {
            setPreferredSize(new Dimension(800, requiredHeight));
            revalidate();
        }
    }
    
    /**
     * Add LED at specific coordinates (for drag-and-drop)
     */
    public void addLED(int x, int y) {
        LEDComponent led = new LEDComponent(x, y);
        
        // Calculate row/column from position for wire routing
        int col = (x - currentX + (horizontalSpacing / 2)) / (gateWidth + horizontalSpacing);
        int row = (y - currentY + (verticalSpacing / 2)) / (gateHeight + verticalSpacing);
        col = Math.max(0, col);
        row = Math.max(0, row);
        
        led.setRowColumn(row, col);
        leds.add(led);
        add(led);
        
        repaint();
    }
    
    /**
     * Check if a rectangle overlaps with any existing components
     * @param bounds The rectangle to check
     * @param excludeComponent Component to exclude from check (for repositioning)
     * @return true if overlaps, false otherwise
     */
    public boolean checkOverlap(Rectangle bounds, Object excludeComponent) {
        // Check overlap with gates
        for (GateComponent gate : gates) {
            if (gate != excludeComponent) {
                Rectangle gateBounds = gate.getBounds();
                if (bounds.intersects(gateBounds)) {
                    return true;
                }
            }
        }
        
        // Check overlap with LEDs
        for (LEDComponent led : leds) {
            if (led != excludeComponent) {
                Rectangle ledBounds = led.getBounds();
                if (bounds.intersects(ledBounds)) {
                    return true;
                }
            }
        }
        
        return false;
    }
 
    public void setConnectorMode(boolean enabled) {
        this.connectorMode = enabled;
        if (enabled) {
            this.deleteMode = false; // Mutually exclusive
            selectedSource = null;
        } else {
            if (selectedSource != null) {
                selectedSource.resetBorder();
            }
            selectedSource = null;
        }
    }
    
    public void setDeleteMode(boolean enabled) {
        this.deleteMode = enabled;
        if (enabled) {
            this.connectorMode = false; // Mutually exclusive
            if (selectedSource != null) {
                selectedSource.resetBorder();
                selectedSource = null;
            }
        }
    }
    
   
    public void toggleConnectorMode() {
        setConnectorMode(!connectorMode);
    }
    
    public void toggleDeleteMode() {
        setDeleteMode(!deleteMode);
    }
    
   
    public boolean isConnectorMode() {
        return connectorMode;
    }
    
    public boolean isDeleteMode() {
        return deleteMode;
    }
    
    public void setOnComponentCountChange(Runnable onComponentCountChange) {
        this.onComponentCountChange = onComponentCountChange;
    }
    
   
    public void handleComponentClick(Object component) {
        if (deleteMode) {
            handleDeleteClick(component);
            return;
        }
        
        if (!connectorMode) {
            return;
        }
        
        if (selectedSource == null) {
            // First click: select source gate
            if (component instanceof GateComponent) {
                GateComponent clickedGate = (GateComponent) component;
                if (clickedGate.getOutput() != null) {
                    selectedSource = clickedGate;
                    // Highlight with green border
                    selectedSource.setSelectedAsSrc();
                }
            }
        } else {
            // Second click: select target
            if (component instanceof GateComponent) {
                GateComponent clickedGate = (GateComponent) component;
                connectToGate(selectedSource, clickedGate);
            } else if (component instanceof LEDComponent) {
                LEDComponent clickedLED = (LEDComponent) component;
                connectToLED(selectedSource, clickedLED);
            }
            
            // Reset border and prepare for next connection
            selectedSource.resetBorder();
            selectedSource = null;
            // Connector mode stays enabled so user can make another connection
        }
    }
    
    private void handleDeleteClick(Object component) {
        if (component instanceof GateComponent) {
            GateComponent gate = (GateComponent) component;
            
            // Remove from service
            service.removeGate(gate.getComponentId());
            
            // Remove from UI list
            gates.remove(gate);
            
            // Remove from panel
            remove(gate);
            
            // Remove connected wires
            removeConnectedWires(gate);
            
        } else if (component instanceof LEDComponent) {
            LEDComponent led = (LEDComponent) component;
            
            // Remove from service
            service.removeLED(led.getComponentId());
            
            // Remove from UI list
            leds.remove(led);
            
            // Remove from panel
            remove(led);
            
            // Remove connected wires
            removeConnectedWires(led);
        }
        
        // Notify listener about count change
        if (onComponentCountChange != null) {
            onComponentCountChange.run();
        }
        
        repaint();
    }
    
    private void removeConnectedWires(Object component) {
        List<WireConnection> wiresToRemove = new ArrayList<>();
        
        for (WireConnection wire : wires) {
            if (wire.getSourceGate() == component || wire.getTargetComponent() == component) {
                wiresToRemove.add(wire);
                
                // Also remove from service
                // Note: We don't have direct mapping from WireConnection to Connector ID easily available
                // But we can clean up connectors in service that reference this component
                // Actually, service.removeGate/removeLED should handle cleaning up connectors in the model
                // But we need to clean up UI wires
            }
        }
        
        wires.removeAll(wiresToRemove);
    }
    
  
    private void connectToGate(GateComponent source, GateComponent target) {
        if (source == target) {
            JOptionPane.showMessageDialog(this, "Cannot connect a gate to itself!");
            return;
        }
        
        // Ask which input to connect to
        List<String> availableInputs = new ArrayList<>();
        if (!target.getInput1().isConnected()) {
            availableInputs.add("Input 1");
        }
        if (target.getInput2() != null && !target.getInput2().isConnected()) {
            availableInputs.add("Input 2");
        }
        
        if (availableInputs.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All inputs are already connected!");
            return;
        }
        
        String[] options = availableInputs.toArray(new String[0]);
        String selected = (String) JOptionPane.showInputDialog(this,
            "Select which input to connect to:",
            "Connect to Input",
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]);
        
        if (selected != null) {
            int inputIndex = selected.equals("Input 1") ? 0 : 1;
            
            // Count how many wires already exist from this source (for vertical offset)
            int wireIndexFromSource = 0;
            for (WireConnection existingWire : wires) {
                if (existingWire.getSourceGate() == source) {
                    wireIndexFromSource++;
                }
            }
            
            // Create wire connection with canvas reference for routing
            WireConnection wire = new WireConnection(source, target, inputIndex, this, wireIndexFromSource);
            wires.add(wire);
            
            // Update target input
            if (inputIndex == 0) {
                target.getInput1().setSourceComponent(source);
                target.getInput1().setValue(source.getOutput());
            } else {
                target.getInput2().setSourceComponent(source);
                target.getInput2().setValue(source.getOutput());
            }
            
            // Sync with Model
            service.addConnector(source.getComponentId(), target.getComponentId(), inputIndex, 
                    String.format("#%02x%02x%02x", wire.getWireColor().getRed(), wire.getWireColor().getGreen(), wire.getWireColor().getBlue()));

            // Recalculate target output
            target.calculateOutput();
            target.updateImage();
            
            repaint();
        }
    }
    
 
    private void connectToLED(GateComponent source, LEDComponent target) {
        if (target.getInput().isConnected()) {
            JOptionPane.showMessageDialog(this, "LED input is already connected!");
            return;
        }
        
        // Count how many wires already exist from this source (for vertical offset)
        int wireIndexFromSource = 0;
        for (WireConnection existingWire : wires) {
            if (existingWire.getSourceGate() == source) {
                wireIndexFromSource++;
            }
        }
        
        // Create wire connection with canvas reference for routing
        WireConnection wire = new WireConnection(source, target, 0, this, wireIndexFromSource);
        wires.add(wire);
        
        // Update LED
        target.setInputSource(source);
        target.updateState();
        
        // Sync with Model
        service.addConnector(source.getComponentId(), target.getComponentId(), 0, 
                String.format("#%02x%02x%02x", wire.getWireColor().getRed(), wire.getWireColor().getGreen(), wire.getWireColor().getBlue()));

        repaint();
        
        JOptionPane.showMessageDialog(this, 
            "Connected " + source.getGateType() + " Gate " + source.getComponentId() + 
            " to LED " + target.getComponentId());
    }
    
    public void clearCanvas() {
        removeAll();
        gates.clear();
        leds.clear();
        wires.clear();
        
        // Reset layout
        currentColumn = 0;
        currentRow = 0;
        rowYPositions.clear();
        columnXPositions.clear();
        routingChannels.clear();
        rowYPositions.add(currentY);
        for (int i = 0; i < maxColumns; i++) {
            columnXPositions.add(currentX + (i * (gateWidth + horizontalSpacing)));
        }
        
        repaint();
    }

    public void loadCircuit(Circuit circuit) {
        clearCanvas();
        service.setCurrentCircuit(circuit);
        
        // Track maximum row and column seen
        int maxRow = 0;
        int maxColumn = 0;
        
        // Load Gates
        for (Gate gate : circuit.getGates()) {
            GateComponent gc = new GateComponent(gate);
            gates.add(gc);
            add(gc);
            
            // Calculate row and column from position
            int x = gate.getPositionX();
            int y = gate.getPositionY();
            
            // Reverse calculate row and column from position
            int col = (x - currentX + (horizontalSpacing / 2)) / (gateWidth + horizontalSpacing);
            int row = (y - currentY + (verticalSpacing / 2)) / (gateHeight + verticalSpacing);
            
            // Ensure non-negative
            col = Math.max(0, col);
            row = Math.max(0, row);
            
            // Set row/column on the gate component
            gc.setRowColumn(row, col);
            
            // Update max values
            maxRow = Math.max(maxRow, row);
            maxColumn = Math.max(maxColumn, col);
        }
        
        // Load LEDs
        for (LED led : circuit.getLeds()) {
            LEDComponent lc = new LEDComponent(led);
            leds.add(lc);
            add(lc);
            
            // Calculate row and column from position
            int x = (int) led.getPositionX();
            int y = (int) led.getPositionY();
            
            int col = (x - currentX + (horizontalSpacing / 2)) / (gateWidth + horizontalSpacing);
            int row = (y - currentY + (verticalSpacing / 2)) / (gateHeight + verticalSpacing);
            
            col = Math.max(0, col);
            row = Math.max(0, row);
            
            lc.setRowColumn(row, col);
            
            maxRow = Math.max(maxRow, row);
            maxColumn = Math.max(maxColumn, col);
        }
        
        // Update layout tracking variables
        currentRow = maxRow;
        currentColumn = maxColumn + 1;
        if (currentColumn >= maxColumns) {
            currentColumn = 0;
            currentRow++;
        }
        
        // Rebuild rowYPositions and routingChannels based on loaded components
        rowYPositions.clear();
        routingChannels.clear();
        for (int r = 0; r <= maxRow; r++) {
            int rowY = currentY + (r * (gateHeight + verticalSpacing));
            rowYPositions.add(rowY);
            
            if (r > 0) {
                int previousRowY = currentY + ((r - 1) * (gateHeight + verticalSpacing));
                int channelY = previousRowY + gateHeight + (verticalSpacing / 2);
                routingChannels.add(channelY);
            }
        }
        
        // Load Connectors/Wires
        for (Connector conn : circuit.getConnectors()) {
            GateComponent source = findGateComponent(conn.getSourceComponentId());
            Object target = findComponent(conn.getTargetComponentId());
            
            if (source != null && target != null) {
                 // Count how many wires already exist from this source (for vertical offset)
                int wireIndexFromSource = 0;
                for (WireConnection existingWire : wires) {
                    if (existingWire.getSourceGate() == source) {
                        wireIndexFromSource++;
                    }
                }
                
                WireConnection wire = new WireConnection(source, target, conn.getTargetInputIndex(), this, wireIndexFromSource);
                wires.add(wire);
                
                // Update UI connections
                if (target instanceof GateComponent) {
                    GateComponent targetGate = (GateComponent) target;
                    if (conn.getTargetInputIndex() == 0) {
                        targetGate.getInput1().setSourceComponent(source);
                    } else {
                        targetGate.getInput2().setSourceComponent(source);
                    }
                } else if (target instanceof LEDComponent) {
                    LEDComponent targetLED = (LEDComponent) target;
                    targetLED.setInputSource(source);
                }
            }
        }
        
        revalidate();
        service.calculateCircuit();
        refreshCircuit();
        repaint();
    }

    private GateComponent findGateComponent(int id) {
        for (GateComponent gc : gates) {
            if (gc.getComponentId() == id) return gc;
        }
        return null;
    }

    private Object findComponent(int id) {
        GateComponent gc = findGateComponent(id);
        if (gc != null) return gc;
        for (LEDComponent lc : leds) {
            if (lc.getComponentId() == id) return lc;
        }
        return null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw all wires
        for (WireConnection wire : wires) {
            wire.updatePoints();
            wire.draw(g2d);
        }
        
        // Find and mark wire crossings with bridge symbols
        // Track which wires have crossings to vary bridge orientation
        java.util.Map<Point, Integer> crossingCounts = new java.util.HashMap<>();
        
        for (int i = 0; i < wires.size(); i++) {
            for (int j = i + 1; j < wires.size(); j++) {
                Point intersection = wires.get(i).findIntersection(wires.get(j));
                
                if (intersection != null) {
                    // Track how many times this point has been crossed
                    Point key = new Point(intersection.x, intersection.y);
                    int count = crossingCounts.getOrDefault(key, 0);
                    crossingCounts.put(key, count + 1);
                    
                    // Vary bridge orientation based on crossing count to make multiple crossings visible
                    boolean useVerticalBridge = (count % 2 == 1);
                    
                    // Draw a bridge/jump symbol - semicircular bump showing wire jumping over
                    // First, erase a small section of the lower wire with white background
                    g2d.setColor(Color.WHITE);
                    g2d.setStroke(new BasicStroke(5)); // Slightly thicker to create gap
                    
                    if (useVerticalBridge) {
                        // Vertical gap for odd numbered crossings
                        g2d.drawLine(intersection.x, intersection.y - 6, 
                                    intersection.x, intersection.y + 6);
                    } else {
                        // Horizontal gap for even numbered crossings
                        g2d.drawLine(intersection.x - 6, intersection.y, 
                                    intersection.x + 6, intersection.y);
                    }
                    
                    // Now draw the semicircular bridge for the top wire
                    g2d.setColor(wires.get(j).getWireColor());
                    g2d.setStroke(new BasicStroke(2));
                    
                    // Draw a semicircular arc (bridge) - orientation varies
                    int bridgeWidth = 12;
                    int bridgeHeight = 6;
                    
                    if (useVerticalBridge) {
                        // Vertical bridge (sideways arc) - like ⊃ shape
                        g2d.drawArc(intersection.x - bridgeHeight, 
                                   intersection.y - bridgeWidth/2, 
                                   bridgeHeight * 2, 
                                   bridgeWidth, 
                                   90, 180); // Right half of circle
                    } else {
                        // Horizontal bridge - like ∩ shape
                        g2d.drawArc(intersection.x - bridgeWidth/2, 
                                   intersection.y - bridgeHeight, 
                                   bridgeWidth, 
                                   bridgeHeight * 2, 
                                   0, 180); // Top half of circle
                    }
                }
            }
        }
    }
    
  
    public List<GateComponent> getGates() {
        return gates;
    }
    
  
    public List<LEDComponent> getLEDs() {
        return leds;
    }
    
  
    public List<WireConnection> getWires() {
        return wires;
    }
    
  
    public int getComponentCount() {
        return gates.size() + leds.size();
    }
    
  
    public List<Integer> getRowYPositions() {
        return rowYPositions;
    }
    
   
    public List<Integer> getColumnXPositions() {
        return columnXPositions;
    }
    
   
    public List<Integer> getRoutingChannels() {
        return routingChannels;
    }
    
   
    public int getGateWidth() {
        return gateWidth;
    }
    
    public int getGateHeight() {
        return gateHeight;
    }

    /**
     * Exports the entire circuit canvas to a PNG file.
     * Captures all components regardless of current scroll position.
     */
    public void exportToPNG(java.io.File file) throws java.io.IOException {
        // Calculate the actual bounds needed to capture all components
        int maxX = 0;
        int maxY = 0;
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        
        // Find bounds from gates
        for (GateComponent gate : gates) {
            minX = Math.min(minX, gate.getX());
            minY = Math.min(minY, gate.getY());
            maxX = Math.max(maxX, gate.getX() + gate.getWidth());
            maxY = Math.max(maxY, gate.getY() + gate.getHeight());
        }
        
        // Find bounds from LEDs
        for (LEDComponent led : leds) {
            minX = Math.min(minX, led.getX());
            minY = Math.min(minY, led.getY());
            maxX = Math.max(maxX, led.getX() + led.getWidth());
            maxY = Math.max(maxY, led.getY() + led.getHeight());
        }
        
        // If no components, use default size
        if (gates.isEmpty() && leds.isEmpty()) {
            minX = 0;
            minY = 0;
            maxX = 800;
            maxY = 600;
        }
        
        // Add padding
        int padding = 50;
        minX = Math.max(0, minX - padding);
        minY = Math.max(0, minY - padding);
        maxX += padding;
        maxY += padding;
        
        int width = maxX - minX;
        int height = maxY - minY;
        
        // Create buffered image with the calculated dimensions
        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(
            width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        
        Graphics2D g2d = image.createGraphics();
        
        // Set rendering hints for better quality
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Fill background
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);
        
        // Translate to account for offset
        g2d.translate(-minX, -minY);
        
        // Draw wires first (so they appear behind components)
        for (WireConnection wire : wires) {
            wire.updatePoints();
            wire.draw(g2d);
        }
        
        // Draw wire crossings
        java.util.Map<Point, Integer> crossingCounts = new java.util.HashMap<>();
        for (int i = 0; i < wires.size(); i++) {
            for (int j = i + 1; j < wires.size(); j++) {
                Point intersection = wires.get(i).findIntersection(wires.get(j));
                if (intersection != null) {
                    Point key = new Point(intersection.x, intersection.y);
                    int count = crossingCounts.getOrDefault(key, 0);
                    crossingCounts.put(key, count + 1);
                    boolean useVerticalBridge = (count % 2 == 1);
                    
                    g2d.setColor(Color.WHITE);
                    g2d.setStroke(new BasicStroke(5));
                    if (useVerticalBridge) {
                        g2d.drawLine(intersection.x, intersection.y - 6, intersection.x, intersection.y + 6);
                    } else {
                        g2d.drawLine(intersection.x - 6, intersection.y, intersection.x + 6, intersection.y);
                    }
                    
                    g2d.setColor(wires.get(j).getWireColor());
                    g2d.setStroke(new BasicStroke(2));
                    int bridgeWidth = 12;
                    int bridgeHeight = 6;
                    if (useVerticalBridge) {
                        g2d.drawArc(intersection.x - bridgeHeight, intersection.y - bridgeWidth/2, 
                            bridgeHeight * 2, bridgeWidth, 90, 180);
                    } else {
                        g2d.drawArc(intersection.x - bridgeWidth/2, intersection.y - bridgeHeight, 
                            bridgeWidth, bridgeHeight * 2, 0, 180);
                    }
                }
            }
        }
        
        // Draw gate components
        for (GateComponent gate : gates) {
            g2d.translate(gate.getX(), gate.getY());
            gate.paint(g2d);
            g2d.translate(-gate.getX(), -gate.getY());
        }
        
        // Draw LED components
        for (LEDComponent led : leds) {
            g2d.translate(led.getX(), led.getY());
            led.paint(g2d);
            g2d.translate(-led.getX(), -led.getY());
        }
        
        g2d.dispose();
        
        // Write to file
        javax.imageio.ImageIO.write(image, "PNG", file);
    }
}
