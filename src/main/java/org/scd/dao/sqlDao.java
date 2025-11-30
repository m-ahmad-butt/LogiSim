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

            // 1. Save Project
            String projectSql = "INSERT INTO Project (projectName) VALUES (?)";
            PreparedStatement projectStmt = conn.prepareStatement(projectSql,
                    Statement.RETURN_GENERATED_KEYS);
            projectStmt.setString(1, project.getProject_Name());
            projectStmt.executeUpdate();

            ResultSet projectKeys = projectStmt.getGeneratedKeys();
            int projectId = 0;
            if (projectKeys.next()) {
                projectId = projectKeys.getInt(1);
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
                            saveConnector(connector, gateIdMap);
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

    private int saveCircuit(Circuit circuit, int projectId) throws SQLException {
        String sql = "INSERT INTO Circuit (projectID, circuitName) VALUES (?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        stmt.setInt(1, projectId);
        stmt.setString(2, circuit.getCircuit_Name());
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
        stmt.setString(2, gate.getGate_Name());
        stmt.setFloat(3, gate.getPosition_X());
        stmt.setFloat(4, gate.getPosition_Y());
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
            stmt.setString(2, input.getInput_Value());
            stmt.setString(3, input.getInput_Order());
            stmt.addBatch();
        }
        stmt.executeBatch();
    }

    private void saveConnector(Connector connector, Map<Gate, Integer> gateIdMap)
            throws SQLException {
        String sql = "INSERT INTO Connector (component_color, source_id, sink_id) " +
                "VALUES (?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);

        Integer sourceId = gateIdMap.get(connector.getSource_Gate());
        Integer sinkId = gateIdMap.get(connector.getSink_Gate());

        if (sourceId != null && sinkId != null) {
            stmt.setString(1, connector.getConnector_Color());
            stmt.setInt(2, sourceId);
            stmt.setInt(3, sinkId);
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
                circuit.setCircuit_Name(circuitRs.getString("circuitName"));
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
            Gate gate = createGateByType(gateType);
            if (gate != null) {
                gate.setGate_Name(gateType);
                gate.setPosition_X(posX);
                gate.setPosition_Y(posY);
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
            input.setInput_Value(rs.getString("input_value"));
            input.setInput_Order(rs.getString("input_order"));
            inputs.add(input);
        }

        return inputs;
    }

    private List<Connector> loadConnectors(int circuitId, Map<Integer, Gate> gateMap)
            throws SQLException {
        List<Connector> connectors = new ArrayList<>();

        String sql = "SELECT c.connector_id, c.component_color, c.source_id, c.sink_id " +
                "FROM Connector c " +
                "INNER JOIN Gate src ON c.source_id = src.component_id " +
                "WHERE src.circuit_id = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, circuitId);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            Connector connector = new Connector();
            connector.setConnector_Color(rs.getString("component_color"));

            int sourceId = rs.getInt("source_id");
            int sinkId = rs.getInt("sink_id");

            Gate sourceGate = gateMap.get(sourceId);
            Gate sinkGate = gateMap.get(sinkId);

            if (sourceGate != null && sinkGate != null) {
                connector.setSource_Gate(sourceGate);
                connector.setSink_Gate(sinkGate);
                connectors.add(connector);
            }
        }

        return connectors;
    }

    private Gate createGateByType(String type) {
        Gate result = switch (type) {
            case "And" -> new And();
            case "Or" -> new Or();
            case "Not" -> new Not();
            default -> null;
        };
        return result;
    }

}