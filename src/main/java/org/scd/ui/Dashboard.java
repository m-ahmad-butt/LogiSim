package org.scd.ui;

import javax.swing.*;
import java.awt.*;

public class Dashboard extends JFrame {
    private CardLayout cl;
    private JPanel card;
    private mainPanel mainPage;
    private org.scd.business.service.ProjectService projectService;
    private org.scd.business.service.CircuitService circuitService;

    public Dashboard() {
        projectService = new org.scd.business.service.ProjectService();
        circuitService = org.scd.business.service.CircuitService.getInstance();

        //main frame
        cl = new CardLayout();
        card = new JPanel(cl);
        mainPage = new mainPanel(); //main panel
        card.add(mainPage,"mainPage");
        cl.show(card,"mainPage");
        add(card);
        
        // Create menu bar
        JMenuBar menuBar = new JMenuBar();
        
        JMenu fileMenu = new JMenu("File");
        JMenuItem newMenuItem = new JMenuItem("New Project");
        JMenuItem loadMenuItem = new JMenuItem("Load Project");
        JMenuItem saveMenuItem = new JMenuItem("Save Project");
        JMenuItem exportMenuItem = new JMenuItem("Export");
        
        fileMenu.add(newMenuItem);
        fileMenu.add(loadMenuItem);
        fileMenu.add(saveMenuItem);
        fileMenu.add(exportMenuItem);
        
        menuBar.add(fileMenu);

        // Add Action Listeners
        newMenuItem.addActionListener(e -> handleNewProject());
        saveMenuItem.addActionListener(e -> handleSaveProject());
        loadMenuItem.addActionListener(e -> handleLoadProject());

        setJMenuBar(menuBar);
        
        // Make window fullscreen
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setSize(1920, 1080);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void handleNewProject() {
        int result = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to create a new project? Unsaved changes will be lost.", 
            "New Project", JOptionPane.YES_NO_OPTION);
            
        if (result == JOptionPane.YES_OPTION) {
            circuitService.clearCircuit();
            mainPage.getCircuitCanvas().clearCanvas();
            circuitService.createNewCircuit("New Circuit");
        }
    }

    private void handleSaveProject() {
        String projectName = JOptionPane.showInputDialog(this, "Enter Project Name:");
        if (projectName != null && !projectName.trim().isEmpty()) {
            org.scd.business.model.Project project = new org.scd.business.model.Project();
            project.setProject_Name(projectName);
            
            // Get current circuit
            org.scd.business.model.Circuit currentCircuit = circuitService.getCurrentCircuit();
            if (currentCircuit == null) {
                currentCircuit = circuitService.createNewCircuit("Main Circuit");
            }
            
            java.util.List<org.scd.business.model.Circuit> circuits = new java.util.ArrayList<>();
            circuits.add(currentCircuit);
            project.setCircuits(circuits);
            
            if (projectService.saveProject(project)) {
                JOptionPane.showMessageDialog(this, "Project saved successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save project.");
            }
        }
    }

    private void handleLoadProject() {
        java.util.Map<Integer, String> projects = projectService.getProjectList();
        if (projects == null || projects.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No projects found.");
            return;
        }

        // Create selection array
        String[] options = projects.values().toArray(new String[0]);
        Integer[] ids = projects.keySet().toArray(new Integer[0]);
        
        String selectedName = (String) JOptionPane.showInputDialog(this, 
            "Select a project to load:", "Load Project", 
            JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
            
        if (selectedName != null) {
            // Find ID for selected name
            Integer selectedId = null;
            for (java.util.Map.Entry<Integer, String> entry : projects.entrySet()) {
                if (entry.getValue().equals(selectedName)) {
                    selectedId = entry.getKey();
                    break;
                }
            }
            
            if (selectedId != null) {
                org.scd.business.model.Project loadedProject = projectService.loadProject(selectedId);
                if (loadedProject != null && loadedProject.getCircuits() != null && !loadedProject.getCircuits().isEmpty()) {
                    // Load the first circuit for now
                    org.scd.business.model.Circuit circuit = loadedProject.getCircuits().get(0);
                    mainPage.getCircuitCanvas().loadCircuit(circuit);
                    JOptionPane.showMessageDialog(this, "Project loaded successfully!");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to load project or project is empty.");
                }
            }
        }
    }

}
