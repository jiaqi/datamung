package org.cyclopsgroup.datamung.swf.types;

public class InstanceDescription
{
    private int allocatedStorage;

    private String availabilityZone;

    private String instanceId;

    private String instanceStatus;

    private String instanceType;

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
}
