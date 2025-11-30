package org.scd.ui;

import org.scd.business.service.CircuitService;

import javax.swing.*;
import java.awt.*;

public class mainPanel extends JPanel {
    private CircuitService service; 
    private CircuitCanvas circuitCanvas;
    private JLabel circuitCount;
    
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
       JLabel projectName = new JLabel("");
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

        // Adding circuit panel and side buttons to center panel
        centerPanel.add(circuitScrollPane, BorderLayout.CENTER);
        centerPanel.add(sideBtnsPanel, BorderLayout.EAST);

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
        analyzeBtn.addActionListener(e -> showTruthTable());
        
        // Add all panels to mainPanel
        add(upperPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
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

    public CircuitCanvas getCircuitCanvas() {
        return circuitCanvas;
    }
}
