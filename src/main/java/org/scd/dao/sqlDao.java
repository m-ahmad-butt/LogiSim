package org.scd.dao;

import org.scd.business.model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class sqlDao implements daoInterface {
    Connection conn;

    /**
     * default constructor
     */
    public sqlDao() throws SQLException {
        this.conn = sqlSetup.getConnection();
    }

    /**
     * saves project in sql database
     * @param project Object of Project model class
     * @return true if operation was successful, false otherwise
     */
    @Override
    public boolean saveProject(Project project) {
        try {
            conn.setAutoCommit(false);
            
            int projectId = project.getProjectId();
            
            // Check if this is an update or new project
            if (projectId > 0) {
                // UPDATE existing project - delete old data first
                deleteProjectData(projectId);
                
                // Update project name
                String updateProjectSql = "UPDATE Project SET projectName = ? WHERE projectID = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateProjectSql);
                updateStmt.setString(1, project.getProject_Name());
                updateStmt.setInt(2, projectId);
                updateStmt.executeUpdate();
            } else {
                // INSERT new project
                String projectSql = "INSERT INTO Project (projectName) VALUES (?)";
                PreparedStatement projectStmt = conn.prepareStatement(projectSql,
                        Statement.RETURN_GENERATED_KEYS);
                projectStmt.setString(1, project.getProject_Name());
                projectStmt.executeUpdate();

                ResultSet projectKeys = projectStmt.getGeneratedKeys();
                if (projectKeys.next()) {
                    projectId = projectKeys.getInt(1);
                    project.setProjectId(projectId);
                }
            }

            // 2. Save each Circuit
            if (project.getCircuits() != null) {
                for (Circuit circuit : project.getCircuits()) {
                    int circuitId = saveCircuit(circuit, projectId);

                    // 3. Save Gates for this circuit
                    Map<Gate, Integer> gateIdMap = new HashMap<>();

                    if (circuit.getGates() != null) {
                        for (Gate gate : circuit.getGates()) {
                            int gateId = saveGate(gate, circuitId);
                            gateIdMap.put(gate, gateId);

                            // 4. Save Inputs for gates
                            if (gate.getInputs() != null) {
                                saveInputs(gate.getInputs(), gateId);
                            }
                        }
                    }

                    // 5. Save Connectors after all gates are saved
                    if (circuit.getConnectors() != null) {
                        for (Connector connector : circuit.getConnectors()) {
                            saveConnector(connector, gateIdMap, circuit);
                        }
                    }
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void deleteProjectData(int projectId) throws SQLException {
        // Delete in correct order due to foreign keys
        String deleteConnectorsSql = "DELETE FROM Connector WHERE source_id IN " +
            "(SELECT component_id FROM Gate WHERE circuit_id IN " +
            "(SELECT circuitID FROM Circuit WHERE projectID = ?))";
        PreparedStatement deleteConnStmt = conn.prepareStatement(deleteConnectorsSql);
        deleteConnStmt.setInt(1, projectId);
        deleteConnStmt.executeUpdate();
        
        String deleteInputsSql = "DELETE FROM Gate_Input WHERE component_id IN " +
            "(SELECT component_id FROM Gate WHERE circuit_id IN " +
            "(SELECT circuitID FROM Circuit WHERE projectID = ?))";
        PreparedStatement deleteInputsStmt = conn.prepareStatement(deleteInputsSql);
        deleteInputsStmt.setInt(1, projectId);
        deleteInputsStmt.executeUpdate();
        
        String deleteGatesSql = "DELETE FROM Gate WHERE circuit_id IN " +
            "(SELECT circuitID FROM Circuit WHERE projectID = ?)";
        PreparedStatement deleteGatesStmt = conn.prepareStatement(deleteGatesSql);
        deleteGatesStmt.setInt(1, projectId);
        deleteGatesStmt.executeUpdate();
        
        String deleteCircuitsSql = "DELETE FROM Circuit WHERE projectID = ?";
        PreparedStatement deleteCircuitsStmt = conn.prepareStatement(deleteCircuitsSql);
        deleteCircuitsStmt.setInt(1, projectId);
        deleteCircuitsStmt.executeUpdate();
    }

    private int saveCircuit(Circuit circuit, int projectId) throws SQLException {
        String sql = "INSERT INTO Circuit (projectID, circuitName) VALUES (?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        stmt.setInt(1, projectId);
        stmt.setString(2, circuit.getCircuitName());
        stmt.executeUpdate();

        ResultSet keys = stmt.getGeneratedKeys();
        if (keys.next()) {
            return keys.getInt(1);
        }
        throw new SQLException("Failed to get circuit ID");
    }

    private int saveGate(Gate gate, int circuitId) throws SQLException {
        String sql = "INSERT INTO Gate (circuit_id, component_type, positionX, " +
                "positionY, component_output) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

        stmt.setInt(1, circuitId);
        stmt.setString(2, gate.getComponentType());
        stmt.setFloat(3, gate.getPositionX());
        stmt.setFloat(4, gate.getPositionY());
        stmt.setInt(5, gate.isOutput() ? 1 : 0);

        stmt.executeUpdate();
        ResultSet keys = stmt.getGeneratedKeys();
        if (keys.next()) {
            return keys.getInt(1);
        }
        throw new SQLException("Failed to get gate ID");
    }

    private void saveInputs(List<Input> inputs, int gateId) throws SQLException {
        String sql = "INSERT INTO Gate_Input (component_id, input_value, input_order) " +
                "VALUES (?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);

        for (Input input : inputs) {
            stmt.setInt(1, gateId);
            stmt.setString(2, input.getValue() != null ? String.valueOf(input.getValue()) : null);
            stmt.setString(3, String.valueOf(input.getInputIndex()));
            stmt.addBatch();
        }
        stmt.executeBatch();
    }

    private void saveConnector(Connector connector, Map<Gate, Integer> gateIdMap, Circuit circuit)
            throws SQLException {
        String sql = "INSERT INTO Connector (component_color, source_id, sink_id, target_input_index) " +
                "VALUES (?, ?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);

        // Find the Gate objects using the IDs stored in Connector
        Gate sourceGate = circuit.findGateById(connector.getSourceComponentId());
        Gate sinkGate = circuit.findGateById(connector.getTargetComponentId());

        Integer sourceId = gateIdMap.get(sourceGate);
        Integer sinkId = gateIdMap.get(sinkGate);

        if (sourceId != null && sinkId != null) {
            stmt.setString(1, connector.getWireColor());
            stmt.setInt(2, sourceId);
            stmt.setInt(3, sinkId);
            stmt.setInt(4, connector.getTargetInputIndex());
            stmt.executeUpdate();
        }
    }

    /**
     * loads an existing project from database
     * @param projectId id of project to be loaded
     * @return Project Object with project id given in parameter
     */
    @Override
    public Project loadProject(int projectId) {
        try {
            Project project = new Project();

            // 1. Load Project
            String projectSql = "SELECT projectName FROM Project WHERE projectID = ?";
            PreparedStatement projectStmt = conn.prepareStatement(projectSql);
            projectStmt.setInt(1, projectId);
            ResultSet projectRs = projectStmt.executeQuery();

            if (projectRs.next()) {
                project.setProject_Name(projectRs.getString("projectName"));
                project.setProjectId(projectId); // Set the project ID
            } else {
                return null; // Project not found
            }

            // 2. Load Circuits
            List<Circuit> circuits = new ArrayList<>();
            String circuitSql = "SELECT circuitID, circuitName FROM Circuit WHERE projectID = ?";
            PreparedStatement circuitStmt = conn.prepareStatement(circuitSql);
            circuitStmt.setInt(1, projectId);
            ResultSet circuitRs = circuitStmt.executeQuery();

            while (circuitRs.next()) {
                Circuit circuit = new Circuit();
                circuit.setCircuitName(circuitRs.getString("circuitName"));
                int circuitId = circuitRs.getInt("circuitID");

                // 3. Load Gates and build gateMap
                Map<Integer, Gate> gateMap = new HashMap<>();
                List<Gate> gates = loadGates(circuitId, gateMap);
                circuit.setGates(gates);

                // 4. Load Connectors
                List<Connector> connectors = loadConnectors(circuitId, gateMap);
                circuit.setConnectors(connectors);

                circuits.add(circuit);
            }

            project.setCircuits(circuits);
            return project;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<Gate> loadGates(int circuitId, Map<Integer, Gate> gateMap)
            throws SQLException {
        List<Gate> gates = new ArrayList<>();

        // Load all gates
        String sql = "SELECT component_id, component_type, positionX, positionY, " +
                "component_output FROM Gate WHERE circuit_id = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, circuitId);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            int gateId = rs.getInt("component_id");
            String gateType = rs.getString("component_type");
            float posX = rs.getFloat("positionX");
            float posY = rs.getFloat("positionY");
            Integer output = rs.getObject("component_output", Integer.class);

            // Create gate based on type
            Gate gate = createGateByType(gateType, gateId, (int)posX, (int)posY);
            if (gate != null) {
                gate.setOutput(output != null && output == 1);

                // Load inputs for this gate
                List<Input> inputs = loadInputs(gateId);
                gate.setInputs(inputs);

                gates.add(gate);
                gateMap.put(gateId, gate);
            }
        }

        return gates;
    }

    private List<Input> loadInputs(int gateId) throws SQLException {
        List<Input> inputs = new ArrayList<>();
        String sql = "SELECT input_value, input_order FROM Gate_Input " +
                "WHERE component_id = ? ORDER BY input_order";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, gateId);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            Input input = new Input();
            String valStr = rs.getString("input_value");
            if (valStr != null && !valStr.equals("null")) {
                try {
                    input.setValue(Integer.parseInt(valStr));
                } catch (NumberFormatException e) {
                    // ignore or set null
                }
            }
            
            String orderStr = rs.getString("input_order");
            try {
                 input.setInputIndex(Integer.parseInt(orderStr));
            } catch (NumberFormatException e) {
                // ignore
            }
            
            inputs.add(input);
        }

        return inputs;
    }

    private List<Connector> loadConnectors(int circuitId, Map<Integer, Gate> gateMap)
            throws SQLException {
        List<Connector> connectors = new ArrayList<>();

        String sql = "SELECT c.connector_id, c.component_color, c.source_id, c.sink_id, c.target_input_index " +
                "FROM Connector c " +
                "INNER JOIN Gate src ON c.source_id = src.component_id " +
                "WHERE src.circuit_id = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, circuitId);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            Connector connector = new Connector();
            connector.setWireColor(rs.getString("component_color"));
            connector.setConnectorId(rs.getInt("connector_id"));
            connector.setTargetInputIndex(rs.getInt("target_input_index"));

            int sourceId = rs.getInt("source_id");
            int sinkId = rs.getInt("sink_id");

            Gate sourceGate = gateMap.get(sourceId);
            Gate sinkGate = gateMap.get(sinkId);

            if (sourceGate != null && sinkGate != null) {
                connector.setSourceComponentId(sourceGate.getComponentId());
                connector.setTargetComponentId(sinkGate.getComponentId());
                connectors.add(connector);
            }
        }

        return connectors;
    }

    private Gate createGateByType(String type, int id, int x, int y) {
        Gate result = switch (type) {
            case "AND" -> new And(id, x, y);
            case "OR" -> new Or(id, x, y);
            case "NOT" -> new Not(id, x, y);
            default -> null;
        };
        return result;
    }

    @Override
    public Map<Integer, String> getProjectList() {
        Map<Integer, String> projects = new HashMap<>();
        String sql = "SELECT projectID, projectName FROM Project";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                projects.put(rs.getInt("projectID"), rs.getString("projectName"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return projects;
    }

}