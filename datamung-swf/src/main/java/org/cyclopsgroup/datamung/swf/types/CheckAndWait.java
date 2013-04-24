package org.cyclopsgroup.datamung.swf.types;

import org.cyclopsgroup.datamung.api.types.Identity;

public class CheckAndWait
{
    public static enum Type
    {
        SNAPSHOT_CREATION, INSTANCE_CREATION;
    }

    private Type checkType;

    private long expireOn;

    private Identity identity;

    private int maxChecksPerExecution = 10;

    private String objectName;

    private long waitIntervalSeconds = 300L;

    public Type getCheckType()
    {
        return checkType;
    }

    public long getExpireOn()
    {
        return expireOn;
    }

    public Identity getIdentity()
    {
        return identity;
    }

    public int getMaxChecksPerExecution()
    {
        return maxChecksPerExecution;
    }

    public String getObjectName()
    {
        return objectName;
    }

    public long getWaitIntervalSeconds()
    {
        return waitIntervalSeconds;
    }

    public void setCheckType( Type requestType )
    {
        this.checkType = requestType;
    }

    public void setExpireOn( long expire )
    {
        this.expireOn = expire;
    }

    public void setIdentity( Identity identity )
    {
        this.identity = identity;
    }

    public void setMaxChecksPerExecution( int maxChecksPerExecution )
    {
        this.maxChecksPerExecution = maxChecksPerExecution;
    }

    public void setObjectName( String objectName )
    {
        this.objectName = objectName;
    }

    public void setWaitIntervalSeconds( long waitIntervalSeconds )
    {
        this.waitIntervalSeconds = waitIntervalSeconds;
    }
}
