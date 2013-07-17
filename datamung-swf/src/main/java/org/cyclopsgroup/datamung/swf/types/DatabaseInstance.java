package org.cyclopsgroup.datamung.swf.types;

import org.cyclopsgroup.datamung.api.types.InstanceNetwork;
import org.cyclopsgroup.kaufman.interfaces.BaseComparableBean;

public class DatabaseInstance
    extends BaseComparableBean
{
    private int allocatedStorage;

    private String availabilityZone;

    private String instanceId;

    private String instanceStatus;

    private String instanceType;

    private String masterUser;

    private InstanceNetwork network;

    private int port;

    private String publicHostName;

    public int getAllocatedStorage()
    {
        return allocatedStorage;
    }

    public String getAvailabilityZone()
    {
        return availabilityZone;
    }

    public String getInstanceId()
    {
        return instanceId;
    }

    public String getInstanceStatus()
    {
        return instanceStatus;
    }

    public String getInstanceType()
    {
        return instanceType;
    }

    public String getMasterUser()
    {
        return masterUser;
    }

    public InstanceNetwork getNetwork()
    {
        return network;
    }

    public int getPort()
    {
        return port;
    }

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

    public void setNetwork( InstanceNetwork network )
    {
        this.network = network;
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
