package org.scd.business.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AndGateTest {

    private And andGate;

    @BeforeEach
    void setUp() {
        andGate = new And(1, 0, 0);
    }

    @Test
    void testInitialState() {
        assertNotNull(andGate);
        assertEquals("AND", andGate.getGateType());
        assertNull(andGate.getOutput());
    }

    @Test
    void testCalculate_0_0() {
        andGate.getInput1().setValue(0);
        andGate.getInput2().setValue(0);
        andGate.calculate();
        assertEquals(0, andGate.getOutput());
    }

    @Test
    void testCalculate_0_1() {
        andGate.getInput1().setValue(0);
        andGate.getInput2().setValue(1);
        andGate.calculate();
        assertEquals(0, andGate.getOutput());
    }

    @Test
    void testCalculate_1_0() {
        andGate.getInput1().setValue(1);
        andGate.getInput2().setValue(0);
        andGate.calculate();
        assertEquals(0, andGate.getOutput());
    }

    @Test
    void testCalculate_1_1() {
        andGate.getInput1().setValue(1);
        andGate.getInput2().setValue(1);
        andGate.calculate();
        assertEquals(1, andGate.getOutput());
    }

    @Test
    void testCalculate_NullInput() {
        andGate.getInput1().setValue(1);
        andGate.getInput2().setValue(null);
        andGate.calculate();
        assertNull(andGate.getOutput());
    }
}
