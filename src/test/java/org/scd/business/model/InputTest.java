package org.scd.business.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class InputTest {

    @Test
    void testInitialState() {
        Input input = new Input(0);
        assertEquals(0, input.getInputIndex());
        assertNull(input.getValue());
        assertFalse(input.isConnected());
    }

    @Test
    void testSetValue() {
        Input input = new Input(0);
        input.setValue(1);
        assertEquals(1, input.getValue());
    }

    @Test
    void testSetSourceComponentId() {
        Input input = new Input(0);
        input.setSourceComponentId(10);
        assertEquals(10, input.getSourceComponentId());
        assertTrue(input.isConnected());
    }
    
    @Test
    void testCopyConstructor() {
        Input original = new Input(0);
        original.setValue(1);
        original.setSourceComponentId(10);
        
        Input copy = new Input(original);
        assertEquals(0, copy.getInputIndex());
        assertEquals(1, copy.getValue());
        assertNull(copy.getSourceComponentId()); // Copy should not have connection
        assertFalse(copy.isConnected());
    }
}
