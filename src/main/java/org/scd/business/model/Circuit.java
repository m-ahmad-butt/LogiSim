package org.scd.business.model;

import java.util.List;

public class Circuit {
    private String Circuit_Name;
    List<Gate> gates;
    List<Connector> connectors;

    public List<Gate> getGates() {
        return gates;
    }

    public void setGates(List<Gate> gates) {
        this.gates = gates;
    }

    public List<Connector> getConnectors() {
        return connectors;
    }

    public void setConnectors(List<Connector> connectors) {
        this.connectors = connectors;
    }

    public String getCircuit_Name() {
        return Circuit_Name;
    }

    public void setCircuit_Name(String circuit_Name) {
        Circuit_Name = circuit_Name;
    }
}
