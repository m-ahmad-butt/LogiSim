package org.scd.ui;

import javax.swing.*;
import java.awt.*;

public class Dashboard extends JFrame {
    private CardLayout cl;
    private JPanel card;
    private mainPanel mainPage;
    private org.scd.business.service.ProjectService projectService;
    private org.scd.business.service.CircuitService circuitService;
    private String currentProjectName; // Track current project name
    private int currentProjectId; // Track current project ID

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
        exportMenuItem.addActionListener(e -> handleExportCanvas());

        setJMenuBar(menuBar);
        
        // Make window fullscreen
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setSize(1920, 1080);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        
        // Show startup dialog after window is visible
        SwingUtilities.invokeLater(() -> showStartupDialog());
    }

    private void showStartupDialog() {
        String[] options = {"Create New Project", "Load Existing Project"};
        int choice = JOptionPane.showOptionDialog(
            this,
            "Welcome to LogiSum Circuit Designer!\nWhat would you like to do?",
            "LogiSum - Startup",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]
        );
        
        if (choice == 0) { // Create New Project
            // Keep asking until user provides a valid name or cancels
            String projectName = null;
            while (projectName == null || projectName.trim().isEmpty()) {
                projectName = JOptionPane.showInputDialog(this, 
                    "Enter new project name (required):",
                    "New Project",
                    JOptionPane.QUESTION_MESSAGE);
                
                if (projectName == null) {
                    // User cancelled, ask again if they want to create or load
                    showStartupDialog();
                    return;
                }
                
                if (projectName.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, 
                        "Project name cannot be empty!",
                        "Invalid Name", 
                        JOptionPane.WARNING_MESSAGE);
                }
            }
            
            circuitService.createNewCircuit("Main Circuit");
            currentProjectName = projectName.trim(); // Store project name
            currentProjectId = 0; // New project has no ID yet
            setTitle("LogiSum - " + currentProjectName);
            mainPage.setProjectName(currentProjectName);
            JOptionPane.showMessageDialog(this, 
                "New project '" + currentProjectName + "' created!\nYou can start designing your circuit.",
                "Project Created", JOptionPane.INFORMATION_MESSAGE);
                
        } else if (choice == 1) { // Load Existing Project
            handleLoadProject();
        } else {
            // User closed dialog without choosing, default to create new
            showStartupDialog();
        }
    }

    private void handleNewProject() {
        int result = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to create a new project? Unsaved changes will be lost.", 
            "New Project", JOptionPane.YES_NO_OPTION);
            
        if (result == JOptionPane.YES_OPTION) {
            currentProjectName = null; // Clear project name for new project
            currentProjectId = 0; // Clear project ID
            circuitService.clearCircuit();
            mainPage.getCircuitCanvas().clearCanvas();
            mainPage.setProjectName("");
            setTitle("LogiSum");
            circuitService.createNewCircuit("New Circuit");
        }
    }

    private void handleSaveProject() {
        String projectName = currentProjectName;
        
        // Only ask for project name if we don't have one
        if (projectName == null || projectName.trim().isEmpty()) {
            projectName = JOptionPane.showInputDialog(this, "Enter Project Name:");
            if (projectName == null || projectName.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Project name is required to save!");
                return;
            }
            currentProjectName = projectName.trim();
            setTitle("LogiSum - " + currentProjectName);
            mainPage.setProjectName(currentProjectName);
        }
        
        org.scd.business.model.Project project = new org.scd.business.model.Project();
        project.setProject_Name(currentProjectName);
        project.setProjectId(currentProjectId); // Set the project ID for UPDATE vs INSERT
        
        // Get current circuit
        org.scd.business.model.Circuit currentCircuit = circuitService.getCurrentCircuit();
        if (currentCircuit == null) {
            currentCircuit = circuitService.createNewCircuit("Main Circuit");
        }
        
        java.util.List<org.scd.business.model.Circuit> circuits = new java.util.ArrayList<>();
        circuits.add(currentCircuit);
        project.setCircuits(circuits);
        
        if (projectService.saveProject(project)) {
            // Update project ID after first save
            if (currentProjectId == 0) {
                currentProjectId = project.getProjectId();
            }
            JOptionPane.showMessageDialog(this, "Project saved successfully!");
        } else {
            JOptionPane.showMessageDialog(this, "Failed to save project.");
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
                    
                    // Update window title and store project name and ID
                    currentProjectName = selectedName;
                    currentProjectId = selectedId; // Store the project ID
                    setTitle("LogiSum - " + currentProjectName);
                    mainPage.setProjectName(currentProjectName);
                    
                    JOptionPane.showMessageDialog(this, "Project loaded successfully!");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to load project or project is empty.");
                }
            }
        }
    }

    private void handleExportCanvas() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Circuit as PNG");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PNG Image", "png"));
        
        int userSelection = fileChooser.showSaveDialog(this);
        
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            java.io.File fileToSave = fileChooser.getSelectedFile();
            
            // Ensure .png extension
            String filePath = fileToSave.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".png")) {
                fileToSave = new java.io.File(filePath + ".png");
            }
            
            try {
                mainPage.getCircuitCanvas().exportToPNG(fileToSave);
                JOptionPane.showMessageDialog(this, "Circuit exported successfully to:\n" + fileToSave.getAbsolutePath());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to export circuit: " + ex.getMessage(), 
                    "Export Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

}
