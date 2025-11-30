package org.scd.ui;

import org.scd.business.service.CircuitService;

import javax.swing.*;
import java.awt.*;

public class mainPanel extends JPanel {
    private CircuitService service; 
    private CircuitCanvas circuitCanvas;
    private JLabel circuitCount;
    private JLabel projectName;
    
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
        JButton connectorBtn = new JButton("Connector");
        JButton ledBtn = new JButton("LED");
        upperLeftPanel.add(andBtn);
        upperLeftPanel.add(orBtn);
        upperLeftPanel.add(notBtn);
        upperLeftPanel.add(connectorBtn);
        upperLeftPanel.add(ledBtn);

       JPanel upperRightPanel = new JPanel();
       upperRightPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
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
        JLabel circuitCountTitle = new JLabel("Number of Circuits: ");
        circuitCount = new JLabel("0");
        bottomPanel.add(circuitCountTitle);
        bottomPanel.add(circuitCount);
        
        // Add button listeners
        andBtn.addActionListener(e -> addGate("AND"));
        orBtn.addActionListener(e -> addGate("OR"));
        notBtn.addActionListener(e -> addGate("NOT"));
        ledBtn.addActionListener(e -> addLED());
        connectorBtn.addActionListener(e -> activateConnectorMode());
        simulateBtn.addActionListener(e -> showSimulationDialog());
        analyzeBtn.addActionListener(e -> showTruthTable());
        
        //circuits list panel - full height
        JPanel sideBtnsPanel  = new JPanel(new BorderLayout());
        
        // Add scroll pane for circuits list
        JPanel circuitsListPanel = new JPanel();
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
    
  
    private void addGate(String gateType) {
        circuitCanvas.addGate(gateType);
        updateCircuitCount();
    }
    
   
    private void addLED() {
        circuitCanvas.addLED();
        updateCircuitCount();
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
    
    public void setProjectName(String name) {
        projectName.setText(name);
    }
}
