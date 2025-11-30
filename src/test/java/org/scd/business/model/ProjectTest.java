package org.scd.business.model;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class ProjectTest {

    @Test
    void testInitialState() {
        Project project = new Project();
        assertEquals(0, project.getProjectId());
        assertNull(project.getProject_Name());
        assertNull(project.getCircuits());
    }

    @Test
    void testSetters() {
        Project project = new Project();
        project.setProjectId(1);
        project.setProject_Name("Test Project");
        
        List<Circuit> circuits = new ArrayList<>();
        circuits.add(new Circuit(1, "Main"));
        project.setCircuits(circuits);
        
        assertEquals(1, project.getProjectId());
        assertEquals("Test Project", project.getProject_Name());
        assertEquals(1, project.getCircuits().size());
    }
}
