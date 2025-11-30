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
        
        // Circuit menu
        JMenu circuitMenu = new JMenu("Circuit");
        JMenuItem newCircuitMenuItem = new JMenuItem("New");
        JMenuItem deleteCircuitMenuItem = new JMenuItem("Delete");
        
        circuitMenu.add(newCircuitMenuItem);
        circuitMenu.add(deleteCircuitMenuItem);
        
        menuBar.add(circuitMenu);

        // Add Action Listeners
        newMenuItem.addActionListener(e -> handleNewProject());
        saveMenuItem.addActionListener(e -> handleSaveProject());
        loadMenuItem.addActionListener(e -> handleLoadProject());
        exportMenuItem.addActionListener(e -> handleExportCanvas());
        newCircuitMenuItem.addActionListener(e -> handleNewCircuit());
        deleteCircuitMenuItem.addActionListener(e -> handleDeleteCircuit());

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
        boolean projectSelected = false;
        
        while (!projectSelected) {
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
                        // User cancelled, break inner loop to go back to main dialog
                        break;
                    }
                    
                    if (projectName.trim().isEmpty()) {
                        JOptionPane.showMessageDialog(this, 
                            "Project name cannot be empty!",
                            "Invalid Name", 
                            JOptionPane.WARNING_MESSAGE);
                    }
                }
                
                if (projectName != null && !projectName.trim().isEmpty()) {
                    circuitService.createNewCircuit("Main Circuit");
                    currentProjectName = projectName.trim(); // Store project name
                    currentProjectId = 0; // New project has no ID yet
                    setTitle("LogiSum - " + currentProjectName);
                    mainPage.setProjectName(currentProjectName);
                    mainPage.updateCircuitList(); // Update circuit list after creating first circuit
                    JOptionPane.showMessageDialog(this, 
                        "New project '" + currentProjectName + "' created!\nYou can start designing your circuit.",
                        "Project Created", JOptionPane.INFORMATION_MESSAGE);
                    projectSelected = true;
                }
                    
            } else if (choice == 1) { // Load Existing Project
                if (handleLoadProject()) {
                    projectSelected = true;
                }
            } else {
                // User closed dialog without choosing, loop continues to enforce selection
                // Optionally could add a confirm exit dialog here if they really want to quit app
                int exit = JOptionPane.showConfirmDialog(this, 
                    "You must create or load a project to use the application.\nDo you want to exit LogiSum?", 
                    "Exit Application?", 
                    JOptionPane.YES_NO_OPTION);
                    
                if (exit == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        }
    }

    private void handleNewProject() {
        int result = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to create a new project? Unsaved changes will be lost.", 
            "New Project", JOptionPane.YES_NO_OPTION);
            
        if (result == JOptionPane.YES_OPTION) {
            // Ask for project name FIRST
            String projectName = null;
            while (projectName == null || projectName.trim().isEmpty()) {
                projectName = JOptionPane.showInputDialog(this, 
                    "Enter new project name:",
                    "New Project",
                    JOptionPane.QUESTION_MESSAGE);
                
                if (projectName == null) {
                    // User cancelled, abort operation
                    return;
                }
                
                if (projectName.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, 
                        "Project name cannot be empty!",
                        "Invalid Name", 
                        JOptionPane.WARNING_MESSAGE);
                }
            }
            
            // Only proceed if we have a valid name
            currentProjectName = projectName.trim();
            currentProjectId = 0; // Clear project ID
            circuitService.clearCircuit();
            mainPage.getCircuitCanvas().clearCanvas();
            mainPage.setProjectName(currentProjectName);
            setTitle("LogiSum - " + currentProjectName);
            circuitService.createNewCircuit("Main Circuit");
            mainPage.updateCircuitList(); // Update circuit list after creating new circuit
            
            JOptionPane.showMessageDialog(this, 
                "New project '" + currentProjectName + "' created!",
                "Project Created", JOptionPane.INFORMATION_MESSAGE);
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

    private boolean handleLoadProject() {
        java.util.Map<Integer, String> projects = projectService.getProjectList();
        if (projects == null || projects.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No projects found.");
            return false;
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
                    mainPage.updateCircuitList(); // Update circuit list after loading project
                    
                    JOptionPane.showMessageDialog(this, "Project loaded successfully!");
                    return true;
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to load project or project is empty.");
                    return false;
                }
            }
        }
        return false;
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
    
    private void handleNewCircuit() {
        // Ask user for circuit name
        String circuitName = null;
        while (circuitName == null || circuitName.trim().isEmpty()) {
            circuitName = JOptionPane.showInputDialog(this, 
                "Enter circuit name:",
                "New Circuit",
                JOptionPane.QUESTION_MESSAGE);
            
            if (circuitName == null) {
                // User cancelled
                return;
            }
            
            if (circuitName.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Circuit name cannot be empty!",
                    "Invalid Name", 
                    JOptionPane.WARNING_MESSAGE);
            }
        }
        
        // Create new circuit
        org.scd.business.model.Circuit newCircuit = circuitService.createNewCircuit(circuitName.trim());
        
        // Switch to the new circuit
        circuitService.switchToCircuit(newCircuit);
        
        // Clear and reload canvas for the new (empty) circuit
        mainPage.getCircuitCanvas().clearCanvas();
        
        // Update circuit list in mainPanel to show new circuit as selected
        mainPage.updateCircuitList();
        
        JOptionPane.showMessageDialog(this, 
            "Circuit '" + circuitName.trim() + "' created successfully!",
            "Circuit Created", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void handleDeleteCircuit() {
        org.scd.business.model.Circuit currentCircuit = circuitService.getCurrentCircuit();
        
        if (currentCircuit == null) {
            return;
        }
        
        // Check if it's the main circuit (first one in list)
        java.util.List<org.scd.business.model.Circuit> allCircuits = circuitService.getAllCircuits();
        if (!allCircuits.isEmpty() && allCircuits.get(0).getCircuitId() == currentCircuit.getCircuitId()) {
            JOptionPane.showMessageDialog(this, 
                "Cannot delete the Main Circuit!", 
                "Delete Failed", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete circuit '" + currentCircuit.getCircuitName() + "'?\nThis action cannot be undone.",
            "Delete Circuit",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
            
        if (confirm == JOptionPane.YES_OPTION) {
            boolean deleted = circuitService.deleteCircuit(currentCircuit.getCircuitId());
            
            if (deleted) {
                // Service automatically switches current circuit to main if current was deleted
                org.scd.business.model.Circuit newCurrent = circuitService.getCurrentCircuit();
                
                // Reload canvas with new current circuit
                mainPage.getCircuitCanvas().loadCircuit(newCurrent);
                
                // Update UI list
                mainPage.updateCircuitList();
                
                JOptionPane.showMessageDialog(this,
                    "Circuit deleted successfully.",
                    "Delete Success",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to delete circuit.",
                    "Delete Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

}
