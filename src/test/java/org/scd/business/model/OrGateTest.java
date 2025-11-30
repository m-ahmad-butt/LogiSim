package org.scd.business.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class OrGateTest {

    private Or orGate;

    @BeforeEach
    void setUp() {
        orGate = new Or(1, 0, 0);
    }

    @Test
    void testInitialState() {
        assertNotNull(orGate);
        assertEquals("OR", orGate.getGateType());
        assertNull(orGate.getOutput());
    }

    @Test
    void testCalculate_0_0() {
        orGate.getInput1().setValue(0);
        orGate.getInput2().setValue(0);
        orGate.calculate();
        assertEquals(0, orGate.getOutput());
    }

    @Test
    void testCalculate_0_1() {
        orGate.getInput1().setValue(0);
        orGate.getInput2().setValue(1);
        orGate.calculate();
        assertEquals(1, orGate.getOutput());
    }

    @Test
    void testCalculate_1_0() {
        orGate.getInput1().setValue(1);
        orGate.getInput2().setValue(0);
        orGate.calculate();
        assertEquals(1, orGate.getOutput());
    }

    @Test
    void testCalculate_1_1() {
        orGate.getInput1().setValue(1);
        orGate.getInput2().setValue(1);
        orGate.calculate();
        assertEquals(1, orGate.getOutput());
    }

    @Test
    void testCalculate_NullInput() {
        orGate.getInput1().setValue(1);
        orGate.getInput2().setValue(null);
        orGate.calculate();
        assertNull(orGate.getOutput());
    }
}
