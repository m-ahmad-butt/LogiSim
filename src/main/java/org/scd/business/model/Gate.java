package org.scd.business.model;

import java.util.List;

public abstract class Gate implements Component{
    String Gate_Name;
    Float Position_X;
    Float Position_Y;
    boolean Output;
    // inputs for the component, can be 1 or 2 depending on the gate
    List<Input> inputs;

    public String getGate_Name() {
        return Gate_Name;
    }

    public void setGate_Name(String gate_Name) {
        Gate_Name = gate_Name;
    }

    public Float getPosition_X() {
        return Position_X;
    }

    public void setPosition_X(Float position_X) {
        Position_X = position_X;
    }

    public Float getPosition_Y() {
        return Position_Y;
    }

    public void setPosition_Y(Float position_Y) {
        Position_Y = position_Y;
    }

    public boolean isOutput() {
        return Output;
    }

    public void setOutput(boolean output) {
        Output = output;
    }

    public void setInputs(List<Input> inputs) {
        this.inputs = inputs;
    }

    public List<Input> getInputs() { return inputs;}
}
