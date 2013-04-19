package org.cyclopsgroup.datamung.api.types;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement( name = "ExportHandler" )
public class ExportHandler
{
    private String exportId;

    private String workflowId;

    private String workflowRunId;

    @XmlElement
    public String getExportId()
    {
        return exportId;
    }

    @XmlElement
    public String getWorkflowId()
    {
        return workflowId;
    }

    @XmlElement
    public String getWorkflowRunId()
    {
        return workflowRunId;
    }

    public void setExportId( String exportId )
    {
        this.exportId = exportId;
    }

    public void setWorkflowId( String workflowId )
    {
        this.workflowId = workflowId;
    }

    public void setWorkflowRunId( String workflowRunId )
    {
        this.workflowRunId = workflowRunId;
    }
}
