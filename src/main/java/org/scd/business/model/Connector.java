package org.scd.business.model;

public class Connector {
    public String Connector_Color;
    Gate Sink_Gate;
    Gate Source_Gate;
    Float Position_X;
    Float Position_Y;


    void process(){
        //TODo
    }

    public String getConnector_Color() {
        return Connector_Color;
    }

    public void setConnector_Color(String connector_Color) {
        Connector_Color = connector_Color;
    }

    public Gate getSink_Gate() {
        return Sink_Gate;
    }

    public void setSink_Gate(Gate sink_Gate) {
        Sink_Gate = sink_Gate;
    }

    public Gate getSource_Gate() {
        return Source_Gate;
    }

    public void setSource_Gate(Gate source_Gate) {
        Source_Gate = source_Gate;
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
}
