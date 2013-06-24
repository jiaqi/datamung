package org.cyclopsgroup.datamung.swf.types;

import org.cyclopsgroup.datamung.api.types.BaseType;

public class WorkerInstance
    extends BaseType
{
    private String instanceId;

    private String instanceStatus;

    private String publicIpAddress;

    public String getInstanceId()
    {
        return instanceId;
    }

    public String getInstanceStatus()
    {
        return instanceStatus;
    }

    public String getPublicIpAddress()
    {
        return publicIpAddress;
    }

    public void setInstanceId( String instanceId )
    {
        this.instanceId = instanceId;
    }

    public void setInstanceStatus( String instanceStatus )
    {
        this.instanceStatus = instanceStatus;
    }

    public void setPublicIpAddress( String publicIpAddress )
    {
        this.publicIpAddress = publicIpAddress;
    }

    public WorkerInstance withInstanceStatus( String status )
    {
        setInstanceStatus( status );
        return this;
    }
}
