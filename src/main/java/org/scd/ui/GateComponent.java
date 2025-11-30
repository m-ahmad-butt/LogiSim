package org.scd.ui;

import org.scd.business.service.CircuitService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;


public class GateComponent extends JPanel {
    private int componentId; // ID to communicate with service
    private CircuitService service; 
    
    private ComponentInput input1; 
    private ComponentInput input2; 
    
    // UI components
    private JPanel inputPanel;
    private JPanel gateImagePanel;
    private JPanel outputPanel;
    private JLabel input1Label;
    private JLabel input2Label;
    private JLabel outputLabel;
    
    public GateComponent(String gateType, int x, int y) {
        this.service = CircuitService.getInstance();
        
        // Create business model through service and store the ID
        this.componentId = service.addGate(gateType, x, y).getComponentId();
        service.registerUIComponent(componentId, this);
        
        initComponent();
    }

    public GateComponent(org.scd.business.model.Gate gate) {
        this.service = CircuitService.getInstance();
        this.componentId = gate.getComponentId();
        service.registerUIComponent(componentId, this);
        
        initComponent();
        
        // Restore input values from the model
        if (gate.getInput1() != null && gate.getInput1().getValue() != null) {
            this.input1.setValue(gate.getInput1().getValue());
        }
        
        if (gate.getInput2() != null && gate.getInput2().getValue() != null && this.input2 != null) {
            this.input2.setValue(gate.getInput2().getValue());
        }
        
        // Update UI to show restored values
        updateImage();
    }

    private void initComponent() {
        // Initialize UI helpers for inputs
        this.input1 = new ComponentInput(0);
        if (!service.getGateType(componentId).equals("NOT")) {
            this.input2 = new ComponentInput(1);
        }
        
        // Setup UI
        setupUI();
        
        // Set bounds from service
        setBounds(service.getComponentPositionX(componentId), 
                  service.getComponentPositionY(componentId), 150, 80);
        setOpaque(true);
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        
        // Add mouse listeners
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    handleDoubleClick();
                } else if (e.getClickCount() == 1) {
                    Container parent = getParent();
                    if (parent instanceof CircuitCanvas) {
                        ((CircuitCanvas) parent).handleComponentClick(GateComponent.this);
                    }
                }
            }
        });
    }
    
  
    private void setupUI() {
        setLayout(new BorderLayout(5, 0));
        
        // Input Panel (Left side)
        inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setPreferredSize(new Dimension(30, 80));
        
        input1Label = new JLabel("-");
        input1Label.setFont(new Font("Arial", Font.BOLD, 14));
        input1Label.setAlignmentX(Component.CENTER_ALIGNMENT);
        inputPanel.add(Box.createVerticalGlue());
        inputPanel.add(input1Label);
        
        if (!service.getGateType(componentId).equals("NOT")) {
            inputPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            input2Label = new JLabel("-");
            input2Label.setFont(new Font("Arial", Font.BOLD, 14));
            input2Label.setAlignmentX(Component.CENTER_ALIGNMENT);
            inputPanel.add(input2Label);
        }
        inputPanel.add(Box.createVerticalGlue());
        
        // Gate Image Panel (Center)
        gateImagePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawGateImage(g);
            }
        };
        gateImagePanel.setBackground(Color.WHITE);
        gateImagePanel.setPreferredSize(new Dimension(80, 80));
        
        // Output Panel (Right side)
        outputPanel = new JPanel();
        outputPanel.setLayout(new BoxLayout(outputPanel, BoxLayout.Y_AXIS));
        outputPanel.setBackground(Color.WHITE);
        outputPanel.setPreferredSize(new Dimension(30, 80));
        
        outputLabel = new JLabel("-");
        outputLabel.setFont(new Font("Arial", Font.BOLD, 14));
        outputLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        outputPanel.add(Box.createVerticalGlue());
        outputPanel.add(outputLabel);
        outputPanel.add(Box.createVerticalGlue());
        
        // Add panels to main component
        add(inputPanel, BorderLayout.WEST);
        add(gateImagePanel, BorderLayout.CENTER);
        add(outputPanel, BorderLayout.EAST);
    }
    
  
    private void drawGateImage(Graphics g) {
        String imagePath = null;
        String gateType = service.getGateType(componentId);
        
        // Get appropriate image path based on gate type
        if (gateType.equals("AND")) {
            imagePath = ResourcePath.getInstance().getAndWithoutInputs();
        } else if (gateType.equals("OR")) {
            imagePath = ResourcePath.getInstance().getOrWithoutInputs();
        } else if (gateType.equals("NOT")) {
            imagePath = ResourcePath.getInstance().getNotWithoutInput();
        }
        
        // Load and draw the image
        if (imagePath != null) {
            try {
                java.net.URL imgUrl = getClass().getClassLoader().getResource(imagePath);
                if (imgUrl != null) {
                    ImageIcon icon = new ImageIcon(imgUrl);
                    Image img = icon.getImage();
                    
                    int width = gateImagePanel.getWidth();
                    int height = gateImagePanel.getHeight();
                    
                    // Draw image centered
                    int imgWidth = img.getWidth(null);
                    int imgHeight = img.getHeight(null);
                    int x = (width - imgWidth) / 2;
                    int y = (height - imgHeight) / 2;
                    
                    g.drawImage(img, x, y, null);
                } else {
                    // Image not found
                    g.setColor(Color.RED);
                    g.drawString("Image not found", 10, 40);
                    g.setColor(Color.BLACK);
                    g.drawString(gateType, 10, 55);
                }
            } catch (Exception e) {
                // Error loading image
                g.setColor(Color.RED);
                g.drawString("Load error", 10, 40);
                g.setColor(Color.BLACK);
                g.drawString(gateType, 10, 55);
            }
        }
    }
    

    private void handleDoubleClick() {
        String gateType = service.getGateType(componentId);
        if (gateType.equals("NOT")) {
            // NOT gate: only ask for one input if not connected
            if (!input1.isConnected()) {
                String result = JOptionPane.showInputDialog(this, 
                    "Enter input value (0 or 1) for " + gateType + " Gate " + componentId + ":");
                if (result != null) {
                    try {
                        int val = Integer.parseInt(result.trim());
                        if (val == 0 || val == 1) {
                            input1.setValue(val);
                            calculateOutput();
                            updateImage();
                        } else {
                            JOptionPane.showMessageDialog(this, "Please enter 0 or 1");
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Invalid input!");
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Input is already connected!");
            }
        } else {
            // AND/OR gate: ask for inputs that are not connected
            List<String> inputsNeeded = new ArrayList<>();
            if (!input1.isConnected()) inputsNeeded.add("Input 1");
            if (!input2.isConnected()) inputsNeeded.add("Input 2");
            
            if (inputsNeeded.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Both inputs are already connected!");
                return;
            }
            
            if (!input1.isConnected()) {
                String result = JOptionPane.showInputDialog(this, 
                    "Enter Input 1 value (0 or 1) for " + gateType + " Gate " + getComponentId() + ":");
                if (result != null) {
                    try {
                        int val = Integer.parseInt(result.trim());
                        if (val == 0 || val == 1) {
                            input1.setValue(val);
                        } else {
                            JOptionPane.showMessageDialog(this, "Please enter 0 or 1");
                            return;
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Invalid input!");
                        return;
                    }
                }
            }
            
            if (!input2.isConnected()) {
                String result = JOptionPane.showInputDialog(this, 
                    "Enter Input 2 value (0 or 1) for " + gateType + " Gate " + getComponentId() + ":");
                if (result != null) {
                    try {
                        int val = Integer.parseInt(result.trim());
                        if (val == 0 || val == 1) {
                            input2.setValue(val);
                        } else {
                            JOptionPane.showMessageDialog(this, "Please enter 0 or 1");
                            return;
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Invalid input!");
                        return;
                    }
                }
            }
            
            calculateOutput();
            updateImage();
        }
    }
    
  
    public void updateImage() {
        // Update output label using service
        Integer output = service.getComponentOutput(componentId);
        outputLabel.setText(output != null ? output.toString() : "-");
        outputLabel.setForeground(output != null ? (output == 1 ? Color.GREEN : Color.RED) : Color.BLACK);
        
        // Update input labels
        Integer val1 = input1.getValue();
        input1Label.setText(val1 != null ? val1.toString() : "-");
        input1Label.setForeground(val1 != null ? (val1 == 1 ? Color.GREEN : Color.RED) : Color.BLACK);
        
        if (input2 != null) {
            Integer val2 = input2.getValue();
            input2Label.setText(val2 != null ? val2.toString() : "-");
            input2Label.setForeground(val2 != null ? (val2 == 1 ? Color.GREEN : Color.RED) : Color.BLACK);
        }
        
        repaint();
    }
    
  
    
    public void calculateOutput() {
        // Sync UI inputs to service/model
        if (input1 != null && input1.getValue() != null) {
            Integer sourceId = (input1.getSourceComponent() != null) ? 
                input1.getSourceComponent().getComponentId() : null;
            service.setGateInput(componentId, 0, input1.getValue(), sourceId);
        }
        
        if (input2 != null && input2.getValue() != null) {
            Integer sourceId = (input2.getSourceComponent() != null) ? 
                input2.getSourceComponent().getComponentId() : null;
            service.setGateInput(componentId, 1, input2.getValue(), sourceId);
        }
    }
    
    // Getters (all delegate to service)
    public int getComponentId() {
        return componentId;
    }
    
    public String getGateType() {
        return service.getGateType(componentId);
    }
    
    public ComponentInput getInput1() {
        return input1;
    }
    
    public ComponentInput getInput2() {
        return input2;
    }
    
    public Integer getOutput() {
        return service.getComponentOutput(componentId);
    }
    
    public void setRowColumn(int row, int column) {
        service.setComponentRowColumn(componentId, row, column);
    }
    
    public int getRow() {
        return service.getComponentRow(componentId);
    }
    
    public int getColumn() {
        return service.getComponentColumn(componentId);
    }
    
    public int getPositionX() {
        return service.getComponentPositionX(componentId);
    }
    
    public int getPositionY() {
        return service.getComponentPositionY(componentId);
    }
    
    public Point getOutputPoint() {
        return new Point(service.getComponentPositionX(componentId) + 150, 
                        service.getComponentPositionY(componentId) + 40);
    }
    
    public Point getInput1Point() {
        if (service.getGateType(componentId).equals("NOT")) {
            return new Point(service.getComponentPositionX(componentId), 
                           service.getComponentPositionY(componentId) + 40);
        } else {
            return new Point(service.getComponentPositionX(componentId), 
                           service.getComponentPositionY(componentId) + 25);
        }
    }
    
    public Point getInput2Point() {
        if (input2 != null) {
            return new Point(service.getComponentPositionX(componentId), 
                           service.getComponentPositionY(componentId) + 55);
        }
        return null;
    }
}
