package org.scd.business.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ConnectorTest {

    @Test
    void testInitialState() {
        Connector connector = new Connector(1, 10, 20, 0, "#FF0000");
        assertEquals(1, connector.getConnectorId());
        assertEquals(10, connector.getSourceComponentId());
        assertEquals(20, connector.getTargetComponentId());
        assertEquals(0, connector.getTargetInputIndex());
        assertEquals("#FF0000", connector.getWireColor());
    }

    @Test
    void testSetters() {
        Connector connector = new Connector(1, 10, 20, 0, "#FF0000");
        
        connector.setSourceComponentId(30);
        assertEquals(30, connector.getSourceComponentId());
        
        connector.setTargetComponentId(40);
        assertEquals(40, connector.getTargetComponentId());
        
        connector.setTargetInputIndex(1);
        assertEquals(1, connector.getTargetInputIndex());
        
        connector.setWireColor("#00FF00");
        assertEquals("#00FF00", connector.getWireColor());
    }
}
