package org.scd.business.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class Circuit implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int circuitId;
    private String circuitName;
    private String description;
    private Date createdDate;
    private Date modifiedDate;
    private List<Gate> gates;
    private List<LED> leds;
    private List<Connector> connectors;
    
    public Circuit() {
        this.gates = new ArrayList<>();
        this.leds = new ArrayList<>();
        this.connectors = new ArrayList<>();
        this.createdDate = new Date();
        this.modifiedDate = new Date();
    }
    
    public Circuit(int circuitId, String circuitName) {
        this();
        this.circuitId = circuitId;
        this.circuitName = circuitName;
    }
    
   
    public void addGate(Gate gate) {
        gates.add(gate);
        modifiedDate = new Date();
    }
    
    public void addLED(LED led) {
        leds.add(led);
        modifiedDate = new Date();
    }
    
    public void addConnector(Connector connector) {
        connectors.add(connector);
        modifiedDate = new Date();
    }
    
    public void removeGate(int gateId) {
        gates.removeIf(g -> g.getComponentId() == gateId);
        connectors.removeIf(c -> c.getSourceComponentId() == gateId || 
                                  c.getTargetComponentId() == gateId);
        modifiedDate = new Date();
    }
    
    public void removeLED(int ledId) {
        leds.removeIf(l -> l.getComponentId() == ledId);
        connectors.removeIf(c -> c.getTargetComponentId() == ledId);
        modifiedDate = new Date();
    }
    
    public void removeConnector(int connectorId) {
        connectors.removeIf(c -> c.getConnectorId() == connectorId);
        modifiedDate = new Date();
    }
    
    public Gate findGateById(int gateId) {
        return gates.stream()
                   .filter(g -> g.getComponentId() == gateId)
                   .findFirst()
                   .orElse(null);
    }
    
    public LED findLEDById(int ledId) {
        return leds.stream()
                  .filter(l -> l.getComponentId() == ledId)
                  .findFirst()
                  .orElse(null);
    }
    
    public int getComponentCount() {
        return gates.size() + leds.size();
    }
    
    // Getters and setters
    public int getCircuitId() {
        return circuitId;
    }
    
    public void setCircuitId(int circuitId) {
        this.circuitId = circuitId;
    }
    
    public String getCircuitName() {
        return circuitName;
    }
    
    public void setCircuitName(String circuitName) {
        this.circuitName = circuitName;
        this.modifiedDate = new Date();
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
        this.modifiedDate = new Date();
    }
    
    public Date getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }
    
    public Date getModifiedDate() {
        return modifiedDate;
    }
    
    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }
    
    public List<Gate> getGates() {
        return gates;
    }
    
    public void setGates(List<Gate> gates) {
        this.gates = gates;
    }
    
    public List<LED> getLeds() {
        return leds;
    }
    
    public void setLeds(List<LED> leds) {
        this.leds = leds;
    }
    
    public List<Connector> getConnectors() {
        return connectors;
    }
    
    public void setConnectors(List<Connector> connectors) {
        this.connectors = connectors;
    }
}
