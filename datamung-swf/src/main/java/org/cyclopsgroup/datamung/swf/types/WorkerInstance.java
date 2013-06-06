package org.cyclopsgroup.datamung.swf.types;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement( name = "WorkerDescription" )
public class WorkerInstance
{
    private String instanceId;

    private String instanceStatus;

    private String publicIpAddress;

    @XmlElement
    public String getInstanceId()
    {
        return instanceId;
    }

    @XmlElement
    public String getInstanceStatus()
    {
        return instanceStatus;
    }

    @XmlElement
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
}
