package org.cyclopsgroup.datamung.api.types;

import org.cyclopsgroup.kaufman.interfaces.BaseComparableBean;

public class AgentConfig
    extends BaseComparableBean
{
    public static final String ROLE_EXTERNAL_ID =
        AgentConfig.class.getPackage().getName();

    private String controllerRoleArn;

    private String workflowDomain;

    private String workflowTaskList;

    public String getControllerRoleArn()
    {
        return controllerRoleArn;
    }

    public String getWorkflowDomain()
    {
        return workflowDomain;
    }

    public String getWorkflowTaskList()
    {
        return workflowTaskList;
    }

    public void setControllerRoleArn( String controllerRoleArn )
    {
        this.controllerRoleArn = controllerRoleArn;
    }

    public void setWorkflowDomain( String workflowDomain )
    {
        this.workflowDomain = workflowDomain;
    }

    public void setWorkflowTaskList( String workflowTaskList )
    {
        this.workflowTaskList = workflowTaskList;
    }
}
