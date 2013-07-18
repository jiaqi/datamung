package org.cyclopsgroup.datamung.swf.types;

import org.cyclopsgroup.datamung.api.types.Identity;
import org.cyclopsgroup.kaufman.interfaces.BaseComparableBean;

public class CheckAndWait
    extends BaseComparableBean
{
    public static enum Type
    {
        DATABASE_CREATION, SNAPSHOT_CREATION, WORKER_LAUNCH;
    }

    private static final int DEFAULT_MAX_CHECKS_PER_EXECUTION = 10;

    private static final long DEFAULT_WAIT_INTERVAL_SECONDS = 120L;

    private Type checkType;

    private long expireOn;

    private Identity identity;

    private int maxChecksPerExecution = DEFAULT_MAX_CHECKS_PER_EXECUTION;

    private String objectName;

    private long waitIntervalSeconds = DEFAULT_WAIT_INTERVAL_SECONDS;

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
