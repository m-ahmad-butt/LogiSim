package org.scd.business.service;

import org.scd.business.model.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CircuitService {
    private static CircuitService instance;
    private Circuit currentCircuit;
    private List<Circuit> allCircuits; // Track all circuits in the project
    
    // Component ID management
    private int componentIdCounter = 0;
    private int connectorIdCounter = 0;
    private int circuitIdCounter = 0;
    
    // Maps to track UI components linked to business models
    private Map<Integer, Object> uiComponentMap; // componentId -> UI component
    
    private CircuitService() {
        currentCircuit = new Circuit();
        allCircuits = new ArrayList<>();
        uiComponentMap = new HashMap<>();
    }
    
    public static CircuitService getInstance() {
        if (instance == null) {
            instance = new CircuitService();
        }
        return instance;
    }
    
   
    public Circuit createNewCircuit(String name) {
        currentCircuit = new Circuit(generateCircuitId(), name);
        allCircuits.add(currentCircuit);
        uiComponentMap.clear();
        return currentCircuit;
    }
    

    public Circuit getCurrentCircuit() {
        return currentCircuit;
    }
    
  
    public void setCurrentCircuit(Circuit circuit) {
        this.currentCircuit = circuit;
    }


    public Gate addGate(String gateType, int positionX, int positionY) {
        Gate gate;
        int id = generateComponentId();
        
        switch (gateType.toUpperCase()) {
            case "AND":
                gate = new And(id, positionX, positionY);
                break;
            case "OR":
                gate = new Or(id, positionX, positionY);
                break;
            case "NOT":
                gate = new Not(id, positionX, positionY);
                break;
            default:
                throw new IllegalArgumentException("Unknown gate type: " + gateType);
        }
        
        currentCircuit.addGate(gate);
        return gate;
    }
    

    public LED addLED(int positionX, int positionY) {
        LED led = new LED(generateComponentId(), positionX, positionY);
        currentCircuit.addLED(led);
        return led;
    }
    
 
    public Connector addConnector(int sourceId, int targetId, int targetInputIndex, String color) {
        Connector connector = new Connector(
            generateConnectorId(), 
            sourceId, 
            targetId, 
            targetInputIndex, 
            color
        );
        currentCircuit.addConnector(connector);
        
        // Update target input connection
        Gate targetGate = findGate(targetId);
        if (targetGate != null) {
            Input input = (targetInputIndex == 0) ? targetGate.getInput1() : targetGate.getInput2();
            if (input != null) {
                input.setSourceComponentId(sourceId);
            }
        } else {
            LED targetLED = findLED(targetId);
            if (targetLED != null) {
                targetLED.getInput().setSourceComponentId(sourceId);
            }
        }
        
        return connector;
    }
    
 
    public void removeGate(int gateId) {
        currentCircuit.removeGate(gateId);
        uiComponentMap.remove(gateId);
    }
    

    public void removeLED(int ledId) {
        currentCircuit.removeLED(ledId);
        uiComponentMap.remove(ledId);
    }
    
 
    public void removeConnector(int connectorId) {
        currentCircuit.removeConnector(connectorId);
    }
    
  
    public Gate findGate(int gateId) {
        return currentCircuit.findGateById(gateId);
    }
    
   
    public LED findLED(int ledId) {
        return currentCircuit.findLEDById(ledId);
    }
    

    public void setGateInput(int gateId, int inputIndex, Integer value, Integer sourceComponentId) {
        Gate gate = findGate(gateId);
        if (gate != null) {
            Input input = (inputIndex == 0) ? gate.getInput1() : gate.getInput2();
            if (input != null) {
                input.setValue(value);
                input.setSourceComponentId(sourceComponentId);
                calculateCircuit();
            }
        }
    }
    
  
    public void setLEDInput(int ledId, Integer value, Integer sourceComponentId) {
        LED led = findLED(ledId);
        if (led != null) {
            led.getInput().setValue(value);
            led.getInput().setSourceComponentId(sourceComponentId);
            led.calculate();
        }
    }
    
   
    public void calculateCircuit() {
        // Propagate signals through wires first
        boolean changed = true;
        int maxIterations = 10; // Prevent infinite loops
        int iterations = 0;
        
        while (changed && iterations < maxIterations) {
            changed = propagateSignals();
            
            // Calculate all gates
            for (Gate gate : currentCircuit.getGates()) {
                Integer oldOutput = gate.getOutput();
                gate.calculate();
                Integer newOutput = gate.getOutput();
                
                if (oldOutput != newOutput && (oldOutput == null || !oldOutput.equals(newOutput))) {
                    changed = true;
                }
            }
            iterations++;
        }
        
        // Final calculation for LEDs
        for (LED led : currentCircuit.getLeds()) {
            led.calculate();
        }
    }
    
    private boolean propagateSignals() {
        boolean changed = false;
        for (Connector connector : currentCircuit.getConnectors()) {
            Integer sourceOutput = getComponentOutput(connector.getSourceComponentId());
            
            // Update target input
            Gate targetGate = findGate(connector.getTargetComponentId());
            if (targetGate != null) {
                Input targetInput = (connector.getTargetInputIndex() == 0) ? 
                                    targetGate.getInput1() : targetGate.getInput2();
                
                if (targetInput != null) {
                    Integer currentValue = targetInput.getValue();
                    if (currentValue != sourceOutput && (currentValue == null || !currentValue.equals(sourceOutput))) {
                        targetInput.setValue(sourceOutput);
                        changed = true;
                    }
                }
            } else {
                LED targetLED = findLED(connector.getTargetComponentId());
                if (targetLED != null) {
                    Integer currentValue = targetLED.getInput().getValue();
                    if (currentValue != sourceOutput && (currentValue == null || !currentValue.equals(sourceOutput))) {
                        targetLED.getInput().setValue(sourceOutput);
                        changed = true;
                    }
                }
            }
        }
        return changed;
    }
    
  
    public List<String[]> generateTruthTable() {
        List<String[]> table = new ArrayList<>();
        
        // 1. Identify Circuit Inputs (Unconnected inputs)
        List<Input> circuitInputs = new ArrayList<>();
        List<String> inputLabels = new ArrayList<>();
        
        for (Gate gate : currentCircuit.getGates()) {
            // Check Input 1
            if (gate.getInput1() != null && !gate.getInput1().isConnected()) {
                circuitInputs.add(gate.getInput1());
                inputLabels.add(gate.getGateType() + " " + gate.getComponentId() + " In 1");
            }
            // Check Input 2
            if (gate.getInput2() != null && !gate.getInput2().isConnected()) {
                circuitInputs.add(gate.getInput2());
                inputLabels.add(gate.getGateType() + " " + gate.getComponentId() + " In 2");
            }
        }
        
        // 2. Identify Circuit Outputs (LEDs + Gates not acting as sources)
        List<Object> circuitOutputs = new ArrayList<>(); // Gate or LED
        List<String> outputLabels = new ArrayList<>();
        
        // Find all components that are acting as sources
        java.util.Set<Integer> sourceComponentIds = new java.util.HashSet<>();
        for (Connector connector : currentCircuit.getConnectors()) {
            sourceComponentIds.add(connector.getSourceComponentId());
        }
        
        // Add Gates that are NOT sources as outputs
        for (Gate gate : currentCircuit.getGates()) {
            if (!sourceComponentIds.contains(gate.getComponentId())) {
                circuitOutputs.add(gate);
                outputLabels.add(gate.getGateType() + " " + gate.getComponentId() + " Out");
            }
        }
        
        // Add all LEDs as outputs
        for (LED led : currentCircuit.getLeds()) {
            circuitOutputs.add(led);
            outputLabels.add("LED " + led.getComponentId());
        }
        
        // 3. Build Header
        List<String> header = new ArrayList<>();
        header.addAll(inputLabels);
        header.addAll(outputLabels);
        table.add(header.toArray(new String[0]));
        
        // 4. Save Current State
        Map<Input, Integer> savedState = new HashMap<>();
        for (Input input : circuitInputs) {
            savedState.put(input, input.getValue());
        }
        
        // 5. Run Simulation
        int numInputs = circuitInputs.size();
        int numRows = 1 << numInputs; // 2^n
        
        // Limit max rows to prevent hanging on large circuits (e.g., max 10 inputs = 1024 rows)
        if (numInputs > 10) {
            numRows = 1024; 
        }
        
        for (int i = 0; i < numRows; i++) {
            List<String> row = new ArrayList<>();
            
            // Set Inputs
            for (int j = 0; j < numInputs; j++) {
                // Use MSB first for standard truth table order
                int bit = (i >> (numInputs - 1 - j)) & 1;
                circuitInputs.get(j).setValue(bit);
                row.add(String.valueOf(bit));
            }
            
            // Calculate Circuit
            calculateCircuit();
            
            // Read Outputs
            for (Object outputComponent : circuitOutputs) {
                if (outputComponent instanceof Gate) {
                    Integer val = ((Gate) outputComponent).getOutput();
                    row.add(val != null ? val.toString() : "0");
                } else if (outputComponent instanceof LED) {
                    row.add(((LED) outputComponent).isOn() ? "1" : "0");
                }
            }
            
            table.add(row.toArray(new String[0]));
        }
        
        // 6. Restore State
        for (Map.Entry<Input, Integer> entry : savedState.entrySet()) {
            entry.getKey().setValue(entry.getValue());
        }
        calculateCircuit(); // Recalculate to restore original state
        
        return table;
    }
    
   
    public void clearCircuit() {
        currentCircuit = new Circuit();
        allCircuits.clear();
        componentIdCounter = 0;
        connectorIdCounter = 0;
        uiComponentMap.clear();
    }

    public void registerUIComponent(int componentId, Object uiComponent) {
        uiComponentMap.put(componentId, uiComponent);
    }
    
  
    public Object getUIComponent(int componentId) {
        return uiComponentMap.get(componentId);
    }
    
  
    public List<Gate> getAllGates() {
        return new ArrayList<>(currentCircuit.getGates());
    }
    
 
    public List<LED> getAllLEDs() {
        return new ArrayList<>(currentCircuit.getLeds());
    }
    
   
    public List<Connector> getAllConnectors() {
        return new ArrayList<>(currentCircuit.getConnectors());
    }
    
  
    public int getComponentCount() {
        return currentCircuit.getComponentCount();
    }
    

    public String getGateType(int gateId) {
        Gate gate = findGate(gateId);
        return gate != null ? gate.getGateType() : null;
    }
 
    public Integer getComponentOutput(int componentId) {
        Gate gate = findGate(componentId);
        if (gate != null) {
            return gate.getOutput();
        }
        LED led = findLED(componentId);
        return led != null && led.isOn() ? 1 : 0;
    }
    
   
    public int getComponentPositionX(int componentId) {
        Gate gate = findGate(componentId);
        if (gate != null) return gate.getPositionX();
        LED led = findLED(componentId);
        return led != null ? led.getPositionX() : 0;
    }
    
   
    public int getComponentPositionY(int componentId) {
        Gate gate = findGate(componentId);
        if (gate != null) return gate.getPositionY();
        LED led = findLED(componentId);
        return led != null ? led.getPositionY() : 0;
    }
    
 
    public int getComponentRow(int componentId) {
        Gate gate = findGate(componentId);
        if (gate != null) return gate.getRow();
        LED led = findLED(componentId);
        return led != null ? led.getRow() : 0;
    }
    
 
    public int getComponentColumn(int componentId) {
        Gate gate = findGate(componentId);
        if (gate != null) return gate.getColumn();
        LED led = findLED(componentId);
        return led != null ? led.getColumn() : 0;
    }
    
  
    public void setComponentRowColumn(int componentId, int row, int column) {
        Gate gate = findGate(componentId);
        if (gate != null) {
            gate.setRowColumn(row, column);
            return;
        }
        LED led = findLED(componentId);
        if (led != null) {
            led.setRowColumn(row, column);
        }
    }
    
 
    public Integer getInputValue(int componentId, int inputIndex) {
        Gate gate = findGate(componentId);
        if (gate != null) {
            Input input = (inputIndex == 0) ? gate.getInput1() : gate.getInput2();
            return input != null ? input.getValue() : null;
        }
        LED led = findLED(componentId);
        if (led != null && inputIndex == 0) {
            return led.getInput().getValue();
        }
        return null;
    }
    
 
    public boolean isInputConnected(int componentId, int inputIndex) {
        Gate gate = findGate(componentId);
        if (gate != null) {
            Input input = (inputIndex == 0) ? gate.getInput1() : gate.getInput2();
            return input != null && input.isConnected();
        }
        LED led = findLED(componentId);
        if (led != null && inputIndex == 0) {
            return led.getInput().isConnected();
        }
        return false;
    }
    
   
    public boolean isLEDOn(int ledId) {
        LED led = findLED(ledId);
        return led != null && led.isOn();
    }
    
    // Get LED component ID
    public int getLEDComponentId(LED led) {
        return led != null ? led.getComponentId() : -1;
    }
    
    // Get LED state (on/off)
    public boolean getLEDState(int ledId) {
        LED led = findLED(ledId);
        return led != null && led.isOn();
    }
    
    /**
     * Get all circuits in the current project
     * @return List of all circuits
     */
    public List<Circuit> getAllCircuits() {
        return new ArrayList<>(allCircuits);
    }
    
    private int generateComponentId() {
        return ++componentIdCounter;
    }
    
    private int generateConnectorId() {
        return ++connectorIdCounter;
    }
    
    private int generateCircuitId() {
        return ++circuitIdCounter;
    }
    
    /**
     * Inner class to hold cloned circuit components data
     */
    public static class ClonedComponents {
        public List<Gate> gates;
        public List<LED> leds;
        public List<Connector> connectors;
        public Map<Integer, Integer> idMapping; // old ID -> new ID
        
        public ClonedComponents() {
            gates = new ArrayList<>();
            leds = new ArrayList<>();
            connectors = new ArrayList<>();
            idMapping = new HashMap<>();
        }
    }
    
    /**
     * Switches to the specified circuit as the current circuit.
     * Saves the current state before switching.
     * 
     * @param circuit The circuit to switch to
     */
    public void switchToCircuit(Circuit circuit) {
        if (circuit != null) {
            this.currentCircuit = circuit;
            // Note: componentIdCounter keeps incrementing globally to avoid conflicts
        }
    }
    
    /**
     * Clones all components from a source circuit with position offset.
     * Generates new unique IDs for all components and remaps connector references.
     * 
     * @param sourceCircuit The circuit to clone from
     * @param offsetX X-axis offset for all components
     * @param offsetY Y-axis offset for all components
     * @return ClonedComponents containing the cloned gates, LEDs, connectors, and ID mapping
     */
    public ClonedComponents cloneCircuitComponents(Circuit sourceCircuit, int offsetX, int offsetY) {
        ClonedComponents cloned = new ClonedComponents();
        
        System.out.println("=== Cloning Circuit Components ===");
        System.out.println("Offset to apply: (" + offsetX + ", " + offsetY + ")");
        
        // Clone gates with new IDs
        for (Gate sourceGate : sourceCircuit.getGates()) {
            int newId = generateComponentId();
            cloned.idMapping.put(sourceGate.getComponentId(), newId);
            
            Gate clonedGate = null;
            String gateType = sourceGate.getGateType();
            
            System.out.println("Cloning " + gateType + " gate from (" + sourceGate.getPositionX() + ", " + sourceGate.getPositionY() + ")");
            
            if (gateType.equals("AND")) {
                clonedGate = new And((And)sourceGate, newId, offsetX, offsetY);
            } else if (gateType.equals("OR")) {
                clonedGate = new Or((Or)sourceGate, newId, offsetX, offsetY);
            } else if (gateType.equals("NOT")) {
                clonedGate = new Not((Not)sourceGate, newId, offsetX, offsetY);
            }
            
            if (clonedGate != null) {
                System.out.println("  -> Cloned to (" + clonedGate.getPositionX() + ", " + clonedGate.getPositionY() + ")");
                cloned.gates.add(clonedGate);
            }
        }
        
        // Clone LEDs with new IDs
        for (LED sourceLED : sourceCircuit.getLeds()) {
            int newId = generateComponentId();
            cloned.idMapping.put(sourceLED.getComponentId(), newId);
            
            LED clonedLED = new LED(sourceLED, newId, offsetX, offsetY);
            cloned.leds.add(clonedLED);
        }
        
        // Clone connectors with remapped IDs
        for (Connector sourceConnector : sourceCircuit.getConnectors()) {
            int newConnectorId = generateConnectorId();
            int newSourceId = cloned.idMapping.get(sourceConnector.getSourceComponentId());
            int newTargetId = cloned.idMapping.get(sourceConnector.getTargetComponentId());
            
            Connector clonedConnector = new Connector(sourceConnector, newConnectorId, newSourceId, newTargetId);
            cloned.connectors.add(clonedConnector);
        }
        
        return cloned;
    }
    
    /**
     * Merges cloned components into the current circuit.
     * 
     * @param gates List of gates to merge
     * @param leds List of LEDs to merge
     * @param connectors List of connectors to merge
     */
    public void mergeComponentsIntoCurrentCircuit(List<Gate> gates, List<LED> leds, List<Connector> connectors) {
        // Add all gates
        for (Gate gate : gates) {
            currentCircuit.addGate(gate);
        }
        
        // Add all LEDs
        for (LED led : leds) {
            currentCircuit.addLED(led);
        }
        
        // Add all connectors and establish connections
        for (Connector connector : connectors) {
            currentCircuit.addConnector(connector);
            
            // Update target input connection
            Gate targetGate = currentCircuit.findGateById(connector.getTargetComponentId());
            if (targetGate != null) {
                Input input = (connector.getTargetInputIndex() == 0) ? 
                              targetGate.getInput1() : targetGate.getInput2();
                if (input != null) {
                    input.setSourceComponentId(connector.getSourceComponentId());
                }
            } else {
                LED targetLED = currentCircuit.findLEDById(connector.getTargetComponentId());
                if (targetLED != null) {
                    targetLED.getInput().setSourceComponentId(connector.getSourceComponentId());
                }
            }
        }
    }
  
    public void saveCircuit(Circuit circuit) {
          //krlo yar
    }
    
   
    public Circuit loadCircuit(int circuitId) {
        //krlo yar
        return null;
    }
    
   
    public boolean deleteCircuit(int circuitId) {
        // Prevent deleting the first circuit (Main Circuit)
        if (allCircuits.isEmpty()) return false;
        
        Circuit mainCircuit = allCircuits.get(0);
        if (mainCircuit.getCircuitId() == circuitId) {
            return false; // Cannot delete main circuit
        }
        
        // Find and remove the circuit
        boolean removed = allCircuits.removeIf(c -> c.getCircuitId() == circuitId);
        
        if (removed) {
            // If we deleted the current circuit, switch to the main circuit
            if (currentCircuit.getCircuitId() == circuitId) {
                currentCircuit = allCircuits.get(0);
            }
            return true;
        }
        
        return false;
    }
}
