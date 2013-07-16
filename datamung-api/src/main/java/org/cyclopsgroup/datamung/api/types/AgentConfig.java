package org.cyclopsgroup.datamung.api.types;

import org.cyclopsgroup.kaufman.interfaces.BaseComparableBean;

public class AgentConfig
    extends BaseComparableBean
{
    private String workflowTaskList;

    public String getWorkflowTaskList()
    {
        return workflowTaskList;
    }

    public void setWorkflowTaskList( String workflowTaskList )
    {
        this.workflowTaskList = workflowTaskList;
    }
}
