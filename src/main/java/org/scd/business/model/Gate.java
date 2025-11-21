package org.scd.business.model;

import java.io.Serializable;


public abstract class Gate implements Component, Serializable {
    private static final long serialVersionUID = 1L;
    
    protected int componentId;
    protected String gateType;
    protected int numInputs;
    protected int positionX;
    protected int positionY;
    protected int row;
    protected int column;
    
    protected Input input1;
    protected Input input2; // null for single-input gates like NOT
    protected Integer output;
    
    public Gate(int componentId, String gateType, int numInputs, int positionX, int positionY) {
        this.componentId = componentId;
        this.gateType = gateType;
        this.numInputs = numInputs;
        this.positionX = positionX;
        this.positionY = positionY;
        
        this.input1 = new Input(0);
        if (numInputs > 1) {
            this.input2 = new Input(1);
        }
    }
    
    @Override
    public int getComponentId() {
        return componentId;
    }
    
    @Override
    public String getComponentType() {
        return gateType;
    }
    
    @Override
    public int getPositionX() {
        return positionX;
    }
    
    @Override
    public int getPositionY() {
        return positionY;
    }
    
    @Override
    public int getRow() {
        return row;
    }
    
    @Override
    public int getColumn() {
        return column;
    }
    
    @Override
    public void setRowColumn(int row, int column) {
        this.row = row;
        this.column = column;
    }
    
    public void setPositionX(int positionX) {
        this.positionX = positionX;
    }
    
    public void setPositionY(int positionY) {
        this.positionY = positionY;
    }
    
    public String getGateType() {
        return gateType;
    }
    
    public int getNumInputs() {
        return numInputs;
    }
    
    public Input getInput1() {
        return input1;
    }
    
    public Input getInput2() {
        return input2;
    }
    
    public Integer getOutput() {
        return output;
    }
    
    public void setOutput(Integer output) {
        this.output = output;
    }
    
    @Override
    public abstract void calculate();
}
