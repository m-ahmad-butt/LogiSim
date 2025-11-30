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
        upperLeftPanel.add(andBtn);
        upperLeftPanel.add(orBtn);
        upperLeftPanel.add(notBtn);
        upperLeftPanel.add(ledBtn);

       JPanel upperRightPanel = new JPanel();
       upperRightPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
       
       JToggleButton connectorBtn = new JToggleButton();
       try {
           java.net.URL offUrl = getClass().getClassLoader().getResource(ResourcePath.getInstance().getConnectorOff());
           java.net.URL onUrl = getClass().getClassLoader().getResource(ResourcePath.getInstance().getConnectorOn());
           
           if (offUrl != null && onUrl != null) {
               // Increased width for better visibility
               Image offImg = new ImageIcon(offUrl).getImage().getScaledInstance(50, 30, Image.SCALE_SMOOTH);
               Image onImg = new ImageIcon(onUrl).getImage().getScaledInstance(50, 30, Image.SCALE_SMOOTH);
               
               connectorBtn.setIcon(new ImageIcon(offImg));
               connectorBtn.setSelectedIcon(new ImageIcon(onImg));
               connectorBtn.setPreferredSize(new Dimension(60, 40));
               
               // Make transparent
               connectorBtn.setContentAreaFilled(false);
               connectorBtn.setBorderPainted(false);
               connectorBtn.setFocusPainted(false);
               connectorBtn.setOpaque(false);
           } else {
               connectorBtn.setText("Conn");
           }
       } catch (Exception e) {
           connectorBtn.setText("Conn");
       }
       connectorBtn.setToolTipText("Toggle Connector Mode");
       
       // Add label and button
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
        
        // Keep connector as click button
        // Connector toggle listener
        connectorBtn.addActionListener(e -> circuitCanvas.setConnectorMode(connectorBtn.isSelected()));
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
        
        // Draw ghost component during drag
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
        
        if (tableData.size() <= 1) {
            displayText.append("No components in the circuit.");
        } else {
            for (String[] row : tableData) {
                displayText.append(String.format("%-15s %-10s %-15s %-15s %-10s\n",
                    row[0], row[1], row[2], row[3], row[4]));
                
                if (displayText.toString().split("\n").length == 3) {
                    displayText.append("-".repeat(70)).append("\n");
                }
            }
        }
        
        JTextArea textArea = new JTextArea(displayText.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(600, 400));
        
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
            
            // Note: Circuit switching functionality will be added later
            // For now, just display the circuit names
            
            circuitsListPanel.add(circuitButton);
            circuitsListPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        }
        
        circuitsListPanel.revalidate();
        circuitsListPanel.repaint();
    }
    
    public void setProjectName(String name) {
        projectName.setText(name);
    }
}
