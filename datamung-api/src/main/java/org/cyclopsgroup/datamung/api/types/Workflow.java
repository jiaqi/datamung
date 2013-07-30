package org.cyclopsgroup.datamung.api.types;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.cyclopsgroup.kaufman.interfaces.JodaDateTimeAdapter;
import org.joda.time.DateTime;

@XmlType
public class Workflow
{
    private String domainName;

    private String runId;

    private DateTime startDate;

    private DateTime terminateDate;

    private String workflowId;

    private String workflowStatus;

    private String workflowType;

    private String workflowVersion;

    @XmlElement
    public String getDomainName()
    {
        return domainName;
    }

    @XmlElement
    public String getRunId()
    {
        return runId;
    }

    @XmlElement
    @XmlJavaTypeAdapter( JodaDateTimeAdapter.class )
    public DateTime getStartDate()
    {
        return startDate;
    }

    @XmlElement
    @XmlJavaTypeAdapter( JodaDateTimeAdapter.class )
    public DateTime getTerminateDate()
    {
        return terminateDate;
    }

    @XmlElement
    public String getWorkflowId()
    {
        return workflowId;
    }

    @XmlElement
    public String getWorkflowStatus()
    {
        return workflowStatus;
    }

    @XmlElement
    public String getWorkflowType()
    {
        return workflowType;
    }

    @XmlElement
    public String getWorkflowVersion()
    {
        return workflowVersion;
    }

    public void setDomainName( String domainName )
    {
        this.domainName = domainName;
    }

    public void setRunId( String runId )
    {
        this.runId = runId;
    }

    public void setStartDate( DateTime startDate )
    {
        this.startDate = startDate;
    }

    public void setTerminateDate( DateTime terminateDate )
    {
        this.terminateDate = terminateDate;
    }

    public void setWorkflowId( String workflowId )
    {
        this.workflowId = workflowId;
    }

    public void setWorkflowStatus( String workflowStatus )
    {
        this.workflowStatus = workflowStatus;
    }

    public void setWorkflowType( String workflowType )
    {
        this.workflowType = workflowType;
    }

    public void setWorkflowVersion( String workflowVersion )
    {
        this.workflowVersion = workflowVersion;
    }
}
