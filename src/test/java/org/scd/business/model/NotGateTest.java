package org.scd.business.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class NotGateTest {

    private Not notGate;

    @BeforeEach
    void setUp() {
        notGate = new Not(1, 0, 0);
    }

    @Test
    void testInitialState() {
        assertNotNull(notGate);
        assertEquals("NOT", notGate.getGateType());
        assertNull(notGate.getOutput());
    }

    @Test
    void testCalculate_0() {
        notGate.getInput1().setValue(0);
        notGate.calculate();
        assertEquals(1, notGate.getOutput());
    }

    @Test
    void testCalculate_1() {
        notGate.getInput1().setValue(1);
        notGate.calculate();
        assertEquals(0, notGate.getOutput());
    }

    @Test
    void testCalculate_NullInput() {
        notGate.getInput1().setValue(null);
        notGate.calculate();
        assertNull(notGate.getOutput());
    }
}
