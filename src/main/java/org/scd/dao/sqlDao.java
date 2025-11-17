package org.scd.dao;

import org.scd.business.model.Project;

import java.sql.Connection;
import java.sql.SQLException;

public class sqlDao implements daoInterface{
    Connection conn;
    /**
     * default constructor
     *
     */
    public sqlDao() throws SQLException {
        this.conn = sqlSetup.getConnection();
    }

    /**
     * saves project in sql database
     * @param project Object of Project model class
     * @return true if operation was successful, false otherwise
     */
    @Override
    public boolean saveProject(Project project) {

    }

    /**
     * loads an existing project from database
     * @param projectId id of project to be laoded
     * @return Project Object with project id given in parameter
     */
    @Override
    public Project loadProject(int projectId) {

    }
}
