package org.scd.ui;

import javax.swing.*;
import java.awt.*;

public class mainPanel extends JPanel {
    public mainPanel() {
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
        
        JPanel circuitPanel  = new JPanel(new CardLayout());
        circuitPanel.setBackground(Color.WHITE);


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
        centerPanel.add(circuitPanel, BorderLayout.CENTER);
        centerPanel.add(sideBtnsPanel, BorderLayout.EAST);

        //South panel
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel circuitCountTitle = new JLabel("Number of Circuits: ");
        JLabel circuitCount = new JLabel("0");
        bottomPanel.add(circuitCountTitle);
        bottomPanel.add(circuitCount);
        
        // Add all panels to mainPanel
        add(upperPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }
}
