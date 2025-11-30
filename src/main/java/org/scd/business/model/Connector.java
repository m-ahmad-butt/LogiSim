package org.scd.business.model;

import java.io.Serializable;


public class Connector implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int connectorId;
    private int sourceComponentId;
    private int targetComponentId;
    private int targetInputIndex; // 0 for input1, 1 for input2
    private String wireColor; 
    
    public Connector() {}

    public Connector(int connectorId, int sourceComponentId, int targetComponentId, 
                     int targetInputIndex, String wireColor) {
        this.connectorId = connectorId;
        this.sourceComponentId = sourceComponentId;
        this.targetComponentId = targetComponentId;
        this.targetInputIndex = targetInputIndex;
        this.wireColor = wireColor;
    }
    
    /**
     * Copy constructor for cloning a connector with remapped component IDs.
     * 
     * @param source The connector to copy from
     * @param newId The new connector ID
     * @param newSourceId The new source component ID
     * @param newTargetId The new target component ID
     */
    public Connector(Connector source, int newId, int newSourceId, int newTargetId) {
        this.connectorId = newId;
        this.sourceComponentId = newSourceId;
        this.targetComponentId = newTargetId;
        this.targetInputIndex = source.targetInputIndex;
        this.wireColor = source.wireColor;
    }
    
    // Getters and setters
    public int getConnectorId() {
        return connectorId;
    }
    
    public void setConnectorId(int connectorId) {
        this.connectorId = connectorId;
    }
    
    public int getSourceComponentId() {
        return sourceComponentId;
    }
    
    public void setSourceComponentId(int sourceComponentId) {
        this.sourceComponentId = sourceComponentId;
    }
    
    public int getTargetComponentId() {
        return targetComponentId;
    }
    
    public void setTargetComponentId(int targetComponentId) {
        this.targetComponentId = targetComponentId;
    }
    
    public int getTargetInputIndex() {
        return targetInputIndex;
    }
    
    public void setTargetInputIndex(int targetInputIndex) {
        this.targetInputIndex = targetInputIndex;
    }
    
    public String getWireColor() {
        return wireColor;
    }
    
    public void setWireColor(String wireColor) {
        this.wireColor = wireColor;
    }
}
