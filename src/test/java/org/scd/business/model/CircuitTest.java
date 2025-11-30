package org.scd.business.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CircuitTest {

    private Circuit circuit;

    @BeforeEach
    void setUp() {
        circuit = new Circuit(1, "Test Circuit");
    }

    @Test
    void testInitialState() {
        assertEquals(1, circuit.getCircuitId());
        assertEquals("Test Circuit", circuit.getCircuitName());
        assertEquals(0, circuit.getComponentCount());
        assertTrue(circuit.getGates().isEmpty());
        assertTrue(circuit.getLeds().isEmpty());
        assertTrue(circuit.getConnectors().isEmpty());
    }

    @Test
    void testAddGate() {
        Gate gate = new And(1, 0, 0);
        circuit.addGate(gate);
        assertEquals(1, circuit.getComponentCount());
        assertEquals(gate, circuit.findGateById(1));
    }

    @Test
    void testAddLED() {
        LED led = new LED(1, 0, 0);
        circuit.addLED(led);
        assertEquals(1, circuit.getComponentCount());
        assertEquals(led, circuit.findLEDById(1));
    }

    @Test
    void testAddConnector() {
        Connector connector = new Connector(1, 1, 2, 0, "#000");
        circuit.addConnector(connector);
        assertEquals(1, circuit.getConnectors().size());
    }

    @Test
    void testRemoveGate() {
        Gate gate = new And(1, 0, 0);
        circuit.addGate(gate);
        circuit.removeGate(1);
        assertEquals(0, circuit.getComponentCount());
        assertNull(circuit.findGateById(1));
    }

    @Test
    void testRemoveLED() {
        LED led = new LED(1, 0, 0);
        circuit.addLED(led);
        circuit.removeLED(1);
        assertEquals(0, circuit.getComponentCount());
        assertNull(circuit.findLEDById(1));
    }
}
