package org.cyclopsgroup.datamung.api.types;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement( name = "WorkflowDetail" )
public class WorkflowDetail
{
    private Workflow workflow;

    @XmlElement
    public Workflow getWorkflow()
    {
        return workflow;
    }

    public void setWorkflow( Workflow workflow )
    {
        this.workflow = workflow;
    }
}
