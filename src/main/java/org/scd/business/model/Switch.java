package org.scd.business.model;

import java.io.Serializable;

public class Switch implements Component, Serializable {
    private static final long serialVersionUID = 1L;
    
    private int componentId;
    private int positionX;
    private int positionY;
    private int row;
    private int column;
    private boolean isOn;
    
    public Switch(int componentId, int positionX, int positionY) {
        this.componentId = componentId;
        this.positionX = positionX;
        this.positionY = positionY;
        this.isOn = false;
    }
    
    /**
     * Copy constructor for cloning a Switch with offset position.
     * 
     * @param source The Switch to copy from
     * @param newId The new component ID
     * @param offsetX X-axis offset for position
     * @param offsetY Y-axis offset for position
     */
    public Switch(Switch source, int newId, int offsetX, int offsetY) {
        this.componentId = newId;
        this.positionX = source.positionX + offsetX;
        this.positionY = source.positionY + offsetY;
        this.isOn = source.isOn;
        this.row = source.row;
        this.column = source.column;
    }
    
    @Override
    public int getComponentId() {
        return componentId;
    }
    
    @Override
    public String getComponentType() {
        return "Switch";
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
    
    @Override
    public void calculate() {
        // Switch state is manually toggled, no calculation needed based on inputs
    }
    
    public void setPositionX(int positionX) {
        this.positionX = positionX;
    }
    
    public void setPositionY(int positionY) {
        this.positionY = positionY;
    }
    
    public boolean isOn() {
        return isOn;
    }
    
    public void setOn(boolean on) {
        isOn = on;
    }
    
    public void toggle() {
        isOn = !isOn;
    }
    
    public Integer getOutput() {
        return isOn ? 1 : 0;
    }
}
