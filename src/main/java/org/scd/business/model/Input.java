package org.scd.business.model;

import java.io.Serializable;


public class Input implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int inputIndex; // 0 for input1, 1 for input2
    private Integer value; // 0, 1, or null (not set)
    private Integer sourceComponentId; // ID of the component providing this input
    
    public Input() {}

    public Input(int inputIndex) {
        this.inputIndex = inputIndex;
        this.value = null;
        this.sourceComponentId = null;
    }
    
    /**
     * Copy constructor for cloning an Input.
     * Creates a copy without source connection (sourceComponentId is null).
     * Connections will be re-established via connectors.
     * 
     * @param source The Input to copy from
     */
    public Input(Input source) {
        this.inputIndex = source.inputIndex;
        this.value = source.value;
        this.sourceComponentId = null; // Don't copy connection, will be set by connectors
    }
    
    public boolean isConnected() {
        return sourceComponentId != null;
    }
    
    // Getters and setters
    public int getInputIndex() {
        return inputIndex;
    }
    
    public void setInputIndex(int inputIndex) {
        this.inputIndex = inputIndex;
    }
    
    public Integer getValue() {
        return value;
    }
    
    public void setValue(Integer value) {
        this.value = value;
    }
    
    public Integer getSourceComponentId() {
        return sourceComponentId;
    }
    
    public void setSourceComponentId(Integer sourceComponentId) {
        this.sourceComponentId = sourceComponentId;
    }
}
