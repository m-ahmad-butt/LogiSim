package org.scd.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.scd.business.model.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SqlDaoTest {

    private Connection connection;
    private sqlDao dao;

    @BeforeEach
    void setUp() throws SQLException {
        // Use in-memory SQLite database
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        
        // Create tables
        sqlSetup.createTables(connection);
        
        // Initialize DAO with the test connection
        dao = new sqlDao(connection);
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    void testSaveAndLoadProject() {
        // Create a project structure
        Project project = new Project();
        project.setProject_Name("Test Project");
        
        Circuit circuit = new Circuit();
        circuit.setCircuitName("Main Circuit");
        
        // Add Gates
        And andGate = new And(1, 100, 100);
        Or orGate = new Or(2, 200, 200);
        
        List<Gate> gates = new ArrayList<>();
        gates.add(andGate);
        gates.add(orGate);
        circuit.setGates(gates);
        
        // Add Connectors
        Connector connector = new Connector();
        connector.setSourceComponentId(andGate.getComponentId());
        connector.setTargetComponentId(orGate.getComponentId());
        connector.setWireColor("#000000");
        connector.setTargetInputIndex(0);
        
        List<Connector> connectors = new ArrayList<>();
        connectors.add(connector);
        circuit.setConnectors(connectors);
        
        List<Circuit> circuits = new ArrayList<>();
        circuits.add(circuit);
        project.setCircuits(circuits);

        // Save Project
        boolean saved = dao.saveProject(project);
        assertTrue(saved, "Project should be saved successfully");
        assertTrue(project.getProjectId() > 0, "Project ID should be generated");

        // Load Project
        Project loadedProject = dao.loadProject(project.getProjectId());
        assertNotNull(loadedProject, "Loaded project should not be null");
        assertEquals("Test Project", loadedProject.getProject_Name());
        assertEquals(1, loadedProject.getCircuits().size());
        
        Circuit loadedCircuit = loadedProject.getCircuits().get(0);
        assertEquals("Main Circuit", loadedCircuit.getCircuitName());
        assertEquals(2, loadedCircuit.getGates().size());
        assertEquals(1, loadedCircuit.getConnectors().size());
    }

    @Test
    void testUpdateProject() {
        // 1. Save initial project
        Project project = new Project();
        project.setProject_Name("Initial Name");
        dao.saveProject(project);
        int projectId = project.getProjectId();

        // 2. Modify project
        project.setProject_Name("Updated Name");
        Circuit circuit = new Circuit();
        circuit.setCircuitName("New Circuit");
        List<Circuit> circuits = new ArrayList<>();
        circuits.add(circuit);
        project.setCircuits(circuits);

        // 3. Update (Save again)
        boolean updated = dao.saveProject(project);
        assertTrue(updated, "Update should be successful");

        // 4. Verify update
        Project loadedProject = dao.loadProject(projectId);
        assertEquals("Updated Name", loadedProject.getProject_Name());
        assertEquals(1, loadedProject.getCircuits().size());
        assertEquals("New Circuit", loadedProject.getCircuits().get(0).getCircuitName());
    }
    
    @Test
    void testGetProjectList() {
        Project p1 = new Project();
        p1.setProject_Name("Project 1");
        dao.saveProject(p1);
        
        Project p2 = new Project();
        p2.setProject_Name("Project 2");
        dao.saveProject(p2);
        
        Map<Integer, String> projectList = dao.getProjectList();
        assertEquals(2, projectList.size());
        assertTrue(projectList.containsValue("Project 1"));
        assertTrue(projectList.containsValue("Project 2"));
    }
}
