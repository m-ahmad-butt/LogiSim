package org.scd.business.model;

import java.io.Serializable;


public class LED implements Component, Serializable {
    private static final long serialVersionUID = 1L;
    
    private int componentId;
    private int positionX;
    private int positionY;
    private int row;
    private int column;
    private Input input;
    private boolean isOn;
    
    public LED(int componentId, int positionX, int positionY) {
        this.componentId = componentId;
        this.positionX = positionX;
        this.positionY = positionY;
        this.input = new Input(0);
        this.isOn = false;
    }
    
    /**
     * Copy constructor for cloning an LED with offset position.
     * 
     * @param source The LED to copy from
     * @param newId The new component ID
     * @param offsetX X-axis offset for position
     * @param offsetY Y-axis offset for position
     */
    public LED(LED source, int newId, int offsetX, int offsetY) {
        this.componentId = newId;
        this.positionX = source.positionX + offsetX;
        this.positionY = source.positionY + offsetY;
        this.input = new Input(source.input);
        this.isOn = false; // Will be recalculated
        this.row = source.row;
        this.column = source.column;
    }
    
    @Override
    public int getComponentId() {
        return componentId;
    }
    
    @Override
    public String getComponentType() {
        return "LED";
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
        Integer inputValue = input.getValue();
        if (inputValue != null) {
            isOn = (inputValue == 1);
        } else {
            isOn = false;
        }
    }
    
    public void setPositionX(int positionX) {
        this.positionX = positionX;
    }
    
    public void setPositionY(int positionY) {
        this.positionY = positionY;
    }
    
    public Input getInput() {
        return input;
    }
    
    public void setInput(Input input) {
        this.input = input;
    }
    
    public boolean isOn() {
        return isOn;
    }
    
    public void setOn(boolean on) {
        isOn = on;
    }
}
