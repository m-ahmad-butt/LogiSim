package org.scd.business.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.scd.business.model.Project;
import org.scd.dao.daoInterface;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ProjectServiceTest {

    private ProjectService service;
    private StubDao stubDao;

    // Stub implementation of daoInterface for testing
    private static class StubDao implements daoInterface {
        private Map<Integer, Project> projects = new HashMap<>();
        private int nextId = 1;

        @Override
        public boolean saveProject(Project project) {
            if (project.getProjectId() == 0) {
                project.setProjectId(nextId++);
            }
            projects.put(project.getProjectId(), project);
            return true;
        }

        @Override
        public Project loadProject(int projectId) {
            return projects.get(projectId);
        }

        @Override
        public Map<Integer, String> getProjectList() {
            Map<Integer, String> list = new HashMap<>();
            for (Map.Entry<Integer, Project> entry : projects.entrySet()) {
                list.put(entry.getKey(), entry.getValue().getProject_Name());
            }
            return list;
        }
    }

    @BeforeEach
    void setUp() {
        stubDao = new StubDao();
        service = new ProjectService(stubDao);
    }

    @Test
    void testSaveProject() {
        Project project = new Project();
        project.setProject_Name("Test Project");
        
        boolean result = service.saveProject(project);
        
        assertTrue(result);
        assertNotEquals(0, project.getProjectId());
    }

    @Test
    void testLoadProject() {
        Project project = new Project();
        project.setProject_Name("Test Project");
        service.saveProject(project);
        
        Project loaded = service.loadProject(project.getProjectId());
        
        assertNotNull(loaded);
        assertEquals("Test Project", loaded.getProject_Name());
    }

    @Test
    void testGetProjectList() {
        Project p1 = new Project();
        p1.setProject_Name("P1");
        service.saveProject(p1);
        
        Project p2 = new Project();
        p2.setProject_Name("P2");
        service.saveProject(p2);
        
        Map<Integer, String> list = service.getProjectList();
        
        assertEquals(2, list.size());
        assertTrue(list.containsValue("P1"));
        assertTrue(list.containsValue("P2"));
    }
}
