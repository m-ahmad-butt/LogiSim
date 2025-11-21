package org.scd.ui;


public class ComponentInput {
    private Integer value; // null means not set, 0 or 1 when set
    private GateComponent sourceComponent; // null if direct input, or reference to source gate
    private int inputOrder; // 0 for first input, 1 for second input
    
    public ComponentInput(int inputOrder) {
        this.inputOrder = inputOrder;
        this.value = null;
        this.sourceComponent = null;
    }
    
    public Integer getValue() {
        return value;
    }
    
    public void setValue(Integer value) {
        this.value = value;
    }
    
    public GateComponent getSourceComponent() {
        return sourceComponent;
    }
    
    public void setSourceComponent(GateComponent sourceComponent) {
        this.sourceComponent = sourceComponent;
    }
    
    public int getInputOrder() {
        return inputOrder;
    }
    
    public boolean isConnected() {
        return sourceComponent != null;
    }
    
    public boolean hasDirectValue() {
        return value != null && sourceComponent == null;
    }
}
