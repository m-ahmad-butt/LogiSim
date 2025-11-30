package org.scd.business.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LEDTest {

    private LED led;

    @BeforeEach
    void setUp() {
        led = new LED(1, 0, 0);
    }

    @Test
    void testInitialState() {
        assertNotNull(led);
        assertEquals("LED", led.getComponentType());
        assertFalse(led.isOn());
        assertNotNull(led.getInput());
    }

    @Test
    void testCalculate_On() {
        led.getInput().setValue(1);
        led.calculate();
        assertTrue(led.isOn());
    }

    @Test
    void testCalculate_Off() {
        led.getInput().setValue(0);
        led.calculate();
        assertFalse(led.isOn());
    }

    @Test
    void testCalculate_Null() {
        led.getInput().setValue(null);
        led.calculate();
        assertFalse(led.isOn());
    }
}
