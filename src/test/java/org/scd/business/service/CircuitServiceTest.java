package org.scd.business.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.scd.business.model.Gate;
import org.scd.business.model.Connector;
import static org.junit.jupiter.api.Assertions.*;

public class CircuitServiceTest {

    private CircuitService service;

    @BeforeEach
    void setUp() {
        service = CircuitService.getInstance();
        service.clearCircuit();
        service.createNewCircuit("Test Circuit");
    }

    @Test
    void testAddGate() {
        Gate gate = service.addGate("AND", 10, 10);
        assertNotNull(gate);
        assertEquals("AND", gate.getGateType());
        assertEquals(1, service.getComponentCount());
    }

    @Test
    void testAddConnector() {
        Gate gate1 = service.addGate("AND", 10, 10);
        Gate gate2 = service.addGate("OR", 100, 10);
        
        Connector connector = service.addConnector(gate1.getComponentId(), gate2.getComponentId(), 0, "#000000");
        
        assertNotNull(connector);
        assertEquals(1, service.getAllConnectors().size());
    }

    @Test
    void testSignalPropagation() {
        // Create AND -> NOT circuit
        Gate andGate = service.addGate("AND", 10, 10);
        Gate notGate = service.addGate("NOT", 100, 10);
        
        // Connect AND output to NOT input
        service.addConnector(andGate.getComponentId(), notGate.getComponentId(), 0, "#000000");
        
        // Set inputs for AND gate (1, 1) -> Output 1
        service.setGateInput(andGate.getComponentId(), 0, 1, null);
        service.setGateInput(andGate.getComponentId(), 1, 1, null);
        
        // Calculate
        service.calculateCircuit();
        
        // Check AND output
        assertEquals(1, andGate.getOutput());
        
        // Check NOT output (Input 1 -> Output 0)
        assertEquals(0, notGate.getOutput());
    }

    @Test
    void testDeleteGate() {
        Gate gate = service.addGate("AND", 10, 10);
        assertEquals(1, service.getComponentCount());
        
        service.removeGate(gate.getComponentId());
        assertEquals(0, service.getComponentCount());
    }
}
