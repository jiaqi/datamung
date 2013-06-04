package org.cyclopsgroup.datamung.swf.types;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement( name = "InstanceDescription" )
public class InstanceDescription
{
    private int allocatedStorage;

    private String availabilityZone;

    private String instanceId;

    private String instanceStatus;

    private String instanceType;

    private String masterUser;

    private int port;

    private String publicHostName;

    @XmlElement
    public int getAllocatedStorage()
    {
        return allocatedStorage;
    }

    @XmlElement
    public String getAvailabilityZone()
    {
        return availabilityZone;
    }

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
    public String getInstanceType()
    {
        return instanceType;
    }

    @XmlElement
    public String getMasterUser()
    {
        return masterUser;
    }

    @XmlElement
    public int getPort()
    {
        return port;
    }

    @XmlElement
    public String getPublicHostName()
    {
        return publicHostName;
    }

    public void setAllocatedStorage( int allocatedStorage )
    {
        this.allocatedStorage = allocatedStorage;
    }

    public void setAvailabilityZone( String availabilityZone )
    {
        this.availabilityZone = availabilityZone;
    }

    public void setInstanceId( String instanceId )
    {
        this.instanceId = instanceId;
    }

    public void setInstanceStatus( String instanceStatus )
    {
        this.instanceStatus = instanceStatus;
    }

    public void setInstanceType( String instanceType )
    {
        this.instanceType = instanceType;
    }

    public void setMasterUser( String masterUser )
    {
        this.masterUser = masterUser;
    }

    public void setPort( int port )
    {
        this.port = port;
    }

    public void setPublicHostName( String publicHostName )
    {
        this.publicHostName = publicHostName;
    }
}
