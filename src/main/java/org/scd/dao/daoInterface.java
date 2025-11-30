package org.scd.dao;

import org.scd.business.model.Project;

import javax.sound.sampled.Port;

public interface daoInterface {
    public boolean saveProject(Project project);
    public Project loadProject(int projectId);
    public java.util.Map<Integer, String> getProjectList();
}
