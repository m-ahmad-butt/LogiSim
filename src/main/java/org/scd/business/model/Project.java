package org.scd.business.model;
import java.util.List;

public class Project {
    private int projectId;
    private String Project_Name;
    List<Circuit> circuits;

    public void setCircuits(List<Circuit> circuits) {
        this.circuits = circuits;
    }

    public void setProject_Name(String project_Name) {
        Project_Name = project_Name;
    }
    
    public int getProjectId() {
        return projectId;
    }
    
    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public String getProject_Name(){
        return Project_Name;
    }

    public List<Circuit> getCircuits() { return circuits;}
}
