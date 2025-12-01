package org.scd.business.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SwitchTest {

    private Switch switchComponent;

    @BeforeEach
    void setUp() {
        switchComponent = new Switch(1, 100, 200);
    }

    @Test
    void testInitialState() {
        assertNotNull(switchComponent);
        assertEquals(1, switchComponent.getComponentId());
        assertEquals("Switch", switchComponent.getComponentType());
        assertEquals(100, switchComponent.getPositionX());
        assertEquals(200, switchComponent.getPositionY());
        assertFalse(switchComponent.isOn());
        assertEquals(0, switchComponent.getOutput());
    }

    @Test
    void testToggle() {
        // Initially off
        assertFalse(switchComponent.isOn());
        assertEquals(0, switchComponent.getOutput());
        
        // Toggle to on
        switchComponent.toggle();
        assertTrue(switchComponent.isOn());
        assertEquals(1, switchComponent.getOutput());
        
        // Toggle back to off
        switchComponent.toggle();
        assertFalse(switchComponent.isOn());
        assertEquals(0, switchComponent.getOutput());
    }

    @Test
    void testSetOn() {
        switchComponent.setOn(true);
        assertTrue(switchComponent.isOn());
        assertEquals(1, switchComponent.getOutput());
        
        switchComponent.setOn(false);
        assertFalse(switchComponent.isOn());
        assertEquals(0, switchComponent.getOutput());
    }

    @Test
    void testPositionSetters() {
        switchComponent.setPositionX(300);
        switchComponent.setPositionY(400);
        
        assertEquals(300, switchComponent.getPositionX());
        assertEquals(400, switchComponent.getPositionY());
    }

    @Test
    void testRowColumn() {
        switchComponent.setRowColumn(2, 3);
        
        assertEquals(2, switchComponent.getRow());
        assertEquals(3, switchComponent.getColumn());
    }

    @Test
    void testCalculate() {
        // Calculate does nothing for switches, just ensure it doesn't throw
        assertDoesNotThrow(() -> switchComponent.calculate());
    }

    @Test
    void testCopyConstructor() {
        switchComponent.setOn(true);
        switchComponent.setRowColumn(1, 2);
        
        Switch clonedSwitch = new Switch(switchComponent, 2, 50, 75);
        
        assertEquals(2, clonedSwitch.getComponentId());
        assertEquals(150, clonedSwitch.getPositionX()); // 100 + 50
        assertEquals(275, clonedSwitch.getPositionY()); // 200 + 75
        assertTrue(clonedSwitch.isOn());
        assertEquals(1, clonedSwitch.getOutput());
        assertEquals(1, clonedSwitch.getRow());
        assertEquals(2, clonedSwitch.getColumn());
    }

    @Test
    void testCopyConstructorWithNegativeOffset() {
        Switch clonedSwitch = new Switch(switchComponent, 3, -20, -30);
        
        assertEquals(3, clonedSwitch.getComponentId());
        assertEquals(80, clonedSwitch.getPositionX()); // 100 - 20
        assertEquals(170, clonedSwitch.getPositionY()); // 200 - 30
        assertFalse(clonedSwitch.isOn());
    }

    @Test
    void testOutputWhenOff() {
        switchComponent.setOn(false);
        assertEquals(0, switchComponent.getOutput());
    }

    @Test
    void testOutputWhenOn() {
        switchComponent.setOn(true);
        assertEquals(1, switchComponent.getOutput());
    }
}
