package org.scd.business.service;

import org.scd.business.model.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CircuitService {
    private static CircuitService instance;
    private Circuit currentCircuit;
    
    // Component ID management
    private int componentIdCounter = 0;
    private int connectorIdCounter = 0;
    private int circuitIdCounter = 0;
    
    // Maps to track UI components linked to business models
    private Map<Integer, Object> uiComponentMap; // componentId -> UI component
    
    private CircuitService() {
        currentCircuit = new Circuit();
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
        componentIdCounter = 0;
        connectorIdCounter = 0;
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
        
        // Add header
        List<String> header = new ArrayList<>();
        header.add("Component");
        header.add("Type");
        header.add("Input 1");
        header.add("Input 2");
        header.add("Output");
        table.add(header.toArray(new String[0]));
        
        // Add gates
        for (Gate gate : currentCircuit.getGates()) {
            List<String> row = new ArrayList<>();
            row.add(gate.getGateType() + " " + gate.getComponentId());
            row.add(gate.getGateType());
            row.add(gate.getInput1().getValue() != null ? gate.getInput1().getValue().toString() : "-");
            row.add(gate.getInput2() != null && gate.getInput2().getValue() != null ? 
                   gate.getInput2().getValue().toString() : "-");
            row.add(gate.getOutput() != null ? gate.getOutput().toString() : "-");
            table.add(row.toArray(new String[0]));
        }
        
        // Add LEDs
        for (LED led : currentCircuit.getLeds()) {
            List<String> row = new ArrayList<>();
            row.add("LED " + led.getComponentId());
            row.add("LED");
            row.add(led.getInput().getValue() != null ? led.getInput().getValue().toString() : "-");
            row.add("-");
            row.add(led.isOn() ? "ON" : "OFF");
            table.add(row.toArray(new String[0]));
        }
        
        return table;
    }
    
   
    public void clearCircuit() {
        currentCircuit = new Circuit();
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
    
    private int generateComponentId() {
        return ++componentIdCounter;
    }
    
    private int generateConnectorId() {
        return ++connectorIdCounter;
    }
    
    private int generateCircuitId() {
        return ++circuitIdCounter;
    }
    
 
    public void saveCircuit(Circuit circuit) {
          //krlo yar
    }
    
  
    public Circuit loadCircuit(int circuitId) {
        //krlo yar
        return null;
    }
    
   
    public void deleteCircuit(int circuitId) {
         //krlo yar
    }
}
