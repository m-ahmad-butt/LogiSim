package org.scd.ui;

import org.scd.business.service.CircuitService;

import javax.swing.*;
import java.awt.*;

public class mainPanel extends JPanel {
    private CircuitService service; 
    private CircuitCanvas circuitCanvas;
    private JLabel circuitCount;
    private JLabel projectName;
    private JPanel circuitsListPanel; // Panel to display circuit list
    
    // Drag-and-drop fields
    private String draggedComponentType = null;
    private Point dragStartPoint = null;
    private Point currentDragPoint = null;
    private boolean isDragging = false;
    private boolean isOverlapping = false; // Track if drag position overlaps with existing component
    
    // Circuit drag-and-drop fields
    private org.scd.business.model.Circuit draggedCircuit = null;
    private Rectangle draggedCircuitBounds = null;
    
    public mainPanel() {
        this.service = CircuitService.getInstance();
        setLayout(new BorderLayout());

        //North part upper row panel
        JPanel upperPanel = new JPanel();
        upperPanel.setLayout(new BorderLayout());

       JPanel upperLeftPanel = new JPanel();
       upperLeftPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JButton andBtn = new JButton("AND");
        JButton orBtn = new JButton("OR");
        JButton notBtn = new JButton("NOT");
        JButton ledBtn = new JButton("LED");
        JButton switchBtn = new JButton("Switch");
        upperLeftPanel.add(andBtn);
        upperLeftPanel.add(orBtn);
        upperLeftPanel.add(notBtn);
        upperLeftPanel.add(ledBtn);
        upperLeftPanel.add(switchBtn);

       JPanel upperRightPanel = new JPanel();
       upperRightPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
       
       JToggleButton connectorBtn = new JToggleButton();
       JToggleButton deleteBtn = new JToggleButton();
       
       try {
           java.net.URL offUrl = getClass().getClassLoader().getResource(ResourcePath.getInstance().getConnectorOff());
           java.net.URL onUrl = getClass().getClassLoader().getResource(ResourcePath.getInstance().getConnectorOn());
           
           if (offUrl != null && onUrl != null) {
               // Increased width for better visibility
               Image offImg = new ImageIcon(offUrl).getImage().getScaledInstance(50, 30, Image.SCALE_SMOOTH);
               Image onImg = new ImageIcon(onUrl).getImage().getScaledInstance(50, 30, Image.SCALE_SMOOTH);
               
               // Setup Connector Button
               connectorBtn.setIcon(new ImageIcon(offImg));
               connectorBtn.setSelectedIcon(new ImageIcon(onImg));
               connectorBtn.setPreferredSize(new Dimension(60, 40));
               connectorBtn.setContentAreaFilled(false);
               connectorBtn.setBorderPainted(false);
               connectorBtn.setFocusPainted(false);
               connectorBtn.setOpaque(false);
               
               // Setup Delete Button (same images)
               deleteBtn.setIcon(new ImageIcon(offImg));
               deleteBtn.setSelectedIcon(new ImageIcon(onImg));
               deleteBtn.setPreferredSize(new Dimension(60, 40));
               deleteBtn.setContentAreaFilled(false);
               deleteBtn.setBorderPainted(false);
               deleteBtn.setFocusPainted(false);
               deleteBtn.setOpaque(false);
           } else {
               connectorBtn.setText("Conn");
               deleteBtn.setText("Del");
           }
       } catch (Exception e) {
           connectorBtn.setText("Conn");
           deleteBtn.setText("Del");
       }
       connectorBtn.setToolTipText("Toggle Connector Mode");
       deleteBtn.setToolTipText("Toggle Delete Mode");
       
       // Add label and button
       upperRightPanel.add(new JLabel("Delete Mode: "));
       upperRightPanel.add(deleteBtn);
       upperRightPanel.add(new JLabel("Connector Mode: "));
       upperRightPanel.add(connectorBtn);
       
       JButton simulateBtn = new JButton("Simulate");
       JButton analyzeBtn = new JButton("Analyze");
       JLabel projectTitle = new JLabel("Project Name: ");
       projectName = new JLabel("");
       upperRightPanel.add(simulateBtn);
       upperRightPanel.add(analyzeBtn);
       upperRightPanel.add(projectTitle);
       upperRightPanel.add(projectName);
       
       upperPanel.add(upperLeftPanel, BorderLayout.WEST);
       upperPanel.add(upperRightPanel, BorderLayout.EAST);


       //center panel
        JPanel centerPanel  = new JPanel(new BorderLayout());
        
        // Create circuit canvas with scroll pane
        circuitCanvas = new CircuitCanvas();
        
        // Set callback for component count updates (e.g. after deletion)
        circuitCanvas.setOnComponentCountChange(this::updateCircuitCount);
        
        JScrollPane circuitScrollPane = new JScrollPane(circuitCanvas);
        circuitScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        circuitScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);


        // Adding circuit panel to center panel (removed sidebar)
        centerPanel.add(circuitScrollPane, BorderLayout.CENTER);

        //South panel
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel circuitCountTitle = new JLabel("Number of Components: ");
        circuitCount = new JLabel("0");
        bottomPanel.add(circuitCountTitle);
        bottomPanel.add(circuitCount);
        
        // Add drag-and-drop listeners for component buttons
        setupDragAndDrop(andBtn, "AND");
        setupDragAndDrop(orBtn, "OR");
        setupDragAndDrop(notBtn, "NOT");
        setupDragAndDrop(ledBtn, "LED");
        setupDragAndDrop(switchBtn, "Switch");
        
        // Keep connector as click button
        // Connector toggle listener
        connectorBtn.addActionListener(e -> {
            boolean isSelected = connectorBtn.isSelected();
            circuitCanvas.setConnectorMode(isSelected);
            if (isSelected) {
                deleteBtn.setSelected(false); // Mutually exclusive
            }
        });
        
        // Delete toggle listener
        deleteBtn.addActionListener(e -> {
            boolean isSelected = deleteBtn.isSelected();
            circuitCanvas.setDeleteMode(isSelected);
            if (isSelected) {
                connectorBtn.setSelected(false); // Mutually exclusive
            }
        });
        simulateBtn.addActionListener(e -> showSimulationDialog());
        analyzeBtn.addActionListener(e -> showTruthTable());
        
        //circuits list panel - full height
        JPanel sideBtnsPanel  = new JPanel(new BorderLayout());
        
        // Add scroll pane for circuits list
        circuitsListPanel = new JPanel();
        circuitsListPanel.setLayout(new BoxLayout(circuitsListPanel, BoxLayout.Y_AXIS));
        circuitsListPanel.setBackground(Color.WHITE);
        
        JScrollPane circuitsScrollPane = new JScrollPane(circuitsListPanel);
        circuitsScrollPane.setPreferredSize(new Dimension(200, 0));
        circuitsScrollPane.setMinimumSize(new Dimension(200, 0));
        circuitsScrollPane.setBorder(BorderFactory.createTitledBorder("Circuits"));
        
        sideBtnsPanel.add(circuitsScrollPane, BorderLayout.CENTER);

        // Add all panels to mainPanel
        add(upperPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        add(sideBtnsPanel, BorderLayout.EAST);
    }
    
    /**
     * Setup drag-and-drop functionality for a component button
     */
    private void setupDragAndDrop(JButton button, String componentType) {
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                // Start drag operation
                draggedComponentType = componentType;
                dragStartPoint = e.getPoint();
                isDragging = false; // Not dragging until mouse moves
            }
            
            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                if (isDragging && draggedComponentType != null) {
                    // Convert button coordinates to canvas coordinates
                    Point canvasPoint = SwingUtilities.convertPoint(button, e.getPoint(), circuitCanvas);
                    
                    // Check if released over canvas
                    if (canvasPoint.x >= 0 && canvasPoint.y >= 0 && 
                        canvasPoint.x < circuitCanvas.getWidth() && 
                        canvasPoint.y < circuitCanvas.getHeight()) {
                        
                        // Check for overlap before placing
                        Rectangle newBounds = new Rectangle(canvasPoint.x - 75, canvasPoint.y - 40, 150, 80);
                        if (!circuitCanvas.checkOverlap(newBounds, null)) {
                            // Place component at drop location only if no overlap
                            if (draggedComponentType.equals("LED")) {
                                circuitCanvas.addLED(canvasPoint.x - 75, canvasPoint.y - 40);
                            } else if (draggedComponentType.equals("Switch")) {
                                circuitCanvas.addSwitch(canvasPoint.x - 75, canvasPoint.y - 40);
                            } else {
                                circuitCanvas.addGate(draggedComponentType, canvasPoint.x - 75, canvasPoint.y - 40);
                            }
                            updateCircuitCount();
                        } else {
                            // Show message that placement was blocked
                            JOptionPane.showMessageDialog(mainPanel.this, 
                                "Cannot place component here - overlaps with existing component!",
                                "Invalid Placement", 
                                JOptionPane.WARNING_MESSAGE);
                        }
                    }
                }
                
                // Reset drag state
                draggedComponentType = null;
                dragStartPoint = null;
                currentDragPoint = null;
                isDragging = false;
                isOverlapping = false;
                repaint();
            }
        });
        
        button.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseDragged(java.awt.event.MouseEvent e) {
                if (draggedComponentType != null) {
                    isDragging = true;
                    // Update drag position for visual feedback
                    currentDragPoint = SwingUtilities.convertPoint(button, e.getPoint(), mainPanel.this);
                    
                    // Check for overlap with existing components
                    Point canvasPoint = SwingUtilities.convertPoint(button, e.getPoint(), circuitCanvas);
                    Rectangle dragBounds = new Rectangle(canvasPoint.x - 75, canvasPoint.y - 40, 150, 80);
                    isOverlapping = circuitCanvas.checkOverlap(dragBounds, null);
                    
                    repaint();
                }
            }
        });
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Draw ghost component during component drag
        if (isDragging && currentDragPoint != null && draggedComponentType != null) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            
            // Draw a simple representation of the component being dragged
            // Red if overlapping, blue if valid placement
            if (isOverlapping) {
                g2d.setColor(new Color(255, 100, 100)); // Red for invalid
            } else {
                g2d.setColor(new Color(100, 150, 255)); // Blue for valid
            }
            g2d.fillRect(currentDragPoint.x - 75, currentDragPoint.y - 40, 150, 80);
            
            if (isOverlapping) {
                g2d.setColor(new Color(200, 0, 0)); // Dark red border
            } else {
                g2d.setColor(Color.BLUE);
            }
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRect(currentDragPoint.x - 75, currentDragPoint.y - 40, 150, 80);
            
            // Draw component type label
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(draggedComponentType);
            g2d.drawString(draggedComponentType, 
                currentDragPoint.x - textWidth/2, 
                currentDragPoint.y + 5);
        }
        
        // Draw ghost circuit during circuit drag
        if (isDragging && currentDragPoint != null && draggedCircuit != null && draggedCircuitBounds != null) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
            
            // Draw bounding box for the circuit
            // Red if overlapping, green if valid placement
            if (isOverlapping) {
                g2d.setColor(new Color(255, 100, 100)); // Red for invalid
            } else {
                g2d.setColor(new Color(100, 255, 150)); // Green for valid
            }
            g2d.fillRect(currentDragPoint.x, currentDragPoint.y, 
                draggedCircuitBounds.width, draggedCircuitBounds.height);
            
            // Draw border
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
            if (isOverlapping) {
                g2d.setColor(new Color(200, 0, 0)); // Dark red border
            } else {
                g2d.setColor(new Color(0, 150, 50)); // Dark green border
            }
            g2d.setStroke(new BasicStroke(3)); // Solid border
            g2d.drawRect(currentDragPoint.x, currentDragPoint.y, 
                draggedCircuitBounds.width, draggedCircuitBounds.height);
            
            // Draw circuit name label
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            String circuitName = draggedCircuit.getCircuitName();
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(circuitName);
            
            // Draw text background
            int textX = currentDragPoint.x + draggedCircuitBounds.width / 2 - textWidth / 2;
            int textY = currentDragPoint.y + draggedCircuitBounds.height / 2;
            g2d.setColor(new Color(0, 0, 0, 180));
            g2d.fillRect(textX - 5, textY - fm.getAscent() - 2, textWidth + 10, fm.getHeight() + 4);
            
            // Draw text
            g2d.setColor(Color.WHITE);
            g2d.drawString(circuitName, textX, textY);
        }
    }
    
  
    private void activateConnectorMode() {
        circuitCanvas.toggleConnectorMode();
    }
    
  
    private void updateCircuitCount() {
        circuitCount.setText(String.valueOf(circuitCanvas.getComponentCount()));
    }
    
  
    private void showTruthTable() {
        JPanel truthTablePanel = new JPanel(new BorderLayout());
        truthTablePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        java.util.List<String[]> tableData = service.generateTruthTable();
        
        StringBuilder displayText = new StringBuilder();
        displayText.append("Truth Table for Circuit:\n\n");
        
        if (tableData.isEmpty()) {
            displayText.append("No components in the circuit.");
        } else {
            // Calculate column widths based on header
            String[] header = tableData.get(0);
            int numCols = header.length;
            int colWidth = 15; // Default width
            
            // Build format string dynamically
            StringBuilder formatBuilder = new StringBuilder();
            for (int i = 0; i < numCols; i++) {
                formatBuilder.append("%-").append(colWidth).append("s ");
            }
            formatBuilder.append("\n");
            String formatString = formatBuilder.toString();
            
            // Print Header
            displayText.append(String.format(formatString, (Object[])header));
            
            // Print Separator
            displayText.append("-".repeat(numCols * (colWidth + 1))).append("\n");
            
            // Print Rows (skip header)
            for (int i = 1; i < tableData.size(); i++) {
                displayText.append(String.format(formatString, (Object[])tableData.get(i)));
            }
        }
        
        JTextArea textArea = new JTextArea(displayText.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(800, 500)); // Increased size for larger tables
        
        truthTablePanel.add(scrollPane, BorderLayout.CENTER);
        
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Truth Table", true);
        dialog.setContentPane(truthTablePanel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    private void showSimulationDialog() {
        // Get all gates and LEDs
        java.util.List<org.scd.business.model.Gate> gates = service.getAllGates();
        java.util.List<org.scd.business.model.LED> leds = service.getAllLEDs();
        
        if (gates.isEmpty() && leds.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No components in the circuit to simulate!", 
                "Simulation", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Create simulation dialog
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Circuit Simulation", true);
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Input panel
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Set Input Values"));
        
        java.util.Map<Integer, JToggleButton[]> inputButtons = new java.util.HashMap<>();
        
        for (org.scd.business.model.Gate gate : gates) {
            // Check if gate has manual inputs (not connected)
            boolean hasManualInput1 = gate.getInput1() != null && !service.isInputConnected(gate.getComponentId(), 0);
            boolean hasManualInput2 = gate.getInput2() != null && !service.isInputConnected(gate.getComponentId(), 1);
            
            if (hasManualInput1 || hasManualInput2) {
                JPanel gatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                gatePanel.add(new JLabel(gate.getGateType() + " Gate " + gate.getComponentId() + ":"));
                
                JToggleButton[] buttons = new JToggleButton[2];
                
                if (hasManualInput1) {
                    gatePanel.add(new JLabel("  Input 1:"));
                    JToggleButton input1Button = createToggleButton(gate.getInput1().getValue());
                    gatePanel.add(input1Button);
                    buttons[0] = input1Button;
                }
                
                if (hasManualInput2) {
                    gatePanel.add(new JLabel("  Input 2:"));
                    JToggleButton input2Button = createToggleButton(gate.getInput2().getValue());
                    gatePanel.add(input2Button);
                    buttons[1] = input2Button;
                }
                
                inputButtons.put(gate.getComponentId(), buttons);
                inputPanel.add(gatePanel);
            }
        }
        
        if (inputButtons.isEmpty()) {
            JLabel noInputsLabel = new JLabel("All inputs are connected via wires. No manual inputs needed.");
            noInputsLabel.setForeground(Color.BLUE);
            inputPanel.add(noInputsLabel);
        }
        
        JScrollPane inputScrollPane = new JScrollPane(inputPanel);
        inputScrollPane.setPreferredSize(new Dimension(500, 200));
        
        // Output panel
        JPanel outputPanel = new JPanel();
        outputPanel.setLayout(new BoxLayout(outputPanel, BoxLayout.Y_AXIS));
        outputPanel.setBorder(BorderFactory.createTitledBorder("Circuit Outputs"));
        
        JTextArea outputArea = new JTextArea(10, 40);
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane outputScrollPane = new JScrollPane(outputArea);
        outputPanel.add(outputScrollPane);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton runButton = new JButton("Run Simulation");
        JButton closeButton = new JButton("Close");
        
        runButton.addActionListener(e -> {
            // Set input values from toggle buttons
            for (java.util.Map.Entry<Integer, JToggleButton[]> entry : inputButtons.entrySet()) {
                int gateId = entry.getKey();
                JToggleButton[] buttons = entry.getValue();
                
                if (buttons[0] != null) {
                    int value = buttons[0].isSelected() ? 1 : 0;
                    service.setGateInput(gateId, 0, value, null);
                }
                
                if (buttons[1] != null) {
                    int value = buttons[1].isSelected() ? 1 : 0;
                    service.setGateInput(gateId, 1, value, null);
                }
            }
            
            // Run simulation
            service.calculateCircuit();
            circuitCanvas.refreshCircuit();
            
            // Display results
            StringBuilder results = new StringBuilder();
            results.append("SIMULATION RESULTS\n");
            results.append("=".repeat(50)).append("\n\n");
            
            results.append("GATES:\n");
            results.append("-".repeat(50)).append("\n");
            results.append(String.format("%-15s %-10s %-15s %-15s %-10s\n", 
                "Component", "Type", "Input 1", "Input 2", "Output"));
            results.append("-".repeat(50)).append("\n");
            
            for (org.scd.business.model.Gate gate : gates) {
                String input1 = gate.getInput1() != null && gate.getInput1().getValue() != null ? 
                    gate.getInput1().getValue().toString() : "-";
                String input2 = gate.getInput2() != null && gate.getInput2().getValue() != null ? 
                    gate.getInput2().getValue().toString() : "-";
                String output = gate.getOutput() != null ? gate.getOutput().toString() : "-";
                
                results.append(String.format("%-15s %-10s %-15s %-15s %-10s\n",
                    gate.getGateType() + " " + gate.getComponentId(),
                    gate.getGateType(),
                    input1,
                    input2,
                    output));
            }
            
            if (!leds.isEmpty()) {
                results.append("\nLEDs:\n");
                results.append("-".repeat(50)).append("\n");
                results.append(String.format("%-15s %-15s %-10s\n", "Component", "Input", "State"));
                results.append("-".repeat(50)).append("\n");
                
                for (org.scd.business.model.LED led : leds) {
                    String input = led.getInput() != null && led.getInput().getValue() != null ? 
                        led.getInput().getValue().toString() : "-";
                    String state = led.isOn() ? "ON" : "OFF";
                    
                    results.append(String.format("%-15s %-15s %-10s\n",
                        "LED " + led.getComponentId(),
                        input,
                        state));
                }
            }
            
            outputArea.setText(results.toString());
        });
        
        closeButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(runButton);
        buttonPanel.add(closeButton);
        
        mainPanel.add(inputScrollPane, BorderLayout.NORTH);
        mainPanel.add(outputPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setContentPane(mainPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    private JToggleButton createToggleButton(Integer currentValue) {
        JToggleButton button = new JToggleButton();
        button.setPreferredSize(new Dimension(60, 30));
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        
        // Set initial state
        if (currentValue != null && currentValue == 1) {
            button.setSelected(true);
            button.setText("1");
            button.setBackground(new Color(76, 175, 80)); // Green
            button.setForeground(Color.WHITE);
        } else {
            button.setSelected(false);
            button.setText("0");
            button.setBackground(new Color(244, 67, 54)); // Red
            button.setForeground(Color.WHITE);
        }
        
        // Add toggle listener
        button.addActionListener(e -> {
            if (button.isSelected()) {
                button.setText("1");
                button.setBackground(new Color(76, 175, 80)); // Green
            } else {
                button.setText("0");
                button.setBackground(new Color(244, 67, 54)); // Red
            }
        });
        
        return button;
    }

    public CircuitCanvas getCircuitCanvas() {
        return circuitCanvas;
    }
    
    /**
     * Update the circuit list panel with all circuits from the service
     */
    public void updateCircuitList() {
        circuitsListPanel.removeAll();
        
        java.util.List<org.scd.business.model.Circuit> circuits = service.getAllCircuits();
        
        for (org.scd.business.model.Circuit circuit : circuits) {
            JButton circuitButton = new JButton(circuit.getCircuitName());
            circuitButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            circuitButton.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            // Highlight current circuit
            if (circuit == service.getCurrentCircuit()) {
                circuitButton.setBackground(new Color(200, 230, 255));
                circuitButton.setOpaque(true);
            }
            
            // Add click handler for circuit switching
            circuitButton.addActionListener(e -> {
                if (circuit != service.getCurrentCircuit()) {
                    handleCircuitSwitch(circuit);
                }
            });
            
            // Add drag-and-drop for circuits (except current circuit)
            if (circuit != service.getCurrentCircuit()) {
                setupCircuitDragAndDrop(circuitButton, circuit);
            }
            
            circuitsListPanel.add(circuitButton);
            circuitsListPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        }
        
        circuitsListPanel.revalidate();
        circuitsListPanel.repaint();
    }
    
    /**
     * Handle switching to a different circuit
     */
    private void handleCircuitSwitch(org.scd.business.model.Circuit circuit) {
        // Switch to the selected circuit
        service.switchToCircuit(circuit);
        
        // Clear and reload canvas
        circuitCanvas.clearCanvas();
        circuitCanvas.loadCircuit(circuit);
        
        // Update UI
        updateCircuitList(); // Refresh to update highlighting
        updateCircuitCount();
    }
    
    /**
     * Setup drag-and-drop for a circuit button
     */
    private void setupCircuitDragAndDrop(JButton button, org.scd.business.model.Circuit circuit) {
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                // Start drag operation
                draggedCircuit = circuit;
                dragStartPoint = e.getPoint();
                isDragging = false; // Not dragging until mouse moves
                
                // Calculate bounds of circuit (for visualization)
                draggedCircuitBounds = calculateCircuitBounds(circuit);
            }
            
            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                if (isDragging && draggedCircuit != null) {
                    // Convert button coordinates to canvas coordinates
                    Point canvasPoint = SwingUtilities.convertPoint(button, e.getPoint(), circuitCanvas);
                    
                    // Check if released over canvas
                    if (canvasPoint.x >= 0 && canvasPoint.y >= 0 && 
                        canvasPoint.x < circuitCanvas.getWidth() && 
                        canvasPoint.y < circuitCanvas.getHeight()) {
                        
                        handleCircuitDrop(draggedCircuit, canvasPoint);
                    }
                }
                
                // Reset drag state
                draggedCircuit = null;
                draggedCircuitBounds = null;
                dragStartPoint = null;
                currentDragPoint = null;
                isDragging = false;
                isOverlapping = false;
                repaint();
            }
        });
        
        button.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseDragged(java.awt.event.MouseEvent e) {
                if (draggedCircuit != null) {
                    isDragging = true;
                    // Update drag position for visual feedback
                    currentDragPoint = SwingUtilities.convertPoint(button, e.getPoint(), mainPanel.this);
                    
                    // Check for overlap with existing components
                    Point canvasPoint = SwingUtilities.convertPoint(button, e.getPoint(), circuitCanvas);
                    
                    if (draggedCircuitBounds != null) {
                        // Create bounds at the drop location
                        // Note: draggedCircuitBounds already contains width/height of the circuit
                        // canvasPoint is where we want to place the top-left corner
                        Rectangle testBounds = new Rectangle(
                            canvasPoint.x,
                            canvasPoint.y,
                            draggedCircuitBounds.width,
                            draggedCircuitBounds.height
                        );
                        isOverlapping = circuitCanvas.checkOverlap(testBounds, null);
                    }
                    
                    repaint();
                }
            }
        });
    }
    
    /**
     * Calculate the bounding rectangle for a circuit
     */
    private Rectangle calculateCircuitBounds(org.scd.business.model.Circuit circuit) {
        if (circuit.getGates().isEmpty() && circuit.getLeds().isEmpty()) {
            return new Rectangle(0, 0, 200, 100); // Default size for empty circuits
        }
        
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        
        // Find bounds from gates
        for (org.scd.business.model.Gate gate : circuit.getGates()) {
            minX = Math.min(minX, gate.getPositionX());
            minY = Math.min(minY, gate.getPositionY());
            maxX = Math.max(maxX, gate.getPositionX() + 150); // Gate width
            maxY = Math.max(maxY, gate.getPositionY() + 80);  // Gate height
        }
        
        // Find bounds from LEDs
        for (org.scd.business.model.LED led : circuit.getLeds()) {
            minX = Math.min(minX, led.getPositionX());
            minY = Math.min(minY, led.getPositionY());
            maxX = Math.max(maxX, led.getPositionX() + 150); // LED width
            maxY = Math.max(maxY, led.getPositionY() + 80);  // LED height
        }
        
        return new Rectangle(minX, minY, maxX - minX, maxY - minY);
    }
    
    /**
     * Handle dropping a circuit onto the canvas
     */
    private void handleCircuitDrop(org.scd.business.model.Circuit circuit, Point dropPoint) {
        // Get the circuit's bounding box to know where its components start
        Rectangle bounds = calculateCircuitBounds(circuit);
        
        // The offset is how much we need to move the circuit so that its
        // top-left corner (bounds.x, bounds.y) ends up at the dropPoint
        int offsetX = dropPoint.x - bounds.x;
        int offsetY = dropPoint.y - bounds.y;
        
        System.out.println("=== Circuit Drop Debug ===");
        System.out.println("Circuit: " + circuit.getCircuitName());
        System.out.println("Drop point (where mouse released): (" + dropPoint.x + ", " + dropPoint.y + ")");
        System.out.println("Circuit original bounds (minX, minY, width, height): " + bounds);
        System.out.println("Calculated offset: (" + offsetX + ", " + offsetY + ")");
        System.out.println("Components will be moved from original position + offset");
        
        // Clone components from the circuit
        org.scd.business.service.CircuitService.ClonedComponents cloned = 
            service.cloneCircuitComponents(circuit, offsetX, offsetY);
        
        // IMPORTANT: Add cloned components to service FIRST, before creating UI components
        // This ensures that when GateComponent constructor queries service.getComponentPositionX/Y,
        // the components are already in the service with correct positions
        service.mergeComponentsIntoCurrentCircuit(cloned.gates, cloned.leds, cloned.switches, cloned.connectors);
        
        // Now create UI components - they will query service for positions
        java.util.List<GateComponent> tempGates = new java.util.ArrayList<>();
        java.util.List<LEDComponent> tempLEDs = new java.util.ArrayList<>();
        java.util.List<SwitchComponent> tempSwitches = new java.util.ArrayList<>();

        // Create UI components for cloned model components
        for (org.scd.business.model.Gate gate : cloned.gates) {
            GateComponent gc = new GateComponent(gate);
            tempGates.add(gc);
        }
        for (org.scd.business.model.LED led : cloned.leds) {
            LEDComponent lc = new LEDComponent(led);
            tempLEDs.add(lc);
        }
        for (org.scd.business.model.Switch sw : cloned.switches) {
            SwitchComponent sc = new SwitchComponent(sw);
            tempSwitches.add(sc);
        }

        // Check for overlap before committing UI components
        boolean hasOverlap = false;
        for (GateComponent gc : tempGates) {
            if (circuitCanvas.checkOverlap(gc.getBounds(), null)) {
                hasOverlap = true;
                break;
            }
        }

        if (!hasOverlap) {
            for (LEDComponent lc : tempLEDs) {
                if (circuitCanvas.checkOverlap(lc.getBounds(), null)) {
                    hasOverlap = true;
                    break;
                }
            }
        }

        if (!hasOverlap) {
            for (SwitchComponent sc : tempSwitches) {
                if (circuitCanvas.checkOverlap(sc.getBounds(), null)) {
                    hasOverlap = true;
                    break;
                }
            }
        }
        
        if (hasOverlap) {
            // Overlap detected - need to remove the components we just added to service
            for (org.scd.business.model.Gate gate : cloned.gates) {
                service.removeGate(gate.getComponentId());
            }
            for (org.scd.business.model.LED led : cloned.leds) {
                service.removeLED(led.getComponentId());
            }
            for (org.scd.business.model.Switch switchComp : cloned.switches) {
                service.removeSwitch(switchComp.getComponentId());
            }
            for (org.scd.business.model.Connector connector : cloned.connectors) {
                service.removeConnector(connector.getConnectorId());
            }
            
            JOptionPane.showMessageDialog(this,
                "Cannot place circuit here - components would overlap with existing components!",
                "Invalid Placement",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // No overlap, add UI components to canvas
        // (components are already in service from earlier merge)
        
        // Track max bounds for canvas expansion
        int maxRight = 0;
        int maxBottom = 0;
        
        // Add UI components to canvas
        for (GateComponent gc : tempGates) {
            circuitCanvas.getGates().add(gc);
            circuitCanvas.add(gc);
            maxRight = Math.max(maxRight, gc.getX() + gc.getWidth());
            maxBottom = Math.max(maxBottom, gc.getY() + gc.getHeight());
        }
        
        for (LEDComponent lc : tempLEDs) {
            circuitCanvas.getLEDs().add(lc);
            circuitCanvas.add(lc);
            maxRight = Math.max(maxRight, lc.getX() + lc.getWidth());
            maxBottom = Math.max(maxBottom, lc.getY() + lc.getHeight());
        }
        
        for (SwitchComponent sc : tempSwitches) {
            circuitCanvas.addSwitchComponent(sc);
            // circuitCanvas.add(sc) is called inside addSwitchComponent
            maxRight = Math.max(maxRight, sc.getX() + sc.getWidth());
            maxBottom = Math.max(maxBottom, sc.getY() + sc.getHeight());
        }
        
        // Expand canvas to fit new components
        Dimension currentSize = circuitCanvas.getPreferredSize();
        int padding = 100;
        int newWidth = Math.max(currentSize.width, maxRight + padding);
        int newHeight = Math.max(currentSize.height, maxBottom + padding);
        if (newWidth > currentSize.width || newHeight > currentSize.height) {
            circuitCanvas.setPreferredSize(new Dimension(newWidth, newHeight));
            circuitCanvas.revalidate();
        }
        
        // Create wire connections
        for (org.scd.business.model.Connector connector : cloned.connectors) {
            GateComponent sourceGate = findGateComponentById(connector.getSourceComponentId(), tempGates);
            // Also try to find source switch if gate not found
            Object source = sourceGate;
            if (source == null) {
                source = findComponentById(connector.getSourceComponentId(), tempGates, tempLEDs, tempSwitches);
            }
            
            Object target = findComponentById(connector.getTargetComponentId(), tempGates, tempLEDs, tempSwitches);
            
            if (source != null && target != null) {
                int wireIndex = 0;
                // Count existing wires from this source
                for (WireConnection existingWire : circuitCanvas.getWires()) {
                    if (existingWire.getSourceComponent() == source || existingWire.getSourceGate() == source) {
                        wireIndex++;
                    }
                }
                
                WireConnection wire = null;
                if (source instanceof GateComponent) {
                    wire = new WireConnection((GateComponent)source, target, 
                        connector.getTargetInputIndex(), circuitCanvas, wireIndex);
                } else if (source instanceof SwitchComponent) {
                    wire = new WireConnection((SwitchComponent)source, target, 
                        connector.getTargetInputIndex(), circuitCanvas, wireIndex);
                }
                
                if (wire != null) {
                    circuitCanvas.getWires().add(wire);
                }
                
                // Update UI component connections
                if (target instanceof GateComponent) {
                    GateComponent targetGate = (GateComponent) target;
                    if (connector.getTargetInputIndex() == 0) {
                        targetGate.getInput1().setSourceComponent(source);
                    } else {
                        targetGate.getInput2().setSourceComponent(source);
                    }
                } else if (target instanceof LEDComponent) {
                    LEDComponent targetLED = (LEDComponent) target;
                    if (source instanceof GateComponent) {
                        targetLED.setInputSource((GateComponent) source);
                    } else if (source instanceof SwitchComponent) {
                        // For a switch source, set the input directly and update state
                        targetLED.getInput().setSourceComponent(source);
                        targetLED.getInput().setValue(((SwitchComponent) source).getOutput());
                        targetLED.updateState();
                    }
                }
            }
        }
        
        // Recalculate circuit and update display
        service.calculateCircuit();
        circuitCanvas.refreshCircuit();
        circuitCanvas.repaint();
        updateCircuitCount();
    }
    
    /**
     * Find a GateComponent by its ID in a list
     */
    private GateComponent findGateComponentById(int id, java.util.List<GateComponent> gates) {
        for (GateComponent gc : gates) {
            if (gc.getComponentId() == id) {
                return gc;
            }
        }
        // Also check existing canvas gates
        for (GateComponent gc : circuitCanvas.getGates()) {
            if (gc.getComponentId() == id) {
                return gc;
            }
        }
        return null;
    }
    
    /**
     * Find a component (Gate, LED, or Switch) by ID
     */
    private Object findComponentById(int id, java.util.List<GateComponent> gates, java.util.List<LEDComponent> leds, java.util.List<SwitchComponent> switches) {
        // Check in provided lists first
        for (GateComponent gc : gates) {
            if (gc.getComponentId() == id) {
                return gc;
            }
        }
        for (LEDComponent lc : leds) {
            if (lc.getComponentId() == id) {
                return lc;
            }
        }
        for (SwitchComponent sc : switches) {
            if (sc.getComponentId() == id) {
                return sc;
            }
        }
        // Check existing canvas components
        for (GateComponent gc : circuitCanvas.getGates()) {
            if (gc.getComponentId() == id) {
                return gc;
            }
        }
        for (LEDComponent lc : circuitCanvas.getLEDs()) {
            if (lc.getComponentId() == id) {
                return lc;
            }
        }
        for (SwitchComponent sc : circuitCanvas.getSwitches()) {
            if (sc.getComponentId() == id) {
                return sc;
            }
        }
        return null;
    }
    
    public void setProjectName(String name) {
        projectName.setText(name);
    }
}
