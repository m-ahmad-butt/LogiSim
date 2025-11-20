import org.scd.*;
import org.scd.dao.*;
import org.junit.jupiter.api.*;
import org.scd.business.model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class sqlDaoTest {
    private Connection connection;
    private sqlDao dao;

    public sqlDaoTest() throws SQLException {
    }

    @BeforeAll
    public void setupDatabase() throws SQLException {
        // Get the shared connection
        connection = sqlSetup.getConnection();

        // CRITICAL: Enable foreign keys for SQLite
        Statement stmt = connection.createStatement();
        stmt.execute("PRAGMA foreign_keys = ON");
        stmt.close();

        // Create tables once
        createTables();
    }

    @BeforeEach
    public void setup() throws SQLException {
        // Clean database before each test - THIS IS CRITICAL
        cleanDatabase();

        // Reset auto-increment sequences
        resetAutoIncrement();

        // Create new DAO instance
        dao = new sqlDao();
    }

    @AfterAll
    public void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    private void createTables() throws SQLException {
        Statement stmt = connection.createStatement();

        // Drop existing tables in correct order (due to foreign keys)
        stmt.execute("DROP TABLE IF EXISTS Connector");
        stmt.execute("DROP TABLE IF EXISTS Gate_Input");
        stmt.execute("DROP TABLE IF EXISTS Gate");
        stmt.execute("DROP TABLE IF EXISTS Circuit");
        stmt.execute("DROP TABLE IF EXISTS Project");

        // Create tables - MATCHING YOUR ACTUAL SCHEMA
        stmt.execute(
                "CREATE TABLE Project (" +
                        "projectID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "projectName TEXT NOT NULL)"
        );

        stmt.execute(
                "CREATE TABLE Circuit (" +
                        "circuitID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "projectID INTEGER NOT NULL, " +
                        "circuitName TEXT NOT NULL, " +
                        "FOREIGN KEY (projectID) REFERENCES Project(projectID) ON DELETE CASCADE)"
        );

        stmt.execute(
                "CREATE TABLE Gate (" +
                        "component_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "circuit_id INTEGER NOT NULL, " +
                        "component_type TEXT NOT NULL, " +
                        "positionX REAL NOT NULL, " +
                        "positionY REAL NOT NULL, " +
                        "component_output INTEGER, " +
                        "FOREIGN KEY (circuit_id) REFERENCES Circuit(circuitID) ON DELETE CASCADE)"
        );

        stmt.execute(
                "CREATE TABLE Gate_Input (" +
                        "input_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "component_id INTEGER NOT NULL, " +
                        "input_value INTEGER, " +
                        "input_order INTEGER, " +
                        "FOREIGN KEY (component_id) REFERENCES Gate(component_id) ON DELETE CASCADE)"
        );

        stmt.execute(
                "CREATE TABLE Connector (" +
                        "connector_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "component_color TEXT, " +
                        "source_id INTEGER NOT NULL, " +
                        "sink_id INTEGER NOT NULL, " +
                        "FOREIGN KEY (source_id) REFERENCES Gate(component_id) ON DELETE CASCADE, " +
                        "FOREIGN KEY (sink_id) REFERENCES Gate(component_id) ON DELETE CASCADE)"
        );

        stmt.close();
    }

    private void cleanDatabase() throws SQLException {
        Statement stmt = connection.createStatement();
        // Delete in reverse order of foreign key dependencies
        stmt.execute("DELETE FROM Connector");
        stmt.execute("DELETE FROM Gate_Input");
        stmt.execute("DELETE FROM Gate");
        stmt.execute("DELETE FROM Circuit");
        stmt.execute("DELETE FROM Project");
        stmt.close();
    }

    private void resetAutoIncrement() throws SQLException {
        // Reset SQLite auto-increment sequences
        Statement stmt = connection.createStatement();
        stmt.execute("DELETE FROM sqlite_sequence WHERE name='Project'");
        stmt.execute("DELETE FROM sqlite_sequence WHERE name='Circuit'");
        stmt.execute("DELETE FROM sqlite_sequence WHERE name='Gate'");
        stmt.execute("DELETE FROM sqlite_sequence WHERE name='Gate_Input'");
        stmt.execute("DELETE FROM sqlite_sequence WHERE name='Connector'");
        stmt.close();
    }

    // ==================== Test Helper Methods ====================

    private Project createSampleProject() {
        Project project = new Project();
        project.setProject_Name("Test Project");

        List<Circuit> circuits = new ArrayList<>();
        circuits.add(createSampleCircuit("Circuit 1"));
        circuits.add(createSampleCircuit("Circuit 2"));

        project.setCircuits(circuits);
        return project;
    }

    private Circuit createSampleCircuit(String name) {
        Circuit circuit = new Circuit();
        circuit.setCircuit_Name(name);

        // Create gates
        List<Gate> gates = new ArrayList<>();

        And andGate = new And();
        andGate.setGate_Name("And");
        andGate.setPosition_X(100.0f);
        andGate.setPosition_Y(200.0f);
        andGate.setOutput(false);

        List<Input> andInputs = new ArrayList<>();
        Input input1 = new Input();
        input1.setInput_Value("0");
        input1.setInput_Order("1");
        andInputs.add(input1);

        Input input2 = new Input();
        input2.setInput_Value("1");
        input2.setInput_Order("2");
        andInputs.add(input2);

        andGate.setInputs(andInputs);
        gates.add(andGate);

        Or orGate = new Or();
        orGate.setGate_Name("Or");
        orGate.setPosition_X(300.0f);
        orGate.setPosition_Y(400.0f);
        orGate.setOutput(true);

        List<Input> orInputs = new ArrayList<>();
        Input input3 = new Input();
        input3.setInput_Value("1");
        input3.setInput_Order("1");
        orInputs.add(input3);

        orGate.setInputs(orInputs);
        gates.add(orGate);

        Not notGate = new Not();
        notGate.setGate_Name("Not");
        notGate.setPosition_X(500.0f);
        notGate.setPosition_Y(600.0f);
        notGate.setOutput(false);
        gates.add(notGate);

        circuit.setGates(gates);

        // Create connectors
        List<Connector> connectors = new ArrayList<>();

        Connector connector1 = new Connector();
        connector1.setConnector_Color("RED");
        connector1.setSource_Gate(andGate);
        connector1.setSink_Gate(orGate);
        connectors.add(connector1);

        Connector connector2 = new Connector();
        connector2.setConnector_Color("BLUE");
        connector2.setSource_Gate(orGate);
        connector2.setSink_Gate(notGate);
        connectors.add(connector2);

        circuit.setConnectors(connectors);

        return circuit;
    }

    // ==================== Test Cases ====================

    @Test
    @Order(1)
    @DisplayName("Test save and load simple project")
    public void testSaveAndLoadSimpleProject() throws SQLException {
        // Create a simple project
        Project project = new Project();
        project.setProject_Name("Simple Project");

        Circuit circuit = new Circuit();
        circuit.setCircuit_Name("Simple Circuit");

        List<Gate> gates = new ArrayList<>();
        And andGate = new And();
        andGate.setGate_Name("And");
        andGate.setPosition_X(10.0f);
        andGate.setPosition_Y(20.0f);
        andGate.setOutput(true);
        gates.add(andGate);

        circuit.setGates(gates);
        circuit.setConnectors(new ArrayList<>()); // Empty connectors

        List<Circuit> circuits = new ArrayList<>();
        circuits.add(circuit);
        project.setCircuits(circuits);

        // Save project
        boolean saveResult = dao.saveProject(project);
        assertTrue(saveResult, "Project should be saved successfully");

        // Load project
        Project loadedProject = dao.loadProject(1);
        assertNotNull(loadedProject, "Loaded project should not be null");
        assertEquals("Simple Project", loadedProject.getProject_Name());
        assertEquals(1, loadedProject.getCircuits().size());
        assertEquals("Simple Circuit", loadedProject.getCircuits().get(0).getCircuit_Name());
        assertEquals(1, loadedProject.getCircuits().get(0).getGates().size());
    }

    @Test
    @Order(2)
    @DisplayName("Test save project with multiple circuits")
    public void testSaveProjectWithMultipleCircuits() {
        Project project = createSampleProject();

        boolean result = dao.saveProject(project);
        assertTrue(result, "Project with multiple circuits should be saved successfully");

        // Verify in database
        Project loadedProject = dao.loadProject(1);

        assertNotNull(loadedProject);
        assertEquals(2, loadedProject.getCircuits().size());
        assertEquals("Circuit 1", loadedProject.getCircuits().get(0).getCircuit_Name());
        assertEquals("Circuit 2", loadedProject.getCircuits().get(1).getCircuit_Name());
    }

    @Test
    @Order(3)
    @DisplayName("Test save and load gates with inputs")
    public void testSaveAndLoadGatesWithInputs() {
        Project project = createSampleProject();
        dao.saveProject(project);

        Project loadedProject = dao.loadProject(1);
        Circuit loadedCircuit = loadedProject.getCircuits().get(0);

        assertNotNull(loadedCircuit.getGates());
        assertEquals(3, loadedCircuit.getGates().size());

        // Check AND gate
        Gate andGate = loadedCircuit.getGates().get(0);
        assertEquals("And", andGate.getGate_Name());
        assertEquals(100.0f, andGate.getPosition_X());
        assertEquals(200.0f, andGate.getPosition_Y());
        assertFalse(andGate.isOutput());

        // Check inputs
        assertNotNull(andGate.getInputs());
        assertEquals(2, andGate.getInputs().size());
        assertEquals("0", andGate.getInputs().get(0).getInput_Value());
        assertEquals("1", andGate.getInputs().get(0).getInput_Order());

        // Check OR gate
        Gate orGate = loadedCircuit.getGates().get(1);
        assertEquals("Or", orGate.getGate_Name());
        assertTrue(orGate.isOutput());
        assertEquals(1, orGate.getInputs().size());
    }

    @Test
    @Order(4)
    @DisplayName("Test save and load connectors")
    public void testSaveAndLoadConnectors() {
        Project project = createSampleProject();
        dao.saveProject(project);

        Project loadedProject = dao.loadProject(1);
        Circuit loadedCircuit = loadedProject.getCircuits().get(0);

        assertNotNull(loadedCircuit.getConnectors());
        assertEquals(2, loadedCircuit.getConnectors().size());

        // Check first connector
        Connector connector1 = loadedCircuit.getConnectors().get(0);
        assertEquals("RED", connector1.getConnector_Color());
        assertNotNull(connector1.getSource_Gate());
        assertNotNull(connector1.getSink_Gate());
        assertEquals("And", connector1.getSource_Gate().getGate_Name());
        assertEquals("Or", connector1.getSink_Gate().getGate_Name());

        // Check second connector
        Connector connector2 = loadedCircuit.getConnectors().get(1);
        assertEquals("BLUE", connector2.getConnector_Color());
        assertEquals("Or", connector2.getSource_Gate().getGate_Name());
        assertEquals("Not", connector2.getSink_Gate().getGate_Name());
    }

    @Test
    @Order(5)
    @DisplayName("Test load non-existent project")
    public void testLoadNonExistentProject() {
        Project project = dao.loadProject(999);
        assertNull(project, "Loading non-existent project should return null");
    }

    @Test
    @Order(6)
    @DisplayName("Test save project with null circuits")
    public void testSaveProjectWithNullCircuits() {
        Project project = new Project();
        project.setProject_Name("Empty Project");
        project.setCircuits(null);

        boolean result = dao.saveProject(project);
        assertTrue(result, "Should handle null circuits gracefully");

        Project loadedProject = dao.loadProject(1);
        assertNotNull(loadedProject);
        assertEquals("Empty Project", loadedProject.getProject_Name());
    }

    @Test
    @Order(7)
    @DisplayName("Test save circuit with null gates")
    public void testSaveCircuitWithNullGates() {
        Project project = new Project();
        project.setProject_Name("Project with Empty Circuit");

        Circuit circuit = new Circuit();
        circuit.setCircuit_Name("Empty Circuit");
        circuit.setGates(null);
        circuit.setConnectors(null);

        List<Circuit> circuits = new ArrayList<>();
        circuits.add(circuit);
        project.setCircuits(circuits);

        boolean result = dao.saveProject(project);
        assertTrue(result, "Should handle null gates gracefully");

        Project loadedProject = dao.loadProject(1);
        Circuit loadedCircuit = loadedProject.getCircuits().get(0);
        assertNotNull(loadedCircuit);
        assertEquals("Empty Circuit", loadedCircuit.getCircuit_Name());
    }

    @Test
    @Order(8)
    @DisplayName("Test gate type creation")
    public void testGateTypeCreation() {
        Project project = new Project();
        project.setProject_Name("Gate Type Test");

        Circuit circuit = new Circuit();
        circuit.setCircuit_Name("Test Circuit");

        List<Gate> gates = new ArrayList<>();

        // Test different gate types
        And andGate = new And();
        andGate.setGate_Name("And");
        andGate.setPosition_X(0.0f);
        andGate.setPosition_Y(0.0f);
        gates.add(andGate);

        Or orGate = new Or();
        orGate.setGate_Name("Or");
        orGate.setPosition_X(0.0f);
        orGate.setPosition_Y(0.0f);
        gates.add(orGate);

        Not notGate = new Not();
        notGate.setGate_Name("Not");
        notGate.setPosition_X(0.0f);
        notGate.setPosition_Y(0.0f);
        gates.add(notGate);

        circuit.setGates(gates);
        circuit.setConnectors(new ArrayList<>());

        List<Circuit> circuits = new ArrayList<>();
        circuits.add(circuit);
        project.setCircuits(circuits);

        dao.saveProject(project);

        Project loadedProject = dao.loadProject(1);
        List<Gate> loadedGates = loadedProject.getCircuits().get(0).getGates();

        assertEquals(3, loadedGates.size());
        assertTrue(loadedGates.get(0) instanceof And);
        assertTrue(loadedGates.get(1) instanceof Or);
        assertTrue(loadedGates.get(2) instanceof Not);
    }

    @Test
    @Order(9)
    @DisplayName("Test connector references maintain gate identity")
    public void testConnectorGateReferences() {
        Project project = createSampleProject();
        dao.saveProject(project);

        Project loadedProject = dao.loadProject(1);
        Circuit circuit = loadedProject.getCircuits().get(0);

        List<Gate> gates = circuit.getGates();
        List<Connector> connectors = circuit.getConnectors();

        // Verify that connector references point to actual gate objects in the list
        Connector connector1 = connectors.get(0);
        Gate sourceGate = connector1.getSource_Gate();
        Gate sinkGate = connector1.getSink_Gate();

        // Check if source and sink gates are in the gates list
        assertTrue(gates.contains(sourceGate), "Source gate should be in gates list");
        assertTrue(gates.contains(sinkGate), "Sink gate should be in gates list");

        // Verify same object reference (not just equals)
        assertSame(gates.get(0), sourceGate, "Should be same object reference");
        assertSame(gates.get(1), sinkGate, "Should be same object reference");
    }

    @Test
    @Order(10)
    @DisplayName("Test multiple projects can be saved")
    public void testMultipleProjects() {
        Project project1 = new Project();
        project1.setProject_Name("Project 1");
        project1.setCircuits(new ArrayList<>());

        Project project2 = new Project();
        project2.setProject_Name("Project 2");
        project2.setCircuits(new ArrayList<>());

        assertTrue(dao.saveProject(project1));
        assertTrue(dao.saveProject(project2));

        Project loaded1 = dao.loadProject(1);
        Project loaded2 = dao.loadProject(2);

        assertNotNull(loaded1);
        assertNotNull(loaded2);
        assertEquals("Project 1", loaded1.getProject_Name());
        assertEquals("Project 2", loaded2.getProject_Name());
    }

    @Test
    @Order(11)
    @DisplayName("Test gate with no inputs")
    public void testGateWithNoInputs() {
        Project project = new Project();
        project.setProject_Name("Test Project");

        Circuit circuit = new Circuit();
        circuit.setCircuit_Name("Test Circuit");

        List<Gate> gates = new ArrayList<>();
        Not notGate = new Not();
        notGate.setGate_Name("Not");
        notGate.setPosition_X(0.0f);
        notGate.setPosition_Y(0.0f);
        notGate.setInputs(null); // No inputs
        gates.add(notGate);

        circuit.setGates(gates);
        circuit.setConnectors(new ArrayList<>());

        List<Circuit> circuits = new ArrayList<>();
        circuits.add(circuit);
        project.setCircuits(circuits);

        assertTrue(dao.saveProject(project));

        Project loadedProject = dao.loadProject(1);
        Gate loadedGate = loadedProject.getCircuits().get(0).getGates().get(0);

        assertNotNull(loadedGate);
        // Inputs should be empty list, not null
        assertNotNull(loadedGate.getInputs());
        assertEquals(0, loadedGate.getInputs().size());
    }

    @Test
    @Order(12)
    @DisplayName("Test complex circuit with multiple connectors")
    public void testComplexCircuitWithMultipleConnectors() {
        Project project = new Project();
        project.setProject_Name("Complex Project");

        Circuit circuit = new Circuit();
        circuit.setCircuit_Name("Complex Circuit");

        // Create 4 gates
        List<Gate> gates = new ArrayList<>();
        And gate1 = new And();
        gate1.setGate_Name("And");
        gate1.setPosition_X(0.0f);
        gate1.setPosition_Y(0.0f);
        gates.add(gate1);

        Or gate2 = new Or();
        gate2.setGate_Name("Or");
        gate2.setPosition_X(100.0f);
        gate2.setPosition_Y(100.0f);
        gates.add(gate2);

        Not gate3 = new Not();
        gate3.setGate_Name("Not");
        gate3.setPosition_X(200.0f);
        gate3.setPosition_Y(200.0f);
        gates.add(gate3);

        And gate4 = new And();
        gate4.setGate_Name("And");
        gate4.setPosition_X(300.0f);
        gate4.setPosition_Y(300.0f);
        gates.add(gate4);

        circuit.setGates(gates);

        // Create multiple connectors
        List<Connector> connectors = new ArrayList<>();

        Connector c1 = new Connector();
        c1.setConnector_Color("RED");
        c1.setSource_Gate(gate1);
        c1.setSink_Gate(gate2);
        connectors.add(c1);

        Connector c2 = new Connector();
        c2.setConnector_Color("BLUE");
        c2.setSource_Gate(gate2);
        c2.setSink_Gate(gate3);
        connectors.add(c2);

        Connector c3 = new Connector();
        c3.setConnector_Color("GREEN");
        c3.setSource_Gate(gate3);
        c3.setSink_Gate(gate4);
        connectors.add(c3);

        Connector c4 = new Connector();
        c4.setConnector_Color("YELLOW");
        c4.setSource_Gate(gate1);
        c4.setSink_Gate(gate4);
        connectors.add(c4);

        circuit.setConnectors(connectors);

        List<Circuit> circuits = new ArrayList<>();
        circuits.add(circuit);
        project.setCircuits(circuits);

        assertTrue(dao.saveProject(project));

        Project loadedProject = dao.loadProject(1);
        Circuit loadedCircuit = loadedProject.getCircuits().get(0);

        assertEquals(4, loadedCircuit.getGates().size());
        assertEquals(4, loadedCircuit.getConnectors().size());

        // Verify all connectors are properly linked
        for (Connector connector : loadedCircuit.getConnectors()) {
            assertNotNull(connector.getSource_Gate());
            assertNotNull(connector.getSink_Gate());
            assertNotNull(connector.getConnector_Color());
        }
    }

    @Test
    @Order(13)
    @DisplayName("Test load existing project from DAO")
    public void testLoadProjectFromDao() throws SQLException {
        // Step 1: Create and save a sample project
        Project project = createSampleProject();
        boolean saveResult = dao.saveProject(project);
        assertTrue(saveResult, "Project should be saved successfully");

        // Step 2: Load the project by ID
        Project loadedProject = dao.loadProject(1);

        // Step 3: Verify loaded project is not null
        assertNotNull(loadedProject, "Loaded project should not be null");

        // Step 4: Verify basic project properties
        assertEquals(project.getProject_Name(), loadedProject.getProject_Name(), "Project names should match");

        // Step 5: Verify circuits
        assertNotNull(loadedProject.getCircuits(), "Circuits list should not be null");
        assertEquals(project.getCircuits().size(), loadedProject.getCircuits().size(), "Circuit count should match");

        // Step 6: Verify gates and connectors of the first circuit
        Circuit originalCircuit = project.getCircuits().get(0);
        Circuit loadedCircuit = loadedProject.getCircuits().get(0);

        assertEquals(originalCircuit.getCircuit_Name(), loadedCircuit.getCircuit_Name(), "Circuit names should match");
        assertEquals(originalCircuit.getGates().size(), loadedCircuit.getGates().size(), "Gate count should match");
        assertEquals(originalCircuit.getConnectors().size(), loadedCircuit.getConnectors().size(), "Connector count should match");

        // Step 7: Verify individual gate properties (example: first gate)
        Gate originalGate = originalCircuit.getGates().get(0);
        Gate loadedGate = loadedCircuit.getGates().get(0);

        assertEquals(originalGate.getGate_Name(), loadedGate.getGate_Name(), "Gate names should match");
        assertEquals(originalGate.getPosition_X(), loadedGate.getPosition_X(), "Gate X positions should match");
        assertEquals(originalGate.getPosition_Y(), loadedGate.getPosition_Y(), "Gate Y positions should match");
        assertEquals(originalGate.isOutput(), loadedGate.isOutput(), "Gate outputs should match");

        // Step 8: Verify connector links
        Connector originalConnector = originalCircuit.getConnectors().get(0);
        Connector loadedConnector = loadedCircuit.getConnectors().get(0);

        assertEquals(originalConnector.getConnector_Color(), loadedConnector.getConnector_Color(), "Connector colors should match");
        assertEquals(originalConnector.getSource_Gate().getGate_Name(), loadedConnector.getSource_Gate().getGate_Name(), "Connector source gate names should match");
        assertEquals(originalConnector.getSink_Gate().getGate_Name(), loadedConnector.getSink_Gate().getGate_Name(), "Connector sink gate names should match");
    }
}