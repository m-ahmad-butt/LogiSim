package org.scd.business.service;

import org.scd.business.model.Project;
import org.scd.dao.daoInterface;
import org.scd.dao.sqlDao;

import java.sql.SQLException;
import java.util.Map;

public class ProjectService {
    private daoInterface dao;

    public ProjectService() {
        try {
            this.dao = new sqlDao();
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle connection error
        }
    }

    public boolean saveProject(Project project) {
        if (dao != null) {
            return dao.saveProject(project);
        }
        return false;
    }

    public Project loadProject(int projectId) {
        if (dao != null) {
            return dao.loadProject(projectId);
        }
        return null;
    }

    public Map<Integer, String> getProjectList() {
        if (dao != null) {
            return dao.getProjectList();
        }
        return null;
    }
}
